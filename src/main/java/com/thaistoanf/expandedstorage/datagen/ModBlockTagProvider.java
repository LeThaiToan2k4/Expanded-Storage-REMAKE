package com.thaistoanf.expandedstorage.datagen;

import com.thaistoanf.expandedstorage.block.ExpandedChestBlock;
import com.thaistoanf.expandedstorage.core.ChestTier;
import com.thaistoanf.expandedstorage.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

/** Generates pickaxe mining tags, tool-tier tags, and piglin-guarded block tags. */
public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(
            FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture
    ) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        var pickaxe = valueLookupBuilder(BlockTags.PICKAXE_MINEABLE);
        var stone = valueLookupBuilder(BlockTags.NEEDS_STONE_TOOL);
        var iron = valueLookupBuilder(BlockTags.NEEDS_IRON_TOOL);
        var diamond = valueLookupBuilder(BlockTags.NEEDS_DIAMOND_TOOL);
        var piglins = valueLookupBuilder(BlockTags.GUARDED_BY_PIGLINS);

        for (ChestTier tier : ChestTier.values()) {
            ExpandedChestBlock block = ModBlocks.get(tier);
            pickaxe.add(block);
            piglins.add(block);
            switch (tier.miningLevel()) {
                case ChestTier.MINING_STONE -> stone.add(block);
                case ChestTier.MINING_IRON -> iron.add(block);
                case ChestTier.MINING_DIAMOND -> diamond.add(block);
                default -> {
                }
            }
        }
    }
}
