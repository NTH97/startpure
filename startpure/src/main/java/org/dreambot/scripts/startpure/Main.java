package org.dreambot.scripts.startpure;

import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.scripts.startpure.tasks.*;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

@ScriptManifest(
        name = "Start Pure",
        description = "Automated pure account starter - trains 40 Attack and 40 Strength",
        author = "Developer",
        version = 1.4,
        category = Category.COMBAT,
        image = ""
)
public class Main extends AbstractScript {

    private ScriptContext ctx;
    private Map<ScriptState, ScriptTask> tasks;
    private long startTime;

    @Override
    public void onStart() {
        ScriptState[] selectableStates = Arrays.stream(ScriptState.values())
                .filter(s -> s != ScriptState.FINISHED)
                .toArray(ScriptState[]::new);

        JComboBox<ScriptState> combo = new JComboBox<>(selectableStates);
        combo.setSelectedItem(ScriptState.HOP_TO_TRADE_WORLD);

        int result = JOptionPane.showConfirmDialog(
                null, combo, "Start Pure — Select starting state",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            stop();
            return;
        }

        ScriptState startState = (ScriptState) combo.getSelectedItem();

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

        ctx = new ScriptContext(this, startState, buyList);

        tasks = new EnumMap<>(ScriptState.class);
        tasks.put(ScriptState.HOP_TO_TRADE_WORLD, new HopToTradeWorldTask(ctx));
        tasks.put(ScriptState.WALK_TO_GE, new WalkToGeTask(ctx));
        tasks.put(ScriptState.FIND_AND_TRADE, new FindAndTradeTask(ctx));
        tasks.put(ScriptState.ACCEPT_TRADE, new AcceptTradeTask(ctx));
        tasks.put(ScriptState.WAIT_FOR_GOLD, new WaitForGoldTask(ctx));
        tasks.put(ScriptState.BUY_GE_ITEMS, new BuyGeItemsTask(ctx));
        tasks.put(ScriptState.COLLECT_GE_ITEMS, new CollectGeItemsTask(ctx));
        tasks.put(ScriptState.DEPOSIT_INVENTORY, new DepositInventoryTask(ctx));
        tasks.put(ScriptState.WITHDRAW_GEAR, new WithdrawGearTask(ctx));
        tasks.put(ScriptState.EQUIP_GEAR, new EquipGearTask(ctx));
        tasks.put(ScriptState.WALK_TO_TRAINING, new WalkToTrainingTask(ctx));
        tasks.put(ScriptState.FIGHT, new FightTask(ctx));
        tasks.put(ScriptState.NOTIFY_DISCORD, new NotifyDiscordTask(ctx));

        startTime = System.currentTimeMillis();
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

    @Override
    public void onPaint(Graphics g) {
        if (ctx == null) return;

        long elapsed = System.currentTimeMillis() - startTime;
        long seconds = (elapsed / 1000) % 60;
        long minutes = (elapsed / 60000) % 60;
        long hours = elapsed / 3600000;
        String runtime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        int x = 10;
        int y = 340;

        Graphics2D g2 = (Graphics2D) g;

        // Background
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(x, y, 200, 60, 10, 10);

        // Border
        g2.setColor(new Color(255, 255, 255, 80));
        g2.drawRoundRect(x, y, 200, 60, 10, 10);

        // Title
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(Color.WHITE);
        g2.drawString("Start Pure", x + 10, y + 18);

        // Runtime
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(new Color(200, 200, 200));
        g2.drawString("Runtime: " + runtime, x + 10, y + 35);

        // Current task
        g2.drawString("Task: " + ctx.getState().getLabel(), x + 10, y + 52);
    }
}
