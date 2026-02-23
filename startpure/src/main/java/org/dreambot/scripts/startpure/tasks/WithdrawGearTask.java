package org.dreambot.scripts.startpure.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.scripts.startpure.Constants;
import org.dreambot.scripts.startpure.ScriptContext;
import org.dreambot.scripts.startpure.ScriptState;
import org.dreambot.scripts.startpure.ScriptTask;

public class WithdrawGearTask implements ScriptTask {

    private final ScriptContext ctx;

    public WithdrawGearTask(ScriptContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int execute() {
        if (!Bank.isOpen()) {
            Bank.open();
            return Calculations.random(600, 1200);
        }

        // Withdraw armour
        Bank.withdraw(Constants.IRON_FULL_HELM, 1);
        Bank.withdraw(Constants.IRON_PLATEBODY, 1);
        Bank.withdraw(Constants.IRON_PLATELEGS, 1);
        Bank.withdraw(Constants.IRON_KITESHIELD, 1);

        // Withdraw all weapon tiers for upgrades during training
        Bank.withdraw(Constants.IRON_SCIMITAR, 1);
        Bank.withdraw(Constants.MITHRIL_SCIMITAR, 1);
        Bank.withdraw(Constants.ADAMANT_SCIMITAR, 1);
        Bank.withdraw(Constants.RUNE_SCIMITAR, 1);

        // Withdraw food
        Bank.withdraw(Constants.SALMON, Constants.SALMON_WITHDRAW_AMOUNT);

        ctx.sleepUntil(() -> Inventory.contains(Constants.IRON_SCIMITAR), 3000, 300);

        Bank.close();
        ctx.sleepUntil(() -> !Bank.isOpen(), 3000, 300);

        ctx.log("Gear and weapons withdrawn.");
        ctx.setState(ScriptState.EQUIP_GEAR);
        return Calculations.random(300, 600);
    }
}
