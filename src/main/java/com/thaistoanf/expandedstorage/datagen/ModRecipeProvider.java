package com.thaistoanf.expandedstorage.datagen;

import com.thaistoanf.expandedstorage.compat.IdCompat;
import com.thaistoanf.expandedstorage.core.ChestTier;
import com.thaistoanf.expandedstorage.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.SmithingTransformRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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

    private static RegistryKey<Recipe<?>> recipeKey(String path) {
        return RegistryKey.of(RegistryKeys.RECIPE, IdCompat.id(path));
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(
            RegistryWrapper.WrapperLookup registryLookup,
            RecipeExporter exporter
    ) {
        return new RecipeGenerator(registryLookup, exporter) {
            @Override
            public void generate() {
                // 1. Expanded Copper Chest: vanilla chest surrounded by 7 copper ingots and 1 copper block at slot 8 (bottom-center)
                // Avoids conflicting with Vanilla 1.21.9+ minecraft:copper_chest (8 copper ingots + vanilla chest).
                createShaped(RecipeCategory.DECORATIONS, ModBlocks.COPPER_CHEST)
                        .pattern("III")
                        .pattern("ICI")
                        .pattern("IBI")
                        .input('I', Items.COPPER_INGOT)
                        .input('B', Items.COPPER_BLOCK)
                        .input('C', Items.CHEST)
                        .criterion(hasItem(Items.CHEST), conditionsFromItem(Items.CHEST))
                        .criterion(hasItem(Items.COPPER_INGOT), conditionsFromItem(Items.COPPER_INGOT))
                        .criterion(hasItem(Items.COPPER_BLOCK), conditionsFromItem(Items.COPPER_BLOCK))
                        .offerTo(this.exporter, recipeKey("copper_chest"));

                // 2. Iron Chest: expanded copper chest strictly surrounded by 8 iron ingots (never accepts vanilla copper chests)
                createShaped(RecipeCategory.DECORATIONS, ModBlocks.IRON_CHEST)
                        .pattern("III")
                        .pattern("ICI")
                        .pattern("III")
                        .input('I', Items.IRON_INGOT)
                        .input('C', ModBlocks.COPPER_CHEST)
                        .criterion(hasItem(ModBlocks.COPPER_CHEST), conditionsFromItem(ModBlocks.COPPER_CHEST))
                        .criterion(hasItem(Items.IRON_INGOT), conditionsFromItem(Items.IRON_INGOT))
                        .offerTo(this.exporter, recipeKey("iron_chest"));

                // 3. Gold Chest: iron chest surrounded by 8 gold ingots
                createShaped(RecipeCategory.DECORATIONS, ModBlocks.GOLD_CHEST)
                        .pattern("MMM")
                        .pattern("MCM")
                        .pattern("MMM")
                        .input('M', Items.GOLD_INGOT)
                        .input('C', ModBlocks.IRON_CHEST)
                        .criterion(hasItem(ModBlocks.IRON_CHEST), conditionsFromItem(ModBlocks.IRON_CHEST))
                        .criterion(hasItem(Items.GOLD_INGOT), conditionsFromItem(Items.GOLD_INGOT))
                        .offerTo(this.exporter, recipeKey("gold_chest"));

                // 4. Diamond Chest (budget recipe): gold chest center, 2 diamonds left/right, 6 glass outer slots
                createShaped(RecipeCategory.DECORATIONS, ModBlocks.DIAMOND_CHEST)
                        .pattern("GGG")
                        .pattern("DCD")
                        .pattern("GGG")
                        .input('G', Items.GLASS)
                        .input('D', Items.DIAMOND)
                        .input('C', ModBlocks.GOLD_CHEST)
                        .criterion(hasItem(ModBlocks.GOLD_CHEST), conditionsFromItem(ModBlocks.GOLD_CHEST))
                        .criterion(hasItem(Items.DIAMOND), conditionsFromItem(Items.DIAMOND))
                        .offerTo(this.exporter, recipeKey("diamond_chest"));

                // 5. Obsidian Chest: diamond chest surrounded by 8 obsidian
                createShaped(RecipeCategory.DECORATIONS, ModBlocks.OBSIDIAN_CHEST)
                        .pattern("OOO")
                        .pattern("OCO")
                        .pattern("OOO")
                        .input('O', Items.OBSIDIAN)
                        .input('C', ModBlocks.DIAMOND_CHEST)
                        .criterion(hasItem(ModBlocks.DIAMOND_CHEST), conditionsFromItem(ModBlocks.DIAMOND_CHEST))
                        .criterion(hasItem(Items.OBSIDIAN), conditionsFromItem(Items.OBSIDIAN))
                        .offerTo(this.exporter, recipeKey("obsidian_chest"));

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
                        .offerTo(this.exporter, recipeKey("netherite_chest_from_diamond_chest_smithing"));

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
                        .offerTo(this.exporter, recipeKey("netherite_chest_from_obsidian_chest_smithing"));
            }
        };
    }

    @Override
    public String getName() {
        return "Expanded Storage Recipes";
    }
}
