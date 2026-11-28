package com.thaistoanf.expandedstorage.datagen;

import com.thaistoanf.expandedstorage.compat.IdCompat;
import com.thaistoanf.expandedstorage.core.ChestTier;
import com.thaistoanf.expandedstorage.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.concurrent.CompletableFuture;

/**
 * Generates shaped crafting and smithing transform recipes for all six chest
 * tiers.
 */
public class ModRecipeProvider extends FabricRecipeProvider {
        public ModRecipeProvider(
                        FabricPackOutput output,
                        CompletableFuture<HolderLookup.Provider> registriesFuture) {
                super(output, registriesFuture);
        }

        private static ResourceKey<Recipe<?>> recipeKey(String path) {
                return ResourceKey.create(Registries.RECIPE, IdCompat.id(path));
        }

        @Override
        protected RecipeProvider createRecipeProvider(
                        HolderLookup.Provider registryLookup,
                        RecipeOutput exporter) {
                return new RecipeProvider(registryLookup, exporter) {
                        @Override
                        public void buildRecipes() {
                                // 1. Expanded Copper Chest: vanilla chest surrounded by 7 copper ingots and 1
                                // copper block at slot 8 (bottom-center)
                                // Avoids conflicting with Vanilla 1.21.9+ minecraft:copper_chest (8 copper
                                // ingots + vanilla chest).
                                shaped(RecipeCategory.DECORATIONS, ModBlocks.COPPER_CHEST)
                                                .pattern("III")
                                                .pattern("ICI")
                                                .pattern("IBI")
                                                .define('I', Items.COPPER_INGOT)
                                                .define('B', Items.COPPER_BLOCK)
                                                .define('C', Items.CHEST)
                                                .unlockedBy(getHasName(Items.CHEST), has(Items.CHEST))
                                                .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                                                .unlockedBy(getHasName(Items.COPPER_BLOCK), has(Items.COPPER_BLOCK))
                                                .save(this.output, recipeKey("copper_chest"));

                                // 2. Iron Chest: expanded copper chest strictly surrounded by 8 iron ingots
                                // (never accepts vanilla copper chests)
                                shaped(RecipeCategory.DECORATIONS, ModBlocks.IRON_CHEST)
                                                .pattern("III")
                                                .pattern("ICI")
                                                .pattern("III")
                                                .define('I', Items.IRON_INGOT)
                                                .define('C', ModBlocks.COPPER_CHEST)
                                                .unlockedBy(getHasName(ModBlocks.COPPER_CHEST),
                                                                has(ModBlocks.COPPER_CHEST))
                                                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                                                .save(this.output, recipeKey("iron_chest"));

                                // 3. Gold Chest: iron chest surrounded by 8 gold ingots
                                shaped(RecipeCategory.DECORATIONS, ModBlocks.GOLD_CHEST)
                                                .pattern("MMM")
                                                .pattern("MCM")
                                                .pattern("MMM")
                                                .define('M', Items.GOLD_INGOT)
                                                .define('C', ModBlocks.IRON_CHEST)
                                                .unlockedBy(getHasName(ModBlocks.IRON_CHEST), has(ModBlocks.IRON_CHEST))
                                                .unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                                                .save(this.output, recipeKey("gold_chest"));

                                // 4. Diamond Chest (budget recipe): gold chest center, 2 diamonds left/right, 6
                                // glass outer slots
                                shaped(RecipeCategory.DECORATIONS, ModBlocks.DIAMOND_CHEST)
                                                .pattern("GGG")
                                                .pattern("DCD")
                                                .pattern("GGG")
                                                .define('G', Items.GLASS)
                                                .define('D', Items.DIAMOND)
                                                .define('C', ModBlocks.GOLD_CHEST)
                                                .unlockedBy(getHasName(ModBlocks.GOLD_CHEST), has(ModBlocks.GOLD_CHEST))
                                                .unlockedBy(getHasName(Items.DIAMOND), has(Items.DIAMOND))
                                                .save(this.output, recipeKey("diamond_chest"));

                                // 5. Obsidian Chest: diamond chest surrounded by 8 obsidian
                                shaped(RecipeCategory.DECORATIONS, ModBlocks.OBSIDIAN_CHEST)
                                                .pattern("OOO")
                                                .pattern("OCO")
                                                .pattern("OOO")
                                                .define('O', Items.OBSIDIAN)
                                                .define('C', ModBlocks.DIAMOND_CHEST)
                                                .unlockedBy(getHasName(ModBlocks.DIAMOND_CHEST),
                                                                has(ModBlocks.DIAMOND_CHEST))
                                                .unlockedBy(getHasName(Items.OBSIDIAN), has(Items.OBSIDIAN))
                                                .save(this.output, recipeKey("obsidian_chest"));

                                // 6A. Netherite Chest (Smithing Path A: Diamond Chest + Netherite Ingot)
                                SmithingTransformRecipeBuilder.smithing(
                                                Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                                                Ingredient.of(ModBlocks.DIAMOND_CHEST),
                                                Ingredient.of(Items.NETHERITE_INGOT),
                                                RecipeCategory.DECORATIONS,
                                                ModBlocks.getItem(ChestTier.NETHERITE))
                                                .unlocks(getHasName(ModBlocks.DIAMOND_CHEST),
                                                                has(ModBlocks.DIAMOND_CHEST))
                                                .unlocks(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                                                .save(this.output, recipeKey(
                                                                "netherite_chest_from_diamond_chest_smithing"));

                                // 6B. Netherite Chest (Smithing Path B: Obsidian Chest + Netherite Ingot)
                                SmithingTransformRecipeBuilder.smithing(
                                                Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                                                Ingredient.of(ModBlocks.OBSIDIAN_CHEST),
                                                Ingredient.of(Items.NETHERITE_INGOT),
                                                RecipeCategory.DECORATIONS,
                                                ModBlocks.getItem(ChestTier.NETHERITE))
                                                .unlocks(getHasName(ModBlocks.OBSIDIAN_CHEST),
                                                                has(ModBlocks.OBSIDIAN_CHEST))
                                                .unlocks(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                                                .save(this.output, recipeKey(
                                                                "netherite_chest_from_obsidian_chest_smithing"));
                        }
                };
        }

        @Override
        public String getName() {
                return "Expanded Storage Recipes";
        }
}
