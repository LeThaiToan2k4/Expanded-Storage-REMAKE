package com.thaistoanf.expandedstorage.datagen;

import com.thaistoanf.expandedstorage.compat.IdCompat;
import com.thaistoanf.expandedstorage.core.ChestTier;
import com.thaistoanf.expandedstorage.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

/** Generates shaped crafting and smithing transform recipes for all six chest tiers. */
public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(
            FabricDataOutput output,
            CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture
    ) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        // 1. Copper Chest: vanilla chest surrounded by 8 copper ingots
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.COPPER_CHEST)
                .pattern("MMM")
                .pattern("MCM")
                .pattern("MMM")
                .input('M', Items.COPPER_INGOT)
                .input('C', Items.CHEST)
                .criterion(hasItem(Items.CHEST), conditionsFromItem(Items.CHEST))
                .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                .offerTo(exporter, IdCompat.id("copper_chest"));

        // 2. Iron Chest: copper chest surrounded by 8 iron ingots
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.IRON_CHEST)
                .pattern("MMM")
                .pattern("MCM")
                .pattern("MMM")
                .input('M', Items.IRON_INGOT)
                .input('C', ModBlocks.COPPER_CHEST)
                .criterion(hasItem(ModBlocks.COPPER_CHEST), conditionsFromItem(ModBlocks.COPPER_CHEST))
                .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                .offerTo(exporter, IdCompat.id("iron_chest"));

        // 3. Gold Chest: iron chest surrounded by 8 gold ingots
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.GOLD_CHEST)
                .pattern("MMM")
                .pattern("MCM")
                .pattern("MMM")
                .input('M', Items.GOLD_INGOT)
                .input('C', ModBlocks.IRON_CHEST)
                .criterion(hasItem(ModBlocks.IRON_CHEST), conditionsFromItem(ModBlocks.IRON_CHEST))
                .criterion(hasItem(Items.GOLD_INGOT), conditionsFromItem(Items.GOLD_INGOT))
                .offerTo(exporter, IdCompat.id("gold_chest"));

        // 4. Diamond Chest (budget recipe): gold chest center, 2 diamonds left/right, 6 glass outer slots
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.DIAMOND_CHEST)
                .pattern("GGG")
                .pattern("DCD")
                .pattern("GGG")
                .input('G', Items.GLASS)
                .input('D', Items.DIAMOND)
                .input('C', ModBlocks.GOLD_CHEST)
                .criterion(hasItem(ModBlocks.GOLD_CHEST), conditionsFromItem(ModBlocks.GOLD_CHEST))
                .criterion(hasItem(Items.DIAMOND), conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter, IdCompat.id("diamond_chest"));

        // 5. Obsidian Chest: diamond chest surrounded by 8 obsidian
        ShapedRecipeJsonBuilder.create(RecipeCategory.DECORATIONS, ModBlocks.OBSIDIAN_CHEST)
                .pattern("OOO")
                .pattern("OCO")
                .pattern("OOO")
                .input('O', Items.OBSIDIAN)
                .input('C', ModBlocks.DIAMOND_CHEST)
                .criterion(hasItem(ModBlocks.DIAMOND_CHEST), conditionsFromItem(ModBlocks.DIAMOND_CHEST))
                .criterion(hasItem(Items.OBSIDIAN), conditionsFromItem(Items.OBSIDIAN))
                .offerTo(exporter, IdCompat.id("obsidian_chest"));

        // 6A. Netherite Chest (Smithing Path A: Diamond Chest + Netherite Ingot)
        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.ofItems(ModBlocks.DIAMOND_CHEST),
                        Ingredient.ofItems(Items.NETHERITE_INGOT),
                        RecipeCategory.DECORATIONS,
                        ModBlocks.getItem(ChestTier.NETHERITE)
                )
                .criterion(hasItem(ModBlocks.DIAMOND_CHEST), conditionsFromItem(ModBlocks.DIAMOND_CHEST))
                .criterion(hasItem(Items.NETHERITE_INGOT), conditionsFromItem(Items.NETHERITE_INGOT))
                .offerTo(exporter, IdCompat.id("netherite_chest_from_diamond_chest_smithing"));

        // 6B. Netherite Chest (Smithing Path B: Obsidian Chest + Netherite Ingot)
        SmithingTransformRecipeJsonBuilder.create(
                        Ingredient.ofItems(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                        Ingredient.ofItems(ModBlocks.OBSIDIAN_CHEST),
                        Ingredient.ofItems(Items.NETHERITE_INGOT),
                        RecipeCategory.DECORATIONS,
                        ModBlocks.getItem(ChestTier.NETHERITE)
                )
                .criterion(hasItem(ModBlocks.OBSIDIAN_CHEST), conditionsFromItem(ModBlocks.OBSIDIAN_CHEST))
                .criterion(hasItem(Items.NETHERITE_INGOT), conditionsFromItem(Items.NETHERITE_INGOT))
                .offerTo(exporter, IdCompat.id("netherite_chest_from_obsidian_chest_smithing"));
    }
}
