package org.dreambot.scripts.startpure.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.trade.Trade;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.scripts.startpure.Constants;
import org.dreambot.scripts.startpure.ScriptContext;
import org.dreambot.scripts.startpure.ScriptState;
import org.dreambot.scripts.startpure.ScriptTask;

public class FindAndTradeTask implements ScriptTask {

    private final ScriptContext ctx;

    public FindAndTradeTask(ScriptContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int execute() {
        if (Trade.isOpen()) {
            ctx.setState(ScriptState.ACCEPT_TRADE);
            return 600;
        }

        Player partner = Players.closest(Constants.TRADE_PARTNER);
        if (partner == null) {
            ctx.log("Waiting for trade partner: " + Constants.TRADE_PARTNER);
            return Calculations.random(2000, 4000);
        }

        Trade.tradeWithPlayer(partner);
        ctx.sleepUntil(Trade::isOpen, 5000, 300);
        return Calculations.random(600, 1200);
    }
}
