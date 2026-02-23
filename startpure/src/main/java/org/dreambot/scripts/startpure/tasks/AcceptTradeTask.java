package org.dreambot.scripts.startpure.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.scripts.startpure.ScriptContext;
import org.dreambot.scripts.startpure.ScriptState;
import org.dreambot.scripts.startpure.ScriptTask;

public class AcceptTradeTask implements ScriptTask {

    private final ScriptContext ctx;

    public AcceptTradeTask(ScriptContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int execute() {
        if (!Trade.isOpen()) {
            ctx.setState(ScriptState.FIND_AND_TRADE);
            return 600;
        }

        if (Trade.isOpen(1)) {
            Trade.acceptTrade();
            ctx.sleepUntil(() -> Trade.isOpen(2), 5000, 300);
            return Calculations.random(600, 1200);
        }

        if (Trade.isOpen(2)) {
            Trade.acceptTrade();
            ctx.sleepUntil(() -> !Trade.isOpen(), 5000, 300);
            ctx.setState(ScriptState.WAIT_FOR_GOLD);
            return Calculations.random(600, 1200);
        }

        return 600;
    }
}
