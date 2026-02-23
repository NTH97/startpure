package org.dreambot.scripts.startpure.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.scripts.startpure.Constants;
import org.dreambot.scripts.startpure.ScriptContext;
import org.dreambot.scripts.startpure.ScriptState;
import org.dreambot.scripts.startpure.ScriptTask;

public class WalkToGeTask implements ScriptTask {

    private final ScriptContext ctx;

    public WalkToGeTask(ScriptContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int execute() {
        if (Constants.GE_AREA.contains(Players.getLocal())) {
            ctx.setState(ScriptState.FIND_AND_TRADE);
            return 600;
        }
        Walking.walk(Constants.GE_CENTER);
        return Calculations.random(600, 1800);
    }
}
