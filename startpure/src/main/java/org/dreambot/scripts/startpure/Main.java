package org.dreambot.scripts.startpure;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;

@ScriptManifest(
        name = "Start Pure",
        description = "Automated pure account starter - trains 40 Attack and 40 Strength",
        author = "Developer",
        version = 1.0,
        category = Category.COMBAT,
        image = ""
)
public class Main extends AbstractScript {

    private ScriptState state = ScriptState.HOP_TO_TRADE_WORLD;

    // GE buy list: [itemId, quantity, price]
    private final int[][] buyList = {
            {Constants.IRON_SCIMITAR, 1, 5000},
            {Constants.MITHRIL_SCIMITAR, 1, 5000},
            {Constants.ADAMANT_SCIMITAR, 1, 10000},
            {Constants.RUNE_SCIMITAR, 1, 30000},
            {Constants.IRON_FULL_HELM, 1, 5000},
            {Constants.IRON_PLATEBODY, 1, 10000},
            {Constants.IRON_PLATELEGS, 1, 5000},
            {Constants.IRON_KITESHIELD, 1, 5000},
            {Constants.SALMON, Constants.SALMON_BUY_QUANTITY, 200}
    };
    private int buyIndex = 0;
    private boolean currentItemCollected = true;

    private int tradeFailsafe = 0;
    private int notifyRetries = 0;

    @Override
    public void onStart() {
        log("Start Pure script started. State: " + state);
    }

    @Override
    public int onLoop() {
        switch (state) {
            case HOP_TO_TRADE_WORLD:
                return handleHopToTradeWorld();
            case WALK_TO_GE:
                return handleWalkToGe();
            case FIND_AND_TRADE:
                return handleFindAndTrade();
            case ACCEPT_TRADE:
                return handleAcceptTrade();
            case WAIT_FOR_GOLD:
                return handleWaitForGold();
            case BUY_GE_ITEMS:
                return handleBuyGeItems();
            case COLLECT_GE_ITEMS:
                return handleCollectGeItems();
            case EQUIP_GEAR:
                return handleEquipGear();
            case WALK_TO_TRAINING:
                return handleWalkToTraining();
            case FIGHT:
                return handleFight();
            case NOTIFY_DISCORD:
                return handleNotifyDiscord();
            case FINISHED:
                log("Script complete! 40 Attack and 40 Strength achieved.");
                stop();
                return 600;
            default:
                return 600;
        }
    }

    private int handleHopToTradeWorld() {
        if (Worlds.getCurrentWorld() == Constants.TRADE_WORLD) {
            state = ScriptState.WALK_TO_GE;
            return 600;
        }
        log("Hopping to world " + Constants.TRADE_WORLD);
        WorldHopper.hopWorld(Constants.TRADE_WORLD);
        sleepUntil(() -> Worlds.getCurrentWorld() == Constants.TRADE_WORLD, 10000, 600);
        return Calculations.random(600, 1200);
    }

    private int handleWalkToGe() {
        if (Constants.GE_AREA.contains(Players.getLocal())) {
            state = ScriptState.FIND_AND_TRADE;
            return 600;
        }
        Walking.walk(Constants.GE_CENTER);
        return Calculations.random(600, 1800);
    }

    private int handleFindAndTrade() {
        if (Trade.isOpen()) {
            state = ScriptState.ACCEPT_TRADE;
            return 600;
        }

        Player partner = Players.closest(Constants.TRADE_PARTNER);
        if (partner == null) {
            log("Waiting for trade partner: " + Constants.TRADE_PARTNER);
            return Calculations.random(2000, 4000);
        }

        Trade.tradeWithPlayer(partner);
        sleepUntil(Trade::isOpen, 5000, 300);
        return Calculations.random(600, 1200);
    }

    private int handleAcceptTrade() {
        if (!Trade.isOpen()) {
            state = ScriptState.FIND_AND_TRADE;
            return 600;
        }

        if (Trade.isOpen(1)) {
            Trade.acceptTrade();
            sleepUntil(() -> Trade.isOpen(2), 5000, 300);
            return Calculations.random(600, 1200);
        }

        if (Trade.isOpen(2)) {
            Trade.acceptTrade();
            sleepUntil(() -> !Trade.isOpen(), 5000, 300);
            state = ScriptState.WAIT_FOR_GOLD;
            return Calculations.random(600, 1200);
        }

        return 600;
    }

    private int handleWaitForGold() {
        if (Inventory.count(Constants.COINS) >= Constants.GOLD_THRESHOLD) {
            log("Received gold. Moving to GE purchases.");
            buyIndex = 0;
            currentItemCollected = true;
            state = ScriptState.BUY_GE_ITEMS;
            return 600;
        }

        tradeFailsafe++;
        if (tradeFailsafe > Constants.TRADE_FAILSAFE_MAX) {
            log("Trade failsafe triggered. Retrying trade.");
            tradeFailsafe = 0;
            state = ScriptState.FIND_AND_TRADE;
            return 600;
        }

        return Calculations.random(1000, 2000);
    }

    private int handleBuyGeItems() {
        if (buyIndex >= buyList.length) {
            log("All GE items purchased and collected.");
            state = ScriptState.EQUIP_GEAR;
            return 600;
        }

        if (!currentItemCollected) {
            state = ScriptState.COLLECT_GE_ITEMS;
            return 600;
        }

        if (!GrandExchange.isOpen()) {
            GrandExchange.open();
            return Calculations.random(600, 1800);
        }

        int id = buyList[buyIndex][0];
        int qty = buyList[buyIndex][1];
        int price = buyList[buyIndex][2];

        log("Buying item ID " + id + " x" + qty + " at " + price + "gp each");
        GrandExchange.buyItem(id, qty, price);
        sleepUntil(GrandExchange::isReadyToCollect, 15000, 600);
        currentItemCollected = false;
        state = ScriptState.COLLECT_GE_ITEMS;
        return Calculations.random(600, 1200);
    }

    private int handleCollectGeItems() {
        if (!GrandExchange.isOpen()) {
            GrandExchange.open();
            return Calculations.random(600, 1800);
        }

        if (GrandExchange.isReadyToCollect()) {
            // Salmon is non-stackable — collect to bank
            if (buyList[buyIndex][0] == Constants.SALMON) {
                GrandExchange.collectToBank();
            } else {
                GrandExchange.collect();
            }
            sleepUntil(() -> !GrandExchange.isReadyToCollect(), 5000, 300);
        }

        currentItemCollected = true;
        buyIndex++;
        state = ScriptState.BUY_GE_ITEMS;
        return Calculations.random(600, 1200);
    }

    private int handleEquipGear() {
        if (GrandExchange.isOpen()) {
            GrandExchange.close();
            return Calculations.random(600, 1200);
        }

        // Open bank to deposit higher-tier scimitars and coins, withdraw salmon
        if (!Bank.isOpen()) {
            Bank.open();
            return Calculations.random(600, 1200);
        }

        // Deposit everything first
        Bank.depositAllItems();
        sleepUntil(Inventory::isEmpty, 3000, 300);

        // Withdraw iron gear to equip
        Bank.withdraw(Constants.IRON_FULL_HELM, 1);
        Bank.withdraw(Constants.IRON_PLATEBODY, 1);
        Bank.withdraw(Constants.IRON_PLATELEGS, 1);
        Bank.withdraw(Constants.IRON_KITESHIELD, 1);
        Bank.withdraw(Constants.IRON_SCIMITAR, 1);
        Bank.withdraw(Constants.SALMON, Constants.SALMON_WITHDRAW_AMOUNT);
        sleepUntil(() -> Inventory.contains(Constants.IRON_SCIMITAR), 3000, 300);

        Bank.close();
        sleepUntil(() -> !Bank.isOpen(), 3000, 300);

        // Equip all iron gear
        equipItem(Constants.IRON_FULL_HELM);
        equipItem(Constants.IRON_PLATEBODY);
        equipItem(Constants.IRON_PLATELEGS);
        equipItem(Constants.IRON_KITESHIELD);
        equipItem(Constants.IRON_SCIMITAR);

        // Enable auto-retaliate
        Combat.toggleAutoRetaliate(true);

        state = ScriptState.WALK_TO_TRAINING;
        return Calculations.random(600, 1200);
    }

    private int handleWalkToTraining() {
        int atk = Skills.getRealLevel(Skill.ATTACK);
        int str = Skills.getRealLevel(Skill.STRENGTH);
        TrainingLocation location = TrainingLocation.getForLevels(atk, str);

        if (location.getArea().contains(Players.getLocal())) {
            state = ScriptState.FIGHT;
            return 600;
        }

        Walking.walk(location.getCenterTile());
        return Calculations.random(600, 1800);
    }

    private int handleFight() {
        int atk = Skills.getRealLevel(Skill.ATTACK);
        int str = Skills.getRealLevel(Skill.STRENGTH);

        // Goal check
        if (atk >= Constants.TARGET_ATTACK && str >= Constants.TARGET_STRENGTH) {
            log("Target levels reached! Attack: " + atk + ", Strength: " + str);
            state = ScriptState.NOTIFY_DISCORD;
            return 600;
        }

        TrainingLocation location = TrainingLocation.getForLevels(atk, str);

        // Location check
        if (!location.getArea().contains(Players.getLocal())) {
            state = ScriptState.WALK_TO_TRAINING;
            return 600;
        }

        // Weapon upgrade check
        WeaponProgression bestWeapon = WeaponProgression.getBestForLevel(atk);
        if (!Equipment.contains(bestWeapon.getItemId())) {
            if (Inventory.contains(bestWeapon.getItemId())) {
                equipItem(bestWeapon.getItemId());
                return Calculations.random(600, 1200);
            } else {
                // Need to bank for the upgraded weapon
                return bankForWeaponAndFood(bestWeapon.getItemId());
            }
        }

        // Combat style management
        setCombatStyleForLevels(atk, str);

        // Eat food if needed
        int hpPercent = (Skills.getBoostedLevel(Skill.HITPOINTS) * 100) / Skills.getRealLevel(Skill.HITPOINTS);
        if (hpPercent <= Constants.EAT_HP_PERCENT) {
            if (Inventory.contains(Constants.SALMON)) {
                Inventory.interact(Constants.SALMON, "Eat");
                return Calculations.random(600, 1200);
            } else {
                // Rebank for food
                return bankForFood();
            }
        }

        // No food left — rebank before we get too low
        if (!Inventory.contains(Constants.SALMON) && hpPercent < 80) {
            return bankForFood();
        }

        // Attack if not in combat
        if (!Players.getLocal().isInCombat()) {
            NPC target = NPCs.closest(npc ->
                    npc != null
                    && npc.getName() != null
                    && npc.getName().equals(location.getNpcName())
                    && location.getArea().contains(npc)
                    && !npc.isInCombat()
                    && npc.getHealthPercent() > 0
            );
            if (target != null) {
                target.interact("Attack");
                sleepUntil(() -> Players.getLocal().isInCombat(), 3000, 300);
            }
        }

        return Calculations.random(600, 1200);
    }

    private int handleNotifyDiscord() {
        String message = "ACCOUNT: " + Constants.ACCOUNT_CREDENTIALS;
        boolean success = DiscordNotifier.sendNotification(Constants.DISCORD_WEBHOOK_URL, message);
        if (success) {
            log("Discord notification sent successfully.");
            state = ScriptState.FINISHED;
        } else {
            notifyRetries++;
            if (notifyRetries >= 3) {
                log("Failed to send Discord notification after 3 attempts. Finishing anyway.");
                state = ScriptState.FINISHED;
            } else {
                log("Discord notification failed. Retrying...");
            }
        }
        return Calculations.random(1000, 2000);
    }

    private void setCombatStyleForLevels(int atk, int str) {
        // Alternating training: atk→20, str→20, atk→30, str→30, atk→40, str→40
        // Combat mode index: 0 = Accurate (Attack), 1 = Aggressive (Strength)
        int desiredStyle;
        if (atk < 20) {
            desiredStyle = 0; // Attack
        } else if (str < 20) {
            desiredStyle = 1; // Strength
        } else if (atk < 30) {
            desiredStyle = 0;
        } else if (str < 30) {
            desiredStyle = 1;
        } else if (atk < 40) {
            desiredStyle = 0;
        } else {
            desiredStyle = 1; // Strength to 40
        }

        int currentStyle = Combat.getCombatModeIndex();
        if (currentStyle != desiredStyle) {
            Combat.setCombatModeIndex(desiredStyle);
        }
    }

    private int bankForWeaponAndFood(int weaponId) {
        if (!Bank.isOpen()) {
            Bank.open();
            return Calculations.random(600, 1800);
        }

        // Deposit current weapon
        Bank.depositAllEquipment();
        sleepUntil(Inventory::isEmpty, 3000, 300);
        Bank.depositAllItems();
        sleepUntil(Inventory::isEmpty, 3000, 300);

        // Re-equip correct gear
        Bank.withdraw(Constants.IRON_FULL_HELM, 1);
        Bank.withdraw(Constants.IRON_PLATEBODY, 1);
        Bank.withdraw(Constants.IRON_PLATELEGS, 1);
        Bank.withdraw(Constants.IRON_KITESHIELD, 1);
        Bank.withdraw(weaponId, 1);
        Bank.withdraw(Constants.SALMON, Constants.SALMON_WITHDRAW_AMOUNT);
        sleepUntil(() -> Inventory.contains(weaponId), 3000, 300);

        Bank.close();
        sleepUntil(() -> !Bank.isOpen(), 3000, 300);

        equipItem(Constants.IRON_FULL_HELM);
        equipItem(Constants.IRON_PLATEBODY);
        equipItem(Constants.IRON_PLATELEGS);
        equipItem(Constants.IRON_KITESHIELD);
        equipItem(weaponId);

        return Calculations.random(600, 1200);
    }

    private int bankForFood() {
        if (!Bank.isOpen()) {
            Bank.open();
            if (!Bank.isOpen()) {
                Walking.walk(Constants.LUMBRIDGE_BANK_AREA.getCenter());
                return Calculations.random(600, 1800);
            }
            return Calculations.random(600, 1200);
        }

        Bank.depositAllExcept(item ->
                item != null && item.getName() != null && (
                        item.getName().contains("scimitar") ||
                        item.getName().contains("Iron full helm") ||
                        item.getName().contains("Iron platebody") ||
                        item.getName().contains("Iron platelegs") ||
                        item.getName().contains("Iron kiteshield")
                )
        );

        if (Bank.contains(Constants.SALMON)) {
            Bank.withdraw(Constants.SALMON, Constants.SALMON_WITHDRAW_AMOUNT);
            sleepUntil(() -> Inventory.contains(Constants.SALMON), 3000, 300);
        }

        Bank.close();
        sleepUntil(() -> !Bank.isOpen(), 3000, 300);

        state = ScriptState.WALK_TO_TRAINING;
        return Calculations.random(600, 1200);
    }

    private void equipItem(int itemId) {
        if (Inventory.contains(itemId)) {
            Inventory.interact(itemId, "Wield");
            if (itemId == Constants.IRON_FULL_HELM) {
                Inventory.interact(itemId, "Wear");
            }
        }
    }
}
