package org.dreambot.scripts.startpure;

import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;

public enum TrainingLocation {
    CHICKENS("Chicken", Constants.CHICKEN_AREA, Constants.CHICKEN_CENTER, 0, 0),
    COWS("Cow", Constants.COW_AREA, Constants.COW_CENTER, 30, 30);

    private final String npcName;
    private final Area area;
    private final Tile centerTile;
    private final int requiredAttack;
    private final int requiredStrength;

    TrainingLocation(String npcName, Area area, Tile centerTile, int requiredAttack, int requiredStrength) {
        this.npcName = npcName;
        this.area = area;
        this.centerTile = centerTile;
        this.requiredAttack = requiredAttack;
        this.requiredStrength = requiredStrength;
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

    public int getRequiredAttack() {
        return requiredAttack;
    }

    public int getRequiredStrength() {
        return requiredStrength;
    }

    public static TrainingLocation getForLevels(int attack, int strength) {
        if (attack >= COWS.requiredAttack && strength >= COWS.requiredStrength) {
            return COWS;
        }
        return CHICKENS;
    }
}
