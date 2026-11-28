package com.thaistoanf.expandedstorage.registry;

import com.thaistoanf.expandedstorage.block.ExpandedChestBlock;
import com.thaistoanf.expandedstorage.compat.IdCompat;
import com.thaistoanf.expandedstorage.core.ChestTier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Registers all six tiered chests and their matching {@link BlockItem}s. */
public final class ModBlocks {
    private static final Map<ChestTier, ExpandedChestBlock> BLOCKS = new EnumMap<>(ChestTier.class);
    private static final Map<ChestTier, BlockItem> ITEMS = new EnumMap<>(ChestTier.class);

    public static final ExpandedChestBlock COPPER_CHEST = registerChest(ChestTier.COPPER, MapColor.COLOR_ORANGE,
            SoundType.COPPER);
    public static final ExpandedChestBlock IRON_CHEST = registerChest(ChestTier.IRON, MapColor.METAL,
            SoundType.METAL);
    public static final ExpandedChestBlock GOLD_CHEST = registerChest(ChestTier.GOLD, MapColor.GOLD,
            SoundType.METAL);
    public static final ExpandedChestBlock DIAMOND_CHEST = registerChest(ChestTier.DIAMOND, MapColor.DIAMOND,
            SoundType.METAL);
    public static final ExpandedChestBlock OBSIDIAN_CHEST = registerChest(ChestTier.OBSIDIAN, MapColor.COLOR_BLACK,
            SoundType.STONE);
    public static final ExpandedChestBlock NETHERITE_CHEST = registerChest(ChestTier.NETHERITE, MapColor.COLOR_BLACK,
            SoundType.NETHERITE_BLOCK);

    private ModBlocks() {
    }

    private static ExpandedChestBlock registerChest(ChestTier tier, MapColor mapColor, SoundType sounds) {
        Identifier id = IdCompat.id(tier.blockId());
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

        BlockBehaviour.Properties blockSettings = BlockBehaviour.Properties.of()
                .setId(blockKey)
                .mapColor(mapColor)
                .strength(tier.hardness(), tier.blastResistance())
                .sound(sounds)
                .requiresCorrectToolForDrops()
                .noOcclusion();

        ExpandedChestBlock block = new ExpandedChestBlock(tier, blockSettings, () -> ModBlockEntities.get(tier));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        Item.Properties itemSettings = new Item.Properties()
                .setId(itemKey)
                .useBlockDescriptionPrefix();
        if (tier.fireproof()) {
            itemSettings.fireResistant();
        }
        BlockItem blockItem = new BlockItem(block, itemSettings);
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

        BLOCKS.put(tier, block);
        ITEMS.put(tier, blockItem);
        return block;
    }

    public static ExpandedChestBlock get(ChestTier tier) {
        return BLOCKS.get(tier);
    }

    public static Item getItem(ChestTier tier) {
        return ITEMS.get(tier);
    }

    public static Collection<ExpandedChestBlock> allBlocks() {
        return Collections.unmodifiableCollection(BLOCKS.values());
    }

    public static void register() {
        // Static initializers perform registration in deterministic enum order.
    }
}
