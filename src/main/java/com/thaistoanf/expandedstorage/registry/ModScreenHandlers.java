package com.thaistoanf.expandedstorage.registry;

import com.thaistoanf.expandedstorage.compat.IdCompat;
import com.thaistoanf.expandedstorage.compat.NetworkCompat;
import com.thaistoanf.expandedstorage.screen.ExpandedChestScreenHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

/** Registers the shared {@link ExpandedChestScreenHandler} screen handler type. */
public final class ModScreenHandlers {
    public static final MenuType<ExpandedChestScreenHandler> EXPANDED_CHEST = Registry.register(
            BuiltInRegistries.MENU,
            IdCompat.id("expanded_chest"),
            NetworkCompat.createScreenHandlerType(ExpandedChestScreenHandler::new)
    );

    private ModScreenHandlers() {
    }

    public static void register() {
        // Static initializer registers the screen handler type.
    }
}
