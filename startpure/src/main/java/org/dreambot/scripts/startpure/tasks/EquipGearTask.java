package org.dreambot.scripts.startpure.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.scripts.startpure.Constants;
import org.dreambot.scripts.startpure.ScriptContext;
import org.dreambot.scripts.startpure.ScriptState;
import org.dreambot.scripts.startpure.ScriptTask;

public class EquipGearTask implements ScriptTask {

    private final ScriptContext ctx;

    public EquipGearTask(ScriptContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int execute() {
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
        ctx.sleepUntil(Inventory::isEmpty, 3000, 300);

        // Withdraw iron gear to equip
        Bank.withdraw(Constants.IRON_FULL_HELM, 1);
        Bank.withdraw(Constants.IRON_PLATEBODY, 1);
        Bank.withdraw(Constants.IRON_PLATELEGS, 1);
        Bank.withdraw(Constants.IRON_KITESHIELD, 1);
        Bank.withdraw(Constants.IRON_SCIMITAR, 1);
        Bank.withdraw(Constants.SALMON, Constants.SALMON_WITHDRAW_AMOUNT);
        ctx.sleepUntil(() -> Inventory.contains(Constants.IRON_SCIMITAR), 3000, 300);

        Bank.close();
        ctx.sleepUntil(() -> !Bank.isOpen(), 3000, 300);

        // Equip all iron gear
        equipItem(Constants.IRON_FULL_HELM);
        equipItem(Constants.IRON_PLATEBODY);
        equipItem(Constants.IRON_PLATELEGS);
        equipItem(Constants.IRON_KITESHIELD);
        equipItem(Constants.IRON_SCIMITAR);

        // Enable auto-retaliate
        Combat.toggleAutoRetaliate(true);

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
