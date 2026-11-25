package com.thaistoanf.expandedstorage.core;

import java.util.Locale;

/** The three switchable chest GUI layouts. Version-agnostic. */
public enum GuiMode {
    /** Fits every slot on one screen with a tier-specific grid (default). */
    ADAPTIVE("adaptive"),
    /** Double chests are drawn as two wings separated by a divider. */
    SPLIT_WING("split_wing"),
    /** Large inventories are split into clickable pages docked on top. */
    SMART_TABBED("smart_tabbed");

    private static final GuiMode[] VALUES = values();

    private final String key;

    GuiMode(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public String translationKey() {
        return "gui.expandedstorage.mode." + key;
    }

    public GuiMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    public static GuiMode parse(String value) {
        if (value != null) {
            String normalized = value.trim().toUpperCase(Locale.ROOT);
            for (GuiMode mode : VALUES) {
                if (mode.name().equals(normalized) || mode.key.equalsIgnoreCase(value.trim())) {
                    return mode;
                }
            }
        }
        return ADAPTIVE;
    }
}
