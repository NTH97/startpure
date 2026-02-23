package org.dreambot.scripts.startpure.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.scripts.startpure.Constants;
import org.dreambot.scripts.startpure.ScriptContext;
import org.dreambot.scripts.startpure.ScriptState;
import org.dreambot.scripts.startpure.ScriptTask;

public class WaitForGoldTask implements ScriptTask {

    private final ScriptContext ctx;

    public WaitForGoldTask(ScriptContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int execute() {
        if (Inventory.count(Constants.COINS) >= Constants.GOLD_THRESHOLD) {
            ctx.log("Received gold. Moving to GE purchases.");
            ctx.setBuyIndex(0);
            ctx.setCurrentItemCollected(true);
            ctx.setState(ScriptState.BUY_GE_ITEMS);
            return 600;
        }

        ctx.setTradeFailsafe(ctx.getTradeFailsafe() + 1);
        if (ctx.getTradeFailsafe() > Constants.TRADE_FAILSAFE_MAX) {
            ctx.log("Trade failsafe triggered. Retrying trade.");
            ctx.setTradeFailsafe(0);
            ctx.setState(ScriptState.FIND_AND_TRADE);
            return 600;
        }

        return Calculations.random(1000, 2000);
    }
}
