package org.dreambot.scripts.startpure.tasks;

import org.dreambot.api.methods.Calculations;
import org.dreambot.scripts.startpure.Constants;
import org.dreambot.scripts.startpure.DiscordNotifier;
import org.dreambot.scripts.startpure.ScriptContext;
import org.dreambot.scripts.startpure.ScriptState;
import org.dreambot.scripts.startpure.ScriptTask;

public class NotifyDiscordTask implements ScriptTask {

    private final ScriptContext ctx;

    public NotifyDiscordTask(ScriptContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public int execute() {
        String message = "ACCOUNT: " + Constants.ACCOUNT_CREDENTIALS;
        boolean success = DiscordNotifier.sendNotification(Constants.DISCORD_WEBHOOK_URL, message);
        if (success) {
            ctx.log("Discord notification sent successfully.");
            ctx.setState(ScriptState.FINISHED);
        } else {
            ctx.setNotifyRetries(ctx.getNotifyRetries() + 1);
            if (ctx.getNotifyRetries() >= 3) {
                ctx.log("Failed to send Discord notification after 3 attempts. Finishing anyway.");
                ctx.setState(ScriptState.FINISHED);
            } else {
                ctx.log("Discord notification failed. Retrying...");
            }
        }
        return Calculations.random(1000, 2000);
    }
}
