package org.dreambot.scripts.startpure.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.scripts.startpure.ScriptContext;
import org.dreambot.scripts.startpure.ScriptState;
import org.dreambot.scripts.startpure.ScriptTask;

public class DepositInventoryTask implements ScriptTask {

    private final ScriptContext ctx;

    public DepositInventoryTask(ScriptContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int execute() {
        if (GrandExchange.isOpen()) {
            GrandExchange.close();
            return Calculations.random(600, 1200);
        }

        if (!Bank.isOpen()) {
            Bank.open();
            return Calculations.random(600, 1200);
        }

        if (!Inventory.isEmpty()) {
            Bank.depositAllItems();
            ctx.sleepUntil(Inventory::isEmpty, 3000, 300);
            return Calculations.random(300, 600);
        }

        ctx.log("Inventory deposited.");
        ctx.setState(ScriptState.WITHDRAW_GEAR);
        return Calculations.random(300, 600);
    }
}
