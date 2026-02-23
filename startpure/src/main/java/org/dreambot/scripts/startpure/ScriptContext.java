package org.dreambot.scripts.startpure;

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
}
