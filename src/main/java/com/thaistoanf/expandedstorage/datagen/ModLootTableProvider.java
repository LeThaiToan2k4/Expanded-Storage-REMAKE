package com.thaistoanf.expandedstorage.datagen;

import com.thaistoanf.expandedstorage.core.ChestTier;
import com.thaistoanf.expandedstorage.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;

/** Generates standard nameable container drops for all six chest blocks. */
public class ModLootTableProvider extends FabricBlockLootTableProvider {
    public ModLootTableProvider(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void generate() {
        for (ChestTier tier : ChestTier.values()) {
            Block block = ModBlocks.get(tier);
            addDrop(block, this::nameableContainerDrops);
        }
    }
}
