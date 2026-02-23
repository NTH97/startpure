package org.dreambot.scripts.startpure;

public enum WeaponProgression {
    IRON(1, Constants.IRON_SCIMITAR, "Iron scimitar"),
    MITHRIL(20, Constants.MITHRIL_SCIMITAR, "Mithril scimitar"),
    ADAMANT(30, Constants.ADAMANT_SCIMITAR, "Adamant scimitar"),
    RUNE(40, Constants.RUNE_SCIMITAR, "Rune scimitar");

    private final int requiredAttackLevel;
    private final int itemId;
    private final String itemName;

    WeaponProgression(int requiredAttackLevel, int itemId, String itemName) {
        this.requiredAttackLevel = requiredAttackLevel;
        this.itemId = itemId;
        this.itemName = itemName;
    }

    public int getRequiredAttackLevel() {
        return requiredAttackLevel;
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public static WeaponProgression getBestForLevel(int attackLevel) {
        WeaponProgression best = IRON;
        for (WeaponProgression wp : values()) {
            if (attackLevel >= wp.requiredAttackLevel) {
                best = wp;
            }
        }
        return best;
    }

    /**
     * Returns the best weapon based on randomized XP thresholds.
     * Since the thresholds are always >= the level XP, the player
     * is guaranteed to have the required level when the threshold is met.
     */
    public static WeaponProgression getBestForXp(int atkXp, int xp20, int xp30, int xp40) {
        if (atkXp >= xp40) return RUNE;
        if (atkXp >= xp30) return ADAMANT;
        if (atkXp >= xp20) return MITHRIL;
        return IRON;
    }
}
