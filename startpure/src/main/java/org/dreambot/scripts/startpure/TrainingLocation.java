package org.dreambot.scripts.startpure;

import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;

public enum TrainingLocation {
    CHICKENS("Chicken", Constants.CHICKEN_AREA, Constants.CHICKEN_CENTER),
    COWS("Cow", Constants.COW_AREA, Constants.COW_CENTER);

    private final String npcName;
    private final Area area;
    private final Tile centerTile;

    TrainingLocation(String npcName, Area area, Tile centerTile) {
        this.npcName = npcName;
        this.area = area;
        this.centerTile = centerTile;
    }

    public String getNpcName() {
        return npcName;
    }

    public Area getArea() {
        return area;
    }

    public Tile getCenterTile() {
        return centerTile;
    }

    public static TrainingLocation getForXp(int atkXp, int strXp, int cowsXpThreshold) {
        if (atkXp >= cowsXpThreshold && strXp >= cowsXpThreshold) {
            return COWS;
        }
        return CHICKENS;
    }
}
