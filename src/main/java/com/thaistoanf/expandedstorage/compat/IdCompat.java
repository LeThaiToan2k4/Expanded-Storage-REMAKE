package com.thaistoanf.expandedstorage.compat;

import net.minecraft.resources.Identifier;

/**
 * Isolates {@link Identifier} construction across Minecraft versions.
 */
public final class IdCompat {
    public static final String MOD_ID = "expandedstorage";

    private IdCompat() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static Identifier mc(String path) {
        return Identifier.withDefaultNamespace(path);
    }
}
