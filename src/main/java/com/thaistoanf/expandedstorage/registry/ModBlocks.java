package com.thaistoanf.expandedstorage.registry;

import com.thaistoanf.expandedstorage.block.ExpandedChestBlock;
import com.thaistoanf.expandedstorage.compat.IdCompat;
import com.thaistoanf.expandedstorage.core.ChestTier;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Registers all six tiered chests and their matching {@link BlockItem}s. */
public final class ModBlocks {
    private static final Map<ChestTier, ExpandedChestBlock> BLOCKS = new EnumMap<>(ChestTier.class);
    private static final Map<ChestTier, BlockItem> ITEMS = new EnumMap<>(ChestTier.class);

    public static final ExpandedChestBlock COPPER_CHEST = registerChest(ChestTier.COPPER, MapColor.ORANGE,
            BlockSoundGroup.COPPER);
    public static final ExpandedChestBlock IRON_CHEST = registerChest(ChestTier.IRON, MapColor.IRON_GRAY,
            BlockSoundGroup.METAL);
    public static final ExpandedChestBlock GOLD_CHEST = registerChest(ChestTier.GOLD, MapColor.GOLD,
            BlockSoundGroup.METAL);
    public static final ExpandedChestBlock DIAMOND_CHEST = registerChest(ChestTier.DIAMOND, MapColor.DIAMOND_BLUE,
            BlockSoundGroup.METAL);
    public static final ExpandedChestBlock OBSIDIAN_CHEST = registerChest(ChestTier.OBSIDIAN, MapColor.BLACK,
            BlockSoundGroup.STONE);
    public static final ExpandedChestBlock NETHERITE_CHEST = registerChest(ChestTier.NETHERITE, MapColor.BLACK,
            BlockSoundGroup.NETHERITE);

    private ModBlocks() {
    }

    private static ExpandedChestBlock registerChest(ChestTier tier, MapColor mapColor, BlockSoundGroup sounds) {
        Identifier id = IdCompat.id(tier.blockId());
        AbstractBlock.Settings blockSettings = AbstractBlock.Settings.create()
                .mapColor(mapColor)
                .strength(tier.hardness(), tier.blastResistance())
                .sounds(sounds)
                .requiresTool()
                .nonOpaque();

        ExpandedChestBlock block = new ExpandedChestBlock(tier, blockSettings, () -> ModBlockEntities.get(tier));
        Registry.register(Registries.BLOCK, id, block);

        Item.Settings itemSettings = new Item.Settings();
        if (tier.fireproof()) {
            itemSettings.fireproof();
        }
        BlockItem blockItem = new BlockItem(block, itemSettings);
        Registry.register(Registries.ITEM, id, blockItem);

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
