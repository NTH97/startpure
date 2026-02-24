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

    private final double fatigueRate;          // base ~0.007, randomized 0.005 – 0.009
    private final double pauseFocusRecovery;   // base ~0.15,  randomized 0.10 – 0.20
    private final double attentionDrift;       // base ~0.08,  randomized 0.05 – 0.11

    // Randomized base probabilities per account
    private final double misclickBase;         // base ~0.002, randomized 0.001 – 0.004
    private final double pauseBase;            // base ~0.001, randomized 0.0005 – 0.002
    private final double doubleClickBase;      // base ~0.02,  randomized 0.01 – 0.035
    private final double changeOfMindBase;     // base ~0.005, randomized 0.002 – 0.008
    private final double cameraIdleBase;       // base ~0.03,  randomized 0.015 – 0.045
    private final double repetitiveBase;       // base ~0.01,  randomized 0.005 – 0.02

    // Randomized failure delay thresholds per account
    private final double failFrustrationChance; // chance of quick frustrated re-click
    private final int failFrustrationLow;       // min ms for frustrated click
    private final int failFrustrationHigh;      // max ms for frustrated click
    private final int failNormalLow;            // min ms for normal retry
    private final int failNormalHigh;           // max ms for normal retry
    private final int failConfusedLow;          // min ms for confused wait
    private final int failConfusedHigh;         // max ms for confused wait

    // Fatigue wave parameters for 24h sessions
    private final double fatigueCyclePeriod;    // minutes per fatigue wave cycle
    private final double fatigueWaveAmplitude;  // how much fatigue oscillates
    private final double fatigueFloor;          // minimum fatigue after initial ramp

    public HumanBehavior() {
        this.startTime = System.currentTimeMillis();
        this.lastPauseTime = startTime;
        this.focusLevel = 0.85 + Math.random() * 0.15;          // 0.85 – 1.00
        this.fatigue = Math.random() * 0.15;                     // 0.00 – 0.15
        this.attention = 0.80 + Math.random() * 0.20;            // 0.80 – 1.00
        this.fatigueRate = 0.005 + Math.random() * 0.004;        // 0.005 – 0.009
        this.pauseFocusRecovery = 0.10 + Math.random() * 0.10;   // 0.10 – 0.20
        this.attentionDrift = 0.05 + Math.random() * 0.06;       // 0.05 – 0.11
        this.misclickBase = 0.001 + Math.random() * 0.003;       // 0.001 – 0.004
        this.pauseBase = 0.0005 + Math.random() * 0.0015;        // 0.0005 – 0.002
        this.doubleClickBase = 0.01 + Math.random() * 0.025;     // 0.01 – 0.035
        this.changeOfMindBase = 0.002 + Math.random() * 0.006;   // 0.002 – 0.008
        this.cameraIdleBase = 0.015 + Math.random() * 0.030;     // 0.015 – 0.045
        this.repetitiveBase = 0.005 + Math.random() * 0.015;     // 0.005 – 0.02

        // Failure delay profile — varies per account
        this.failFrustrationChance = 0.20 + Math.random() * 0.25;  // 0.20 – 0.45
        this.failFrustrationLow = 50 + (int)(Math.random() * 80);  // 50 – 130
        this.failFrustrationHigh = 250 + (int)(Math.random() * 200); // 250 – 450
        this.failNormalLow = 300 + (int)(Math.random() * 200);     // 300 – 500
        this.failNormalHigh = 700 + (int)(Math.random() * 400);    // 700 – 1100
        this.failConfusedLow = 900 + (int)(Math.random() * 500);   // 900 – 1400
        this.failConfusedHigh = 1800 + (int)(Math.random() * 1200); // 1800 – 3000

        // Fatigue waves — simulates energy cycles over a long session
        this.fatigueCyclePeriod = 60.0 + Math.random() * 90.0;     // 60 – 150 min per cycle
        this.fatigueWaveAmplitude = 0.05 + Math.random() * 0.10;   // 0.05 – 0.15
        this.fatigueFloor = 0.05 + Math.random() * 0.10;           // 0.05 – 0.15
    }

    /**
     * Call once per loop iteration. Updates fatigue, focus, and attention.
     */
    public void tick() {
        double minutesRunning = (System.currentTimeMillis() - startTime) / 60000.0;
        double minutesSincePause = (System.currentTimeMillis() - lastPauseTime) / 60000.0;

        // Fatigue uses waves to simulate energy cycles over 24h sessions.
        // Ramps up slowly over ~2-3h, then oscillates around a low baseline.
        double ramp = Math.min(1.0, minutesRunning * fatigueRate * 3.0); // ramps to 1.0 in ~2-3h
        double wave = Math.sin(2.0 * Math.PI * minutesRunning / fatigueCyclePeriod);
        double baseFatigue = fatigueFloor + ramp * (0.30 - fatigueFloor);
        fatigue = clamp(baseFatigue + wave * fatigueWaveAmplitude, 0.0, 0.60);

        // Focus degrades with fatigue + time since last pause
        double baseFocus = 1.0 - fatigue * 0.6 - Math.min(minutesSincePause * 0.005, 0.15);
        focusLevel = clamp(baseFocus + (Math.random() - 0.5) * 0.03, 0.25, 0.98);

        // Attention drifts randomly — can spike or drop
        double drift = (Math.random() - 0.48) * attentionDrift;
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
        // Thresholds shift with fatigue — when tired, more confused delays
        double frustratedThreshold = failFrustrationChance * (1.0 - fatigue * 0.3);
        double normalThreshold = frustratedThreshold + 0.35;
        if (roll < frustratedThreshold) {
            // Quick frustrated re-click
            return Calculations.random(failFrustrationLow, failFrustrationHigh);
        } else if (roll < normalThreshold) {
            // Normal retry
            return Calculations.random(failNormalLow, failNormalHigh);
        } else {
            // Confused, takes a moment
            return Calculations.random(failConfusedLow, failConfusedHigh);
        }
    }

    // ──────────────── Behavior checks ────────────────

    /**
     * Should we take a random AFK pause? More likely when fatigued.
     */
    public boolean shouldPause() {
        // At start: ~0.1%, after 1hr: ~0.8%
        double chance = pauseBase + fatigue * 0.008 + (1.0 - attention) * 0.003;
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
        focusLevel = clamp(focusLevel + pauseFocusRecovery, 0.25, 0.98);
        attention = clamp(attention + 0.1, 0.2, 1.0);

        return duration;
    }

    /**
     * Should we misclick? Very rare at start, increases with fatigue.
     */
    public boolean shouldMisclick() {
        // At start: ~0.2%, after 1hr: ~5%
        double chance = misclickBase + (1.0 - focusLevel) * 0.06 + fatigue * 0.03;
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
        return Math.random() < doubleClickBase + fatigue * 0.04;
    }

    /**
     * Should we "change our mind" — start an action then abort it?
     * For example: hover an NPC, then walk away or click another one.
     */
    public boolean shouldChangeOfMind() {
        // Very rare: ~0.5% at start, ~2% when fatigued and unfocused
        double chance = changeOfMindBase + (1.0 - attention) * 0.01 + fatigue * 0.01;
        return Math.random() < chance;
    }

    /**
     * Should we randomly move the camera? Humans idle-move camera.
     */
    public boolean shouldMoveCameraIdle() {
        return Math.random() < cameraIdleBase + (1.0 - attention) * 0.04;
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
        return Math.random() < repetitiveBase + fatigue * 0.02;
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
