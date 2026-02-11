package org.dreambot.api.script;

import org.dreambot.api.Client;
import org.dreambot.api.ClientSettings;
import org.dreambot.api.input.Keyboard;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.poh.FurnitureItem;
import org.dreambot.api.script.poh.HouseUtils;
import org.dreambot.api.script.poh.JewelleryBox;
import org.dreambot.api.script.poh.Pool;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.launcher.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ScriptManifest(name = "Vital PoH Builder", description = "Automates Player-Owned House construction", author = "Developer",
        version = 1.3, category = Category.CONSTRUCTION, image = "")
public class Main extends AbstractScript {

    // Varbit IDs
    private static final int VARBIT_SETTINGS_SIDE_PANEL_TAB = 4607;
    private static final int VARBIT_POH_TELE_TOGGLE = 6719;
    private static final int VARBIT_ACCEPT_AID = 4180;
    private static final int VARBIT_YAMA_HORN_RADIUS = 14386;
    private static final int VARBIT_YAMA_HORN_MAX_PLAYERS = 14387;

    // Item IDs
    private static final int COINS = 995;
    private static final int HAMMER = 2347;
    private static final int SAW = 8794;
    private static final int HOUSE_TAB = 8013;
    private static final int RING_OF_WEALTH_5 = 2572;
    private static final int YAMA_HORN = 30759;
    private static final int DRAGON_AXE = 6739;
    private static final int RUNE_AXE = 1359;
    private static final int DWARF_CANNON_SET = 12863;
    private static final int NOTED_ITEM_24587 = 24587;
    private static final int CANNON_PART_12791 = 12791;
    private static final int RING_OF_WEALTH_UNCHARGED = 2572;

    // Cannon part IDs
    private static final int CANNON_BASE = 6;
    private static final int CANNON_STAND = 8;
    private static final int CANNON_BARRELS = 10;
    private static final int CANNON_FURNACE = 12;

    // Config
    private int maxDelay = 3;

    // State
    private boolean needsToBuyHouseTab = false;
    private boolean needsToBuyRingOfWealth = false;
    private boolean buyHammer = false;
    private boolean buySaw = false;
    private boolean shouldWoodCut = false;
    private boolean drop = false;
    private boolean depositInventory = true;
    private int failsafe = 0;
    private List<FurnitureItem> poolFurnitureItems = new ArrayList<>();
    private List<FurnitureItem> jewelleryBoxFurnitureItems = new ArrayList<>();

    private final Tile yews = new Tile(3210, 3503, 0);

    @Override
    public void onStart() {
        failsafe = 0;
    }

    @Override
    public int onLoop() {

        if (failsafe >= 3) {
            return 600;
        }

        // Close settings side panel if open
        if (!Bank.isOpen() && Tabs.isOpen(Tab.OPTIONS) && PlayerSettings.getBitValue(VARBIT_SETTINGS_SIDE_PANEL_TAB) != 0) {
            WidgetChild settingsWidget = Widgets.get(116, 99);
            if (settingsWidget != null && settingsWidget.isVisible()) {
                settingsWidget.interact();
            }
            return Calculations.random(1200, 1800);
        }

        // Deposit inventory first
        if (depositInventory) {
            if (Inventory.isEmpty()) {
                depositInventory = false;
                return 600;
            }

            if (!Bank.isOpen()) {
                Bank.open();
                return Calculations.random(600, 1800);
            }

            Bank.depositAllItems();
            return Calculations.random(600, 1800);
        }

        // Ensure house teleport toggle is set correctly (inside mode)
        if (PlayerSettings.getBitValue(VARBIT_POH_TELE_TOGGLE) != 0) {
            if (Bank.isOpen()) {
                Bank.close();
                return 600;
            }

            if (!HouseUtils.isHouseOptionsOpen()) {
                HouseUtils.openHouseOptions();
                return 1200;
            }

            WidgetChild widget = Widgets.get(370, 8);
            if (widget != null && widget.isVisible()) {
                widget.interact();
            }
            return 1200;
        }

        // Ensure accept aid is on
        if (PlayerSettings.getBitValue(VARBIT_ACCEPT_AID) != 1) {
            if (Bank.isOpen()) {
                Bank.close();
                return 600;
            }
            if (HouseUtils.isHouseOptionsOpen()) {
                Walking.walk(Players.getLocal().getTile());
                return 600;
            }

            if (!Tabs.isOpen(Tab.OPTIONS)) {
                Tabs.open(Tab.OPTIONS);
                return 1200;
            }

            WidgetChild widget = Widgets.get(116, 29);
            if (widget != null && widget.isVisible()) {
                widget.interact();
            }
            return 1200;
        }

        // Switch to Necromancy spellbook
        if (getSpellbook() != 3) { // 4 = Necromancy spellbook
            if (Bank.isOpen()) {
                Bank.close();
                return 600;
            }
            NPC tyss = NPCs.closest("Tyss");
            if (tyss == null) {
                Walking.walk(new Tile(1712, 3884, 0));
                return Calculations.random(600, 1800);
            }

            tyss.interact("Spellbook");
            return 600;
        }

        // Yama Horn configuration
        if (PlayerSettings.getBitValue(VARBIT_YAMA_HORN_RADIUS) != 3 || PlayerSettings.getBitValue(VARBIT_YAMA_HORN_MAX_PLAYERS) != 2) {
            if (Bank.isOpen() && Bank.contains(YAMA_HORN)) {
                Bank.withdraw(YAMA_HORN);
                return 600;
            }

            if (Equipment.contains(YAMA_HORN)) {
                if (Dialogues.inDialogue()) {
                    if (PlayerSettings.getBitValue(VARBIT_YAMA_HORN_RADIUS) != 3) {
                        if (isEnterInputOpen()) {
                            Keyboard.type("3", true);
                            return 600;
                        }
                        Dialogues.chooseFirstOptionContaining("Change Radius");
                        return 600;
                    }
                    if (PlayerSettings.getBitValue(VARBIT_YAMA_HORN_MAX_PLAYERS) != 2) {
                        if (isEnterInputOpen()) {
                            Keyboard.type("2", true);
                            return 600;
                        }
                        Dialogues.chooseFirstOptionContaining("Change Max");
                        return 600;
                    }
                    return 600;
                }

                Item hornEquipped = Equipment.get(YAMA_HORN);
                if (hornEquipped != null) {
                    hornEquipped.interact("Configure");
                }
                return 600;
            }

            if (Inventory.contains(YAMA_HORN)) {
                Item horn = Inventory.get(YAMA_HORN);
                if (horn != null) {
                    horn.interact("Wear");
                }
                return 600;
            }
        } else if (Inventory.contains(YAMA_HORN) || Equipment.contains(YAMA_HORN)) {
            if (Equipment.contains(YAMA_HORN)) {
                Item hornEquipped = Equipment.get(YAMA_HORN);
                if (hornEquipped != null) {
                    hornEquipped.interact("Remove");
                }
                return 600;
            }

            if (!Bank.isOpen()) {
                Bank.open();
                return 600;
            }

            Bank.deposit(YAMA_HORN);
            return 600;
        }

        // Woodcutting level cap check
        if (Skills.getRealLevel(Skill.WOODCUTTING) == 65) {
            Tabs.logout();
            return 600;
        }

        // Woodcutting mode
        if (shouldWoodCut) {
            return handleWoodcutting();
        }

        // Cannon set handling
        if (Inventory.contains(DWARF_CANNON_SET)) {
            if (Widgets.isVisible(451, 0)) {
                Item cannonSet = Inventory.get(DWARF_CANNON_SET);
                if (cannonSet != null) {
                    if (failsafe >= 3) {
                        stop();
                        return 600;
                    }
                    cannonSet.interact("Unpack");
                    failsafe++;
                    return 1800;
                }
                return Calculations.random(600, 1800);
            }

            NPC clerk = NPCs.closest("Grand Exchange Clerk");
            if (clerk != null) {
                clerk.interact("Sets");
            }
            return Calculations.random(600, 1800);
        }

        // Noted item handling
        if (Inventory.contains(NOTED_ITEM_24587)) {
            NPC banker = NPCs.closest("Banker");
            Item note = Inventory.get(NOTED_ITEM_24587);
            if (note != null && banker != null) {
                note.interact("Use");
                sleepUntil(Inventory::isItemSelected, 2000, 300);
                banker.interact("Use");
            }

            return Calculations.random(600, 1800);
        }

        // Bank noted item withdrawal
        if (Bank.isOpen() && Bank.contains(NOTED_ITEM_24587) && !Bank.contains(CANNON_PART_12791)) {
            Bank.withdraw(NOTED_ITEM_24587);
            return Calculations.random(600, 1800);
        }

        // Deposit cannon parts
        if (Inventory.contains(CANNON_PART_12791)) {
            if (!Bank.isOpen()) {
                Bank.open();
                return 600;
            }
            Bank.depositAll(CANNON_PART_12791);
            return Calculations.random(600, 1800);
        }

        // Withdraw cannon set from bank
        if (Bank.isOpen() && Bank.contains(DWARF_CANNON_SET)) {
            Bank.withdraw(DWARF_CANNON_SET);
            return Calculations.random(600, 1800);
        }

        // Deposit individual cannon parts
        if (Inventory.contains(CANNON_BASE) || Inventory.contains(CANNON_STAND)
                || Inventory.contains(CANNON_BARRELS) || Inventory.contains(CANNON_FURNACE)) {
            if (!Bank.isOpen()) {
                Bank.open();
                return 600;
            }
            Bank.depositAll(CANNON_BASE);
            Bank.depositAll(CANNON_STAND);
            Bank.depositAll(CANNON_BARRELS);
            Bank.depositAll(CANNON_FURNACE);
            return Calculations.random(600, 1800);
        }

        // Deposit ring of wealth if in bank
        if (Bank.isOpen() && Inventory.contains(RING_OF_WEALTH_UNCHARGED)) {
            Bank.depositAll(RING_OF_WEALTH_UNCHARGED);
            return 600;
        }

        // Handle dialogues
        if (Dialogues.inDialogue() && Dialogues.getOptions() != null) {
            for (String option : Dialogues.getOptions()) {
                if (option != null && option.contains("Yes")) {
                    Dialogues.chooseOption("Yes");
                    return 600;
                }
            }
        }

        // Buy saw if needed
        if (buySaw) {
            buyItem(SAW, 1, 10000, () -> buySaw = false);
            return Calculations.random(600, 1800);
        }

        // Buy hammer if needed
        if (buyHammer) {
            buyItem(HAMMER, 1, 10000, () -> buyHammer = false);
            return Calculations.random(600, 1800);
        }

        // Buy ring of wealth if needed
        if (needsToBuyRingOfWealth) {
            buyItem(RING_OF_WEALTH_5, 1, 25000, () -> needsToBuyRingOfWealth = false);
            return Calculations.random(600, 1800);
        }

        // Buy house tabs if needed
        if (needsToBuyHouseTab) {
            buyItem(HOUSE_TAB, 10, 3000, () -> needsToBuyHouseTab = false);
            return Calculations.random(600, 1800);
        }

        // Purchase/withdraw pool furniture items
        if (!poolFurnitureItems.isEmpty()) {
            for (FurnitureItem furnitureItem : poolFurnitureItems) {
                if (!furnitureItem.isNeedsToPurchase()) {
                    withdrawFurnitureItem(furnitureItem);
                    return Calculations.random(600, maxDelay * 600);
                }
                buyFurnitureItem(furnitureItem);
                break;
            }
            return Calculations.random(600, 1800);
        }

        // Equip ring of wealth
        if (!Equipment.contains(item -> item.getName() != null && item.getName().toLowerCase().contains("ring of wealth "))) {
            if (Inventory.contains(item -> item.getName() != null && item.getName().toLowerCase().contains("ring of wealth "))) {
                Item ring = Inventory.get(item -> item.getName() != null && item.getName().toLowerCase().contains("ring of wealth "));
                if (ring != null) {
                    ring.interact("Wear");
                }
                return 600;
            }
            return withdrawRingOfWealth();
        }

        // Withdraw saw
        if (!Inventory.contains(SAW)) {
            return withdrawSaw();
        }

        // Withdraw hammer
        if (!Inventory.contains(HAMMER)) {
            return withdrawHammer();
        }

        // Withdraw coins
        if (!Inventory.contains(COINS)) {
            if (!Bank.isOpen()) {
                Bank.open();
                return 1200;
            }
            Bank.withdrawAll(COINS);
            return 1200;
        }

        // Withdraw house tabs
        if (!Inventory.contains(HOUSE_TAB)) {
            return withdrawHouseTab();
        }

        // Teleport to house
        if (!isInsideHouse()) {
            Item tab = Inventory.get(HOUSE_TAB);
            if (tab != null) {
                tab.interact("Inside");
            }
            return Calculations.random(600, maxDelay * 600);
        }

        boolean isViewerOpened = HouseUtils.isViewerOpened();
        boolean isBuildRoomOpen = HouseUtils.isBuildRoomOpen();

        // Enter build mode
        if (!HouseUtils.isInBuildMode() && !isViewerOpened && !isBuildRoomOpen) {
            log("open build mode");
            HouseUtils.openBuildMode();
            return Calculations.random(600, maxDelay * 600);
        }

        // Build pool if needed
        Pool poolToBuild = Pool.getPool();
        if (poolToBuild != null) {
            handlePoolItems(poolToBuild);
            return 3000;
        }

        // Build jewellery box if needed
        JewelleryBox jewelleryBoxToBuild = JewelleryBox.getJewelleryBox();
        if (jewelleryBoxToBuild != null) {
            handleJewelleryBoxItems(jewelleryBoxToBuild);
            return 3000;
        }

        // Open viewer if not open
        if (!isViewerOpened && !isBuildRoomOpen) {
            log("open viewer");
            HouseUtils.openViewer();
            return Calculations.random(600, maxDelay * 600);
        }

        // Build superior garden room
        if (HouseUtils.needsSuperiorGarden()) {
            log("build superior garden");
            HouseUtils.buildSuperiorGarden(maxDelay);
            return Calculations.random(600, maxDelay * 600);
        }

        // Build achievement gallery room
        if (HouseUtils.needsAchievementGallery()) {
            log("build achievement gallery");
            HouseUtils.buildAchievementGallery(maxDelay);
            return Calculations.random(600, maxDelay * 600);
        }

        // All done - switch to woodcutting
        shouldWoodCut = true;
        log("done");
        return 600;
    }

    private void handlePoolItems(Pool poolToBuild) {
        poolFurnitureItems = poolToBuild.getMissingItems();
        if (!poolFurnitureItems.isEmpty()) {
            return;
        }

        if (HouseUtils.isFurnitureCreationOpen()) {
            poolToBuild.build();
            return;
        }

        poolToBuild.interact();
    }

    private void handleJewelleryBoxItems(JewelleryBox jewelleryBox) {
        poolFurnitureItems = jewelleryBox.getMissingItems();
        if (!poolFurnitureItems.isEmpty()) {
            return;
        }

        if (HouseUtils.isFurnitureCreationOpen()) {
            jewelleryBox.build();
            return;
        }

        jewelleryBox.interact();
    }

    private int withdrawHouseTab() {
        if (Bank.isOpen()) {
            if (!Bank.contains(HOUSE_TAB)) {
                needsToBuyHouseTab = true;
                return 600;
            }
            Bank.withdrawAll(HOUSE_TAB);
            return 600;
        }

        if (!Players.getLocal().isMoving()) {
            Bank.open();
        }
        return Calculations.random(600, 1800);
    }

    private int withdrawRingOfWealth() {
        if (Bank.isOpen()) {
            if (!Bank.contains(item -> item.getName() != null && item.getName().toLowerCase().contains("ring of wealth "))) {
                needsToBuyRingOfWealth = true;
                return 600;
            }
            Bank.withdraw(item -> item.getName() != null && item.getName().toLowerCase().contains("ring of wealth "));
            return 600;
        }

        if (!Players.getLocal().isMoving()) {
            Bank.open();
        }
        return Calculations.random(600, 1800);
    }

    private int withdrawHammer() {
        if (Bank.isOpen()) {
            if (!Bank.contains("Hammer")) {
                buyHammer = true;
                return 600;
            }
            Bank.withdraw("Hammer");
            return 600;
        }

        if (!Players.getLocal().isMoving()) {
            Bank.open();
        }
        return Calculations.random(600, 1800);
    }

    private int withdrawSaw() {
        if (Bank.isOpen()) {
            if (!Bank.contains("Saw")) {
                buySaw = true;
                return 600;
            }
            Bank.withdraw("Saw");
            return 600;
        }

        if (!Players.getLocal().isMoving()) {
            Bank.open();
        }
        return Calculations.random(600, 1800);
    }

    private void withdrawFurnitureItem(FurnitureItem furnitureItem) {
        if (Inventory.count(furnitureItem.getId()) >= furnitureItem.getQuantity()) {
            poolFurnitureItems.remove(furnitureItem);
            return;
        }

        if (Bank.isOpen()) {
            if (!Bank.contains(furnitureItem.getId()) || Bank.count(furnitureItem.getId()) < furnitureItem.getQuantity()) {
                furnitureItem.setNeedsToPurchase(true);
                return;
            }
            Bank.withdraw(furnitureItem.getId(), furnitureItem.getQuantity());
            return;
        }

        if (!Players.getLocal().isMoving()) {
            Bank.open();
        }
    }

    private void buyItem(int id, int quantity, int price, Runnable onComplete) {
        if (!GrandExchange.isOpen()) {
            GrandExchange.open();
            return;
        }

        if (GrandExchange.isReadyToCollect()) {
            GrandExchange.collect();
            onComplete.run();
            return;
        }

        GrandExchange.buyItem(id, quantity, price);
    }

    private void buyFurnitureItem(FurnitureItem furnitureItem) {
        if (!GrandExchange.isOpen()) {
            GrandExchange.open();
            return;
        }

        if (GrandExchange.isReadyToCollect()) {
            GrandExchange.collect();
            furnitureItem.setNeedsToPurchase(false);
            return;
        }

        GrandExchange.buyItem(furnitureItem.getId(), furnitureItem.getQuantity(), furnitureItem.getPrice());
    }

    private int handleWoodcutting() {
        if (Equipment.contains(DRAGON_AXE) || Equipment.contains(RUNE_AXE)) {
            if (!Inventory.contains("Yew logs")) {
                drop = false;
            }

            if ((Inventory.isFull() && Inventory.contains("Yew logs")) || drop) {
                if (Inventory.isFull()) {
                    drop = true;
                }

                List<Item> logs = Inventory.all(item -> item.getName() != null && item.getName().equals("Yew logs"));
                int i = Calculations.random(0, 2);
                for (Item log : logs) {
                    if (i >= 3) {
                        return Calculations.random(0, 600);
                    }
                    log.interact("Drop");
                    i++;
                }
                return 600;
            }

            GameObject tree = GameObjects.closest("Yew tree");
            if (tree == null) {
                Walking.walk(yews);
                return Calculations.random(600, 1800);
            }

            if (Equipment.contains(DRAGON_AXE) && Combat.getSpecialPercentage() == 100) {
                Combat.toggleSpecialAttack(true);
                return Calculations.random(600, 1200);
            }

            tree.interact("Chop down");
            return 600;
        } else if (Inventory.contains(DRAGON_AXE) || Inventory.contains(RUNE_AXE)) {
            if (Bank.isOpen()) {
                Bank.close();
                return 600;
            }

            Item axe = Inventory.get(DRAGON_AXE);
            if (axe == null) {
                axe = Inventory.get(RUNE_AXE);
            }
            if (axe != null) {
                axe.interact("Wield");
            }
            return 600;
        }

        // Withdraw axe from bank
        if (!Bank.isOpen()) {
            Bank.open();
            return 600;
        }

        if (Bank.contains(DRAGON_AXE)) {
            Bank.withdraw(DRAGON_AXE);
            return 1200;
        } else if (Bank.contains(RUNE_AXE)) {
            Bank.withdraw(RUNE_AXE);
            return 1200;
        }

        return Calculations.random(600, 1800);
    }

    private boolean isInsideHouse() {
        // Check if the player is in a POH instance by checking the map region
        // POH regions are typically 7513, 7514, 7769, 7770
        if (Players.getLocal() == null) {
            return false;
        }
        Tile tile = Players.getLocal().getTile();
        if (tile == null) {
            return false;
        }
        int regionId = (tile.getX() >> 6) * 256 + (tile.getY() >> 6);
        return regionId == 7513 || regionId == 7514 || regionId == 7769 || regionId == 7770;
    }

    private int getSpellbook() {
        // Varp 439 contains the current spellbook value
        // 0 = Standard, 1 = Ancient, 2 = Lunar, 3 = Arceuus, 4 = Necromancy
        return PlayerSettings.getBitValue(4070);
    }

    private boolean isEnterInputOpen() {
        return Widgets.isVisible(162, 32);
    }
}
