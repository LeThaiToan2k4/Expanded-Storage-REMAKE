package com.thaistoanf.expandedstorage.core;

import java.util.Locale;

/**
 * Single source of truth for every chest tier.
 *
 * <p>This enum is intentionally free of Minecraft classes so it survives version ports unchanged.
 * Minecraft specific mappings (map colors, sounds, tags) live in the registry and datagen layers.</p>
 */
public enum ChestTier {
    //       id           single  hard   resist   fire   mine  adaptive single  adaptive double  tabbed single   tabbed double
    COPPER("copper", 36, 3.0f, 6.0f, false, 1, grid(9, 4), grid(12, 6), grid(9, 4), grid(9, 4)),
    IRON("iron", 54, 5.0f, 6.0f, false, 1, grid(9, 6), grid(12, 9), grid(9, 6), grid(9, 6)),
    GOLD("gold", 81, 3.0f, 6.0f, false, 2, grid(9, 9), grid(18, 9), grid(9, 9), grid(9, 9)),
    DIAMOND("diamond", 108, 5.0f, 6.0f, false, 2, grid(12, 9), grid(24, 9), grid(9, 6), grid(12, 9)),
    OBSIDIAN("obsidian", 108, 10.0f, 1200.0f, false, 3, grid(12, 9), grid(24, 9), grid(9, 6), grid(12, 9)),
    NETHERITE("netherite", 135, 12.0f, 1200.0f, true, 3, grid(15, 9), grid(27, 10), grid(9, 5), grid(10, 9));

    /** Mining levels used by the tag provider: 0 = any, 1 = stone, 2 = iron, 3 = diamond. */
    public static final int MINING_ANY = 0;
    public static final int MINING_STONE = 1;
    public static final int MINING_IRON = 2;
    public static final int MINING_DIAMOND = 3;

    private static final ChestTier[] VALUES = values();

    private final String id;
    private final int singleSlots;
    private final float hardness;
    private final float blastResistance;
    private final boolean fireproof;
    private final int miningLevel;
    private final Grid adaptiveSingle;
    private final Grid adaptiveDouble;
    private final Grid tabbedSingle;
    private final Grid tabbedDouble;

    ChestTier(String id, int singleSlots, float hardness, float blastResistance, boolean fireproof, int miningLevel,
              Grid adaptiveSingle, Grid adaptiveDouble, Grid tabbedSingle, Grid tabbedDouble) {
        this.id = id;
        this.singleSlots = singleSlots;
        this.hardness = hardness;
        this.blastResistance = blastResistance;
        this.fireproof = fireproof;
        this.miningLevel = miningLevel;
        this.adaptiveSingle = adaptiveSingle;
        this.adaptiveDouble = adaptiveDouble;
        this.tabbedSingle = tabbedSingle;
        this.tabbedDouble = tabbedDouble;
    }

    private static Grid grid(int cols, int rows) {
        return new Grid(cols, rows);
    }

    /** Material id, e.g. {@code copper}. */
    public String id() {
        return id;
    }

    /** Registry path of the block and item, e.g. {@code copper_chest}. */
    public String blockId() {
        return id + "_chest";
    }

    public int singleSlots() {
        return singleSlots;
    }

    public int doubleSlots() {
        return singleSlots * 2;
    }

    public int slots(boolean isDouble) {
        return isDouble ? doubleSlots() : singleSlots;
    }

    public float hardness() {
        return hardness;
    }

    public float blastResistance() {
        return blastResistance;
    }

    public boolean fireproof() {
        return fireproof;
    }

    public int miningLevel() {
        return miningLevel;
    }

    /** Grid used by {@link GuiMode#ADAPTIVE}. */
    public Grid adaptive(boolean isDouble) {
        return isDouble ? adaptiveDouble : adaptiveSingle;
    }

    /** Grid of a single wing in {@link GuiMode#SPLIT_WING}; each wing mirrors a single chest. */
    public Grid splitWing() {
        return adaptiveSingle;
    }

    /** Preferred page grid used by {@link GuiMode#SMART_TABBED}. */
    public Grid tabbed(boolean isDouble) {
        return isDouble ? tabbedDouble : tabbedSingle;
    }

    public static ChestTier byIndex(int index) {
        return index >= 0 && index < VALUES.length ? VALUES[index] : COPPER;
    }

    public static ChestTier byId(String id) {
        String key = id == null ? "" : id.toLowerCase(Locale.ROOT);
        for (ChestTier tier : VALUES) {
            if (tier.id.equals(key)) {
                return tier;
            }
        }
        return COPPER;
    }

    /** Immutable column x row pair. */
    public record Grid(int cols, int rows) {
        public int capacity() {
            return cols * rows;
        }
    }
}
