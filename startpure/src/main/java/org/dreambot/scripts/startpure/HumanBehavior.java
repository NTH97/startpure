package org.dreambot.scripts.startpure;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.NPC;

/**
 * Simulates human-like behavior with degrading focus, increasing fatigue,
 * and fluctuating attention. At script start the player is nearly tick-perfect;
 * over time mistakes, pauses and inconsistencies become more frequent.
 */
public class HumanBehavior {

    private double focusLevel;   // 1.0 = laser-focused, 0.0 = completely unfocused
    private double fatigue;      // 0.0 = fresh, 1.0 = exhausted
    private double attention;    // 1.0 = fully attentive, fluctuates randomly

    private final long startTime;
    private long lastPauseTime;

    // How many minutes until fatigue reaches ~0.5
    private static final double FATIGUE_RATE = 0.007;   // ~0.42 after 60 min
    // Focus recovery when a pause happens
    private static final double PAUSE_FOCUS_RECOVERY = 0.15;
    // Attention drift per tick
    private static final double ATTENTION_DRIFT = 0.08;

    public HumanBehavior() {
        this.startTime = System.currentTimeMillis();
        this.lastPauseTime = startTime;
        this.focusLevel = 0.97;
        this.fatigue = 0.0;
        this.attention = 0.95;
    }

    /**
     * Call once per loop iteration. Updates fatigue, focus, and attention.
     */
    public void tick() {
        double minutesRunning = (System.currentTimeMillis() - startTime) / 60000.0;
        double minutesSincePause = (System.currentTimeMillis() - lastPauseTime) / 60000.0;

        // Fatigue increases over the whole session, slowly
        fatigue = Math.min(0.85, minutesRunning * FATIGUE_RATE);

        // Focus degrades with fatigue + time since last pause
        double baseFocus = 1.0 - fatigue * 0.6 - Math.min(minutesSincePause * 0.005, 0.15);
        focusLevel = clamp(baseFocus + (Math.random() - 0.5) * 0.03, 0.25, 0.98);

        // Attention drifts randomly — can spike or drop
        double drift = (Math.random() - 0.48) * ATTENTION_DRIFT;
        attention = clamp(attention + drift, 0.2, 1.0);

        // Rare attention "reset" — simulates player refocusing
        if (Math.random() < 0.005) {
            attention = clamp(0.85 + Math.random() * 0.15, 0.85, 1.0);
        }
    }

    // ──────────────── Delay modifiers ────────────────

    /**
     * Adjusts a base delay range based on current state.
     * High focus = near base. Low focus = wider variance.
     * Sometimes returns very fast (impatient click) or slow (distracted).
     */
    public int modifySleep(int baseLow, int baseHigh) {
        int base = Calculations.random(baseLow, baseHigh);

        // Impatient burst — fast click, more likely when attention is high
        if (attention > 0.8 && focusLevel > 0.7 && Math.random() < 0.12) {
            return (int) (base * Calculations.random(40, 70) / 100.0);
        }

        // Distracted — slow response, more likely when fatigued
        if (attention < 0.4 && Math.random() < 0.15 + fatigue * 0.15) {
            return (int) (base * Calculations.random(180, 300) / 100.0);
        }

        // Normal variance based on focus
        double multiplier = 1.0 + (1.0 - focusLevel) * Calculations.random(20, 80) / 100.0;
        return (int) (base * multiplier);
    }

    /**
     * Delay after something fails. Humans sometimes click fast on the mistake
     * (frustration) and sometimes wait longer (confusion).
     */
    public int failureDelay() {
        double roll = Math.random();
        if (roll < 0.35) {
            // Quick frustrated re-click
            return Calculations.random(80, 350);
        } else if (roll < 0.7) {
            // Normal retry
            return Calculations.random(400, 900);
        } else {
            // Confused, takes a moment
            return Calculations.random(1200, 2500);
        }
    }

    // ──────────────── Behavior checks ────────────────

    /**
     * Should we take a random AFK pause? More likely when fatigued.
     */
    public boolean shouldPause() {
        // At start: ~0.1%, after 1hr: ~0.8%
        double chance = 0.001 + fatigue * 0.008 + (1.0 - attention) * 0.003;
        return Math.random() < chance;
    }

    /**
     * Returns pause duration. Short micro-pauses are common,
     * longer AFKs are rare.
     */
    public int getPauseDuration() {
        double roll = Math.random();
        int duration;
        if (roll < 0.50) {
            // Micro-pause: hesitation
            duration = Calculations.random(1500, 4000);
        } else if (roll < 0.80) {
            // Short pause: checking phone, reading chat
            duration = Calculations.random(4000, 12000);
        } else if (roll < 0.95) {
            // Medium AFK: getting drink, tabbed out
            duration = Calculations.random(12000, 35000);
        } else {
            // Long AFK: bathroom, grabbed food
            duration = Calculations.random(35000, 90000);
        }

        // After pause, recover some focus
        lastPauseTime = System.currentTimeMillis();
        focusLevel = clamp(focusLevel + PAUSE_FOCUS_RECOVERY, 0.25, 0.98);
        attention = clamp(attention + 0.1, 0.2, 1.0);

        return duration;
    }

    /**
     * Should we misclick? Very rare at start, increases with fatigue.
     */
    public boolean shouldMisclick() {
        // At start: ~0.2%, after 1hr: ~5%
        double chance = 0.002 + (1.0 - focusLevel) * 0.06 + fatigue * 0.03;
        return Math.random() < chance;
    }

    /**
     * Perform a misclick — walk to a random tile near the NPC.
     */
    public void doMisclick(NPC target) {
        if (target == null) return;
        Tile npcTile = target.getTile();
        if (npcTile == null) return;
        int dx = Calculations.random(-2, 2);
        int dz = Calculations.random(-2, 2);
        if (dx == 0 && dz == 0) dx = 1;
        Walking.walkOnScreen(new Tile(npcTile.getX() + dx, npcTile.getY() + dz, npcTile.getZ()));
    }

    /**
     * Should we click the same thing twice? Simulates impatient double-click.
     */
    public boolean shouldDoubleClick() {
        // ~2% at start, up to ~6% when fatigued
        return Math.random() < 0.02 + fatigue * 0.04;
    }

    /**
     * Should we "change our mind" — start an action then abort it?
     * For example: hover an NPC, then walk away or click another one.
     */
    public boolean shouldChangeOfMind() {
        // Very rare: ~0.5% at start, ~2% when fatigued and unfocused
        double chance = 0.005 + (1.0 - attention) * 0.01 + fatigue * 0.01;
        return Math.random() < chance;
    }

    /**
     * Should we randomly move the camera? Humans idle-move camera.
     */
    public boolean shouldMoveCameraIdle() {
        return Math.random() < 0.03 + (1.0 - attention) * 0.04;
    }

    /**
     * Rotate camera randomly to simulate idle looking around.
     */
    public void doIdleCameraMove() {
        int currentYaw = Camera.getYaw();
        int newYaw = currentYaw + Calculations.random(-80, 80);
        int currentPitch = Camera.getPitch();
        int newPitch = clampInt(currentPitch + Calculations.random(-30, 30), 128, 383);
        Camera.rotateTo(newYaw, newPitch);
    }

    /**
     * Should we do a repetitive action (e.g. check inventory, open/close tab)?
     */
    public boolean shouldRepetitiveAction() {
        return Math.random() < 0.01 + fatigue * 0.02;
    }

    // ──────────────── Getters ────────────────

    public double getFocusLevel() {
        return focusLevel;
    }

    public double getFatigue() {
        return fatigue;
    }

    public double getAttention() {
        return attention;
    }

    // ──────────────── Utility ────────────────

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
