package org.dreambot.scripts.startpure;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
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

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int x = 10;
        int y = 290;
        int w = 220;
        int h = 120;

        // Background
        g2.setColor(new Color(20, 20, 30, 200));
        g2.fillRoundRect(x, y, w, h, 12, 12);

        // Accent line at top
        g2.setColor(new Color(220, 60, 60));
        g2.fillRoundRect(x, y, w, 4, 12, 12);

        // Border
        g2.setColor(new Color(255, 255, 255, 40));
        g2.drawRoundRect(x, y, w, h, 12, 12);

        int px = x + 12;
        int py = y + 22;

        // Title + runtime on same line
        long elapsed = System.currentTimeMillis() - startTime;
        long seconds = (elapsed / 1000) % 60;
        long minutes = (elapsed / 60000) % 60;
        long hours = elapsed / 3600000;
        String runtime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        g2.setFont(new Font("Arial", Font.BOLD, 13));
        g2.setColor(Color.WHITE);
        g2.drawString("Start Pure", px, py);

        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.setColor(new Color(160, 160, 170));
        g2.drawString(runtime, px + w - 80, py);

        // Separator
        py += 8;
        g2.setColor(new Color(255, 255, 255, 30));
        g2.drawLine(px, py, px + w - 24, py);

        // Skill stats
        int atk = Skills.getRealLevel(Skill.ATTACK);
        int str = Skills.getRealLevel(Skill.STRENGTH);
        int hp = Skills.getRealLevel(Skill.HITPOINTS);
        int hpCurrent = Skills.getBoostedLevel(Skill.HITPOINTS);

        py += 16;
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        drawSkillStat(g2, px, py, new Color(220, 60, 60), "\u2694", "Atk", atk);
        drawSkillStat(g2, px + 68, py, new Color(60, 180, 60), "\u270A", "Str", str);
        drawSkillStat(g2, px + 136, py, new Color(230, 70, 70), "\u2764", "HP", hpCurrent + "/" + hp);

        // Current task
        py += 24;
        g2.setColor(new Color(120, 120, 130));
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString("TASK", px, py);

        py += 14;
        g2.setColor(new Color(100, 180, 255));
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString(ctx.getState().getLabel(), px, py);
    }

    private void drawSkillStat(Graphics2D g2, int x, int y, Color iconColor, String icon, String label, Object value) {
        // Icon
        g2.setColor(iconColor);
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        g2.drawString(icon, x, y);

        // Label + value
        g2.setColor(new Color(180, 180, 190));
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.drawString(label + ": ", x + 15, y);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString(String.valueOf(value), x + 15 + g2.getFontMetrics(new Font("Arial", Font.PLAIN, 11)).stringWidth(label + ": "), y);
    }
}
