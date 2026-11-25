package com.thaistoanf.expandedstorage.compat;

import net.minecraft.util.Identifier;

/**
 * Isolates {@link Identifier} construction so upgrading from 1.20.1 ({@code new Identifier(...)})
 * to 1.21+ ({@code Identifier.of(...)}) is a one-line edit.
 */
public final class IdCompat {
    public static final String MOD_ID = "expandedstorage";

    private IdCompat() {
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static Identifier mc(String path) {
        return new Identifier("minecraft", path);
    }
}
