package com.thaistoanf.expandedstorage.registry;

import com.thaistoanf.expandedstorage.compat.IdCompat;
import com.thaistoanf.expandedstorage.core.ChestTier;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

/** Registers the dedicated Expanded Storage creative mode tab. */
public final class ModItemGroups {
    public static final ItemGroup MAIN = Registry.register(
            Registries.ITEM_GROUP,
            IdCompat.id("main"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.expandedstorage.main"))
                    .icon(() -> new ItemStack(ModBlocks.NETHERITE_CHEST))
                    .entries((displayContext, entries) -> {
                        for (ChestTier tier : ChestTier.values()) {
                            entries.add(ModBlocks.get(tier));
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
