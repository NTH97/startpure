package org.dreambot.scripts.startpure.tasks;

import org.dreambot.api.ClientSettings;
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
        if (!ctx.isSettingsConfigured()) {
            configureSettings();
            ctx.setSettingsConfigured(true);
            return Calculations.random(600, 1200);
        }

        if (Constants.GE_AREA.contains(Players.getLocal())) {
            ctx.setState(ScriptState.FIND_AND_TRADE);
            return 600;
        }
        Walking.walk(Constants.GE_CENTER);
        return Calculations.random(600, 1800);
    }

    private void configureSettings() {
        ctx.log("[Settings] Configuring game settings...");

        if (ClientSettings.isLevelUpInterfaceEnabled()) {
            ClientSettings.toggleLevelUpInterface(false);
            ctx.log("[Settings] Disabled level-up interface.");
        } else {
            ctx.log("[Settings] Level-up interface already disabled.");
        }

        if (ClientSettings.isTradeDelayEnabled()) {
            ClientSettings.toggleTradeDelay(false);
            ctx.log("[Settings] Disabled trade accept delay.");
        } else {
            ctx.log("[Settings] Trade accept delay already disabled.");
        }

        if (ClientSettings.isWorldHopConfirmationEnabled()) {
            ClientSettings.toggleWorldHopConfirmation(false);
            ctx.log("[Settings] Disabled world hop confirmation.");
        } else {
            ctx.log("[Settings] World hop confirmation already disabled.");
        }

        ctx.log("[Settings] Game settings configured.");
    }
}
