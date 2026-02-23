package org.dreambot.scripts.startpure.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.scripts.startpure.Constants;
import org.dreambot.scripts.startpure.ScriptContext;
import org.dreambot.scripts.startpure.ScriptState;
import org.dreambot.scripts.startpure.ScriptTask;

public class CollectGeItemsTask implements ScriptTask {

    private final ScriptContext ctx;

    public CollectGeItemsTask(ScriptContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int execute() {
        if (!GrandExchange.isOpen()) {
            GrandExchange.open();
            return Calculations.random(600, 1800);
        }

        if (GrandExchange.isReadyToCollect()) {
            // Salmon is non-stackable — collect to bank
            if (ctx.getBuyList()[ctx.getBuyIndex()][0] == Constants.SALMON) {
                GrandExchange.collectToBank();
            } else {
                GrandExchange.collect();
            }
            ctx.sleepUntil(() -> !GrandExchange.isReadyToCollect(), 5000, 300);
        }

        ctx.setCurrentItemCollected(true);
        ctx.setBuyIndex(ctx.getBuyIndex() + 1);
        ctx.setState(ScriptState.BUY_GE_ITEMS);
        return Calculations.random(600, 1200);
    }
}
