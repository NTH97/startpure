package org.dreambot.scripts.startpure.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.scripts.startpure.Constants;
import org.dreambot.scripts.startpure.ScriptContext;
import org.dreambot.scripts.startpure.ScriptState;
import org.dreambot.scripts.startpure.ScriptTask;

public class HopToTradeWorldTask implements ScriptTask {

    private final ScriptContext ctx;

    public HopToTradeWorldTask(ScriptContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int execute() {
        if (Worlds.getCurrentWorld() == Constants.TRADE_WORLD) {
            ctx.setState(ScriptState.WALK_TO_GE);
            return 600;
        }
        ctx.log("Hopping to world " + Constants.TRADE_WORLD);
        WorldHopper.hopWorld(Constants.TRADE_WORLD);
        ctx.sleepUntil(() -> Worlds.getCurrentWorld() == Constants.TRADE_WORLD, 10000, 600);
        return Calculations.random(600, 1200);
    }
}
