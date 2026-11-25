package com.thaistoanf.expandedstorage.registry;

import com.thaistoanf.expandedstorage.compat.IdCompat;
import com.thaistoanf.expandedstorage.compat.NetworkCompat;
import com.thaistoanf.expandedstorage.screen.ExpandedChestScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;

/** Registers the shared {@link ExpandedChestScreenHandler} screen handler type. */
public final class ModScreenHandlers {
    public static final ScreenHandlerType<ExpandedChestScreenHandler> EXPANDED_CHEST = Registry.register(
            Registries.SCREEN_HANDLER,
            IdCompat.id("expanded_chest"),
            NetworkCompat.createScreenHandlerType(ExpandedChestScreenHandler::new)
    );

    private ModScreenHandlers() {
    }

    public static void register() {
        // Static initializer registers the screen handler type.
    }
}
