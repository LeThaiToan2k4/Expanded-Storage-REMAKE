package com.thaistoanf.expandedstorage.registry;

import com.thaistoanf.expandedstorage.block.entity.ExpandedChestBlockEntity;
import com.thaistoanf.expandedstorage.compat.IdCompat;
import com.thaistoanf.expandedstorage.core.ChestTier;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.EnumMap;
import java.util.Map;

/** Registers a dedicated {@link BlockEntityType} per chest tier for strict double-chest pairing. */
public final class ModBlockEntities {
    private static final Map<ChestTier, BlockEntityType<ExpandedChestBlockEntity>> TYPES = new EnumMap<>(ChestTier.class);

    static {
        for (ChestTier tier : ChestTier.values()) {
            BlockEntityType<ExpandedChestBlockEntity> type = Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    IdCompat.id(tier.blockId()),
                    FabricBlockEntityTypeBuilder.create(
                            (pos, state) -> new ExpandedChestBlockEntity(tier, pos, state),
                            ModBlocks.get(tier)
                    ).build()
            );
            TYPES.put(tier, type);
        }
    }

    private ModBlockEntities() {
    }

    public static BlockEntityType<ExpandedChestBlockEntity> get(ChestTier tier) {
        return TYPES.get(tier);
    }

    public static void register() {
        // Static initializer registers all block entity types after ModBlocks.
    }
}
