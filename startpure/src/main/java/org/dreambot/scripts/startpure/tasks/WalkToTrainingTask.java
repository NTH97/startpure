package org.dreambot.scripts.startpure.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.scripts.startpure.ScriptContext;
import org.dreambot.scripts.startpure.ScriptState;
import org.dreambot.scripts.startpure.ScriptTask;
import org.dreambot.scripts.startpure.TrainingLocation;

public class WalkToTrainingTask implements ScriptTask {

    private final ScriptContext ctx;

    public WalkToTrainingTask(ScriptContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int execute() {
        int atk = Skills.getRealLevel(Skill.ATTACK);
        int str = Skills.getRealLevel(Skill.STRENGTH);
        TrainingLocation location = TrainingLocation.getForLevels(atk, str);

        if (location.getArea().contains(Players.getLocal())) {
            ctx.setState(ScriptState.FIGHT);
            return 600;
        }

        Walking.walk(location.getCenterTile());
        return Calculations.random(600, 1800);
    }
}
