package org.dreambot.scripts.startpure;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.script.AbstractScript;

import org.dreambot.api.utilities.impl.Condition;

public class ScriptContext {

    private final AbstractScript script;
    private final int[][] buyList;

    private ScriptState state;
    private int buyIndex;
    private boolean currentItemCollected;
    private int tradeFailsafe;
    private int notifyRetries;
    private int lastAttackLevel;
    private int lastStrengthLevel;
    private final HumanBehavior humanBehavior;

    // Randomized XP targets for each milestone
    private final int targetAtkXp20;
    private final int targetAtkXp30;
    private final int targetAtkXp40;
    private final int targetStrXp20;
    private final int targetStrXp30;
    private final int targetStrXp40;
    private final int targetCowsXp;

    public ScriptContext(AbstractScript script, ScriptState initialState, int[][] buyList) {
        this.script = script;
        this.state = initialState;
        this.buyList = buyList;
        this.buyIndex = 0;
        this.currentItemCollected = true;
        this.tradeFailsafe = 0;
        this.notifyRetries = 0;
        this.lastAttackLevel = -1;
        this.lastStrengthLevel = -1;
        this.humanBehavior = new HumanBehavior();

        // Generate randomized XP targets once
        this.targetAtkXp20 = Constants.XP_LEVEL_20 + randomXpOffset();
        this.targetAtkXp30 = Constants.XP_LEVEL_30 + randomXpOffset();
        this.targetAtkXp40 = Constants.XP_LEVEL_40 + randomXpOffset();
        this.targetStrXp20 = Constants.XP_LEVEL_20 + randomXpOffset();
        this.targetStrXp30 = Constants.XP_LEVEL_30 + randomXpOffset();
        this.targetStrXp40 = Constants.XP_LEVEL_40 + randomXpOffset();
        this.targetCowsXp = Constants.XP_LEVEL_30 + randomXpOffset();

        script.log("XP targets — Atk: " + targetAtkXp20 + "/" + targetAtkXp30 + "/" + targetAtkXp40
                + " | Str: " + targetStrXp20 + "/" + targetStrXp30 + "/" + targetStrXp40
                + " | Cows at: " + targetCowsXp);
    }

    private int randomXpOffset() {
        return Calculations.random(Constants.XP_RANDOM_MIN, Constants.XP_RANDOM_MAX);
    }

    public AbstractScript getScript() {
        return script;
    }

    public void log(String message) {
        script.log(message);
    }

    public void stop() {
        script.stop();
    }

    public void sleepUntil(Condition condition, long timeout, int pollInterval) {
        script.sleepUntil(condition, timeout, pollInterval);
    }

    public ScriptState getState() {
        return state;
    }

    public void setState(ScriptState state) {
        this.state = state;
    }

    public int[][] getBuyList() {
        return buyList;
    }

    public int getBuyIndex() {
        return buyIndex;
    }

    public void setBuyIndex(int buyIndex) {
        this.buyIndex = buyIndex;
    }

    public boolean isCurrentItemCollected() {
        return currentItemCollected;
    }

    public void setCurrentItemCollected(boolean currentItemCollected) {
        this.currentItemCollected = currentItemCollected;
    }

    public int getTradeFailsafe() {
        return tradeFailsafe;
    }

    public void setTradeFailsafe(int tradeFailsafe) {
        this.tradeFailsafe = tradeFailsafe;
    }

    public int getNotifyRetries() {
        return notifyRetries;
    }

    public void setNotifyRetries(int notifyRetries) {
        this.notifyRetries = notifyRetries;
    }

    public int getLastAttackLevel() {
        return lastAttackLevel;
    }

    public void setLastAttackLevel(int lastAttackLevel) {
        this.lastAttackLevel = lastAttackLevel;
    }

    public int getLastStrengthLevel() {
        return lastStrengthLevel;
    }

    public void setLastStrengthLevel(int lastStrengthLevel) {
        this.lastStrengthLevel = lastStrengthLevel;
    }

    public HumanBehavior getHumanBehavior() { return humanBehavior; }

    public int getTargetAtkXp20() { return targetAtkXp20; }
    public int getTargetAtkXp30() { return targetAtkXp30; }
    public int getTargetAtkXp40() { return targetAtkXp40; }
    public int getTargetStrXp20() { return targetStrXp20; }
    public int getTargetStrXp30() { return targetStrXp30; }
    public int getTargetStrXp40() { return targetStrXp40; }
    public int getTargetCowsXp() { return targetCowsXp; }
}
