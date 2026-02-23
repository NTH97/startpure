package org.dreambot.scripts.startpure;

import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.scripts.startpure.tasks.*;

import java.util.EnumMap;
import java.util.Map;

@ScriptManifest(
        name = "Start Pure",
        description = "Automated pure account starter - trains 40 Attack and 40 Strength",
        author = "Developer",
        version = 1.1,
        category = Category.COMBAT,
        image = ""
)
public class Main extends AbstractScript {

    private ScriptContext ctx;
    private Map<ScriptState, ScriptTask> tasks;

    @Override
    public void onStart() {
        int[][] buyList = {
                {Constants.IRON_SCIMITAR, 1, 5000},
                {Constants.MITHRIL_SCIMITAR, 1, 5000},
                {Constants.ADAMANT_SCIMITAR, 1, 10000},
                {Constants.RUNE_SCIMITAR, 1, 30000},
                {Constants.IRON_FULL_HELM, 1, 5000},
                {Constants.IRON_PLATEBODY, 1, 10000},
                {Constants.IRON_PLATELEGS, 1, 5000},
                {Constants.IRON_KITESHIELD, 1, 5000},
                {Constants.SALMON, Constants.SALMON_BUY_QUANTITY, 200}
        };

        ctx = new ScriptContext(this, ScriptState.HOP_TO_TRADE_WORLD, buyList);

        tasks = new EnumMap<>(ScriptState.class);
        tasks.put(ScriptState.HOP_TO_TRADE_WORLD, new HopToTradeWorldTask(ctx));
        tasks.put(ScriptState.WALK_TO_GE, new WalkToGeTask(ctx));
        tasks.put(ScriptState.FIND_AND_TRADE, new FindAndTradeTask(ctx));
        tasks.put(ScriptState.ACCEPT_TRADE, new AcceptTradeTask(ctx));
        tasks.put(ScriptState.WAIT_FOR_GOLD, new WaitForGoldTask(ctx));
        tasks.put(ScriptState.BUY_GE_ITEMS, new BuyGeItemsTask(ctx));
        tasks.put(ScriptState.COLLECT_GE_ITEMS, new CollectGeItemsTask(ctx));
        tasks.put(ScriptState.EQUIP_GEAR, new EquipGearTask(ctx));
        tasks.put(ScriptState.WALK_TO_TRAINING, new WalkToTrainingTask(ctx));
        tasks.put(ScriptState.FIGHT, new FightTask(ctx));
        tasks.put(ScriptState.NOTIFY_DISCORD, new NotifyDiscordTask(ctx));

        log("Start Pure script started. State: " + ctx.getState());
    }

    @Override
    public int onLoop() {
        if (ctx.getState() == ScriptState.FINISHED) {
            log("Script complete! 40 Attack and 40 Strength achieved.");
            stop();
            return 600;
        }

        ScriptTask task = tasks.get(ctx.getState());
        if (task != null) {
            return task.execute();
        }

        return 600;
    }
}
