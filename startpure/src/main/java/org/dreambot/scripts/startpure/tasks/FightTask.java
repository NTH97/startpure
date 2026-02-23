package org.dreambot.scripts.startpure.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.scripts.startpure.Constants;
import org.dreambot.scripts.startpure.ScriptContext;
import org.dreambot.scripts.startpure.ScriptState;
import org.dreambot.scripts.startpure.ScriptTask;
import org.dreambot.scripts.startpure.TrainingLocation;
import org.dreambot.scripts.startpure.WeaponProgression;

public class FightTask implements ScriptTask {

    private final ScriptContext ctx;

    public FightTask(ScriptContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int execute() {
        int atk = Skills.getRealLevel(Skill.ATTACK);
        int str = Skills.getRealLevel(Skill.STRENGTH);

        // Goal check
        if (atk >= Constants.TARGET_ATTACK && str >= Constants.TARGET_STRENGTH) {
            ctx.log("Target levels reached! Attack: " + atk + ", Strength: " + str);
            ctx.setState(ScriptState.NOTIFY_DISCORD);
            return 600;
        }

        TrainingLocation location = TrainingLocation.getForLevels(atk, str);

        // Location check
        if (!location.getArea().contains(Players.getLocal())) {
            ctx.setState(ScriptState.WALK_TO_TRAINING);
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
                ctx.sleepUntil(() -> Players.getLocal().isInCombat(), 3000, 300);
            }
        }

        return Calculations.random(600, 1200);
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
        ctx.sleepUntil(Inventory::isEmpty, 3000, 300);
        Bank.depositAllItems();
        ctx.sleepUntil(Inventory::isEmpty, 3000, 300);

        // Re-equip correct gear
        Bank.withdraw(Constants.IRON_FULL_HELM, 1);
        Bank.withdraw(Constants.IRON_PLATEBODY, 1);
        Bank.withdraw(Constants.IRON_PLATELEGS, 1);
        Bank.withdraw(Constants.IRON_KITESHIELD, 1);
        Bank.withdraw(weaponId, 1);
        Bank.withdraw(Constants.SALMON, Constants.SALMON_WITHDRAW_AMOUNT);
        ctx.sleepUntil(() -> Inventory.contains(weaponId), 3000, 300);

        Bank.close();
        ctx.sleepUntil(() -> !Bank.isOpen(), 3000, 300);

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
            ctx.sleepUntil(() -> Inventory.contains(Constants.SALMON), 3000, 300);
        }

        Bank.close();
        ctx.sleepUntil(() -> !Bank.isOpen(), 3000, 300);

        ctx.setState(ScriptState.WALK_TO_TRAINING);
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
