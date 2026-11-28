package com.thaistoanf.expandedstorage.registry;

import com.thaistoanf.expandedstorage.compat.IdCompat;
import com.thaistoanf.expandedstorage.core.ChestTier;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** Registers the dedicated Expanded Storage creative mode tab. */
public final class ModItemGroups {
    public static final CreativeModeTab MAIN = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            IdCompat.id("main"),
            FabricCreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.expandedstorage.main"))
                    .icon(() -> new ItemStack(ModBlocks.NETHERITE_CHEST))
                    .displayItems((displayContext, entries) -> {
                        for (ChestTier tier : ChestTier.values()) {
                            entries.accept(ModBlocks.get(tier));
                        }
                    })
                    .build()
    );

    private ModItemGroups() {
    }

    public static void register() {
        // Static initializer registers the creative item group.
    }
}
