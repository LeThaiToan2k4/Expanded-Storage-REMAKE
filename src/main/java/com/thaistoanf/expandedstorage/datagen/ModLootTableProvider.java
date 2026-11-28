package com.thaistoanf.expandedstorage.datagen;

import com.thaistoanf.expandedstorage.core.ChestTier;
import com.thaistoanf.expandedstorage.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

/** Generates standard nameable container drops for all six chest blocks. */
public class ModLootTableProvider extends FabricBlockLootSubProvider {
    public ModLootTableProvider(
            FabricPackOutput dataOutput,
            CompletableFuture<HolderLookup.Provider> registryLookup
    ) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        for (ChestTier tier : ChestTier.values()) {
            Block block = ModBlocks.get(tier);
            add(block, this::createNameableBlockEntityTable);
        }
    }
}
