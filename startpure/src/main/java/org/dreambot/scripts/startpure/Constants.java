package org.dreambot.scripts.startpure;

import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;

public final class Constants {

    private Constants() {}

    // World
    public static final int TRADE_WORLD = 308;

    // Trade partner
    public static final String TRADE_PARTNER = "ridiculous a";

    // Weapon IDs
    public static final int IRON_SCIMITAR = 1323;
    public static final int MITHRIL_SCIMITAR = 1329;
    public static final int ADAMANT_SCIMITAR = 1331;
    public static final int RUNE_SCIMITAR = 1333;

    // Armour IDs
    public static final int IRON_FULL_HELM = 1153;
    public static final int IRON_PLATEBODY = 1115;
    public static final int IRON_PLATELEGS = 1067;
    public static final int IRON_KITESHIELD = 1191;

    // Other items
    public static final int SALMON = 329;
    public static final int COINS = 995;

    // Areas
    public static final Area GE_AREA = new Area(3158, 3479, 3172, 3497);
    public static final Tile GE_CENTER = new Tile(3165, 3487, 0);
    public static final Area CHICKEN_AREA = new Area(3225, 3295, 3236, 3300);
    public static final Tile CHICKEN_CENTER = new Tile(3231, 3298, 0);
    public static final Area COW_AREA = new Area(3253, 3255, 3265, 3296);
    public static final Tile COW_CENTER = new Tile(3258, 3276, 0);

    // Lumbridge bank area (top floor)
    public static final Area LUMBRIDGE_BANK_AREA = new Area(3207, 3217, 3210, 3220, 2);

    // Discord
    public static final String DISCORD_WEBHOOK_URL = "https://discord.com/api/webhooks/1471237574537384139/sKyQEN8uF79osMNVs76nnf_n3Uc1UpdmgoxgYuGRJqugmLoQBPhCa2yUy0yIRFTHvvbB";

    // Config
    public static final int EAT_HP_PERCENT = 50;
    public static final int SALMON_WITHDRAW_AMOUNT = 20;
    public static final int SALMON_BUY_QUANTITY = 100;
    public static final int TARGET_ATTACK = 40;
    public static final int TARGET_STRENGTH = 40;
    public static final int GOLD_THRESHOLD = 100000;
    public static final int TRADE_FAILSAFE_MAX = 10;
}
