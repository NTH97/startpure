package org.dreambot.scripts.startpure.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.scripts.startpure.ScriptContext;
import org.dreambot.scripts.startpure.ScriptState;
import org.dreambot.scripts.startpure.ScriptTask;

public class BuyGeItemsTask implements ScriptTask {

    private final ScriptContext ctx;

    public BuyGeItemsTask(ScriptContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int execute() {
        int[][] buyList = ctx.getBuyList();

        if (ctx.getBuyIndex() >= buyList.length) {
            ctx.log("All GE items purchased and collected.");
            ctx.setState(ScriptState.DEPOSIT_INVENTORY);
            return 600;
        }

        if (!ctx.isCurrentItemCollected()) {
            ctx.setState(ScriptState.COLLECT_GE_ITEMS);
            return 600;
        }

        if (!GrandExchange.isOpen()) {
            GrandExchange.open();
            return Calculations.random(600, 1800);
        }

        int id = buyList[ctx.getBuyIndex()][0];
        int qty = buyList[ctx.getBuyIndex()][1];
        int price = buyList[ctx.getBuyIndex()][2];

        ctx.log("Buying item ID " + id + " x" + qty + " at " + price + "gp each");
        GrandExchange.buyItem(id, qty, price);
        ctx.sleepUntil(GrandExchange::isReadyToCollect, 15000, 600);
        ctx.setCurrentItemCollected(false);
        ctx.setState(ScriptState.COLLECT_GE_ITEMS);
        return Calculations.random(600, 1200);
    }
}
