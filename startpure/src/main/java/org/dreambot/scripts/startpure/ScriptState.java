package org.dreambot.scripts.startpure;

public enum ScriptState {
    HOP_TO_TRADE_WORLD("Hop to trade world"),
    WALK_TO_GE("Walk to GE"),
    FIND_AND_TRADE("Find and trade partner"),
    ACCEPT_TRADE("Accept trade"),
    WAIT_FOR_GOLD("Wait for gold"),
    BUY_GE_ITEMS("Buy GE items"),
    COLLECT_GE_ITEMS("Collect GE items"),
    EQUIP_GEAR("Equip gear"),
    WALK_TO_TRAINING("Walk to training area"),
    FIGHT("Fight — train combat"),
    NOTIFY_DISCORD("Notify Discord"),
    FINISHED("Finished");

    private final String label;

    ScriptState(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
