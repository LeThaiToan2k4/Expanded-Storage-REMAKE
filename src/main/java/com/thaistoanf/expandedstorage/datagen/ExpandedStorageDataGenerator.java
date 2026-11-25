package com.thaistoanf.expandedstorage.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/** Fabric DataGen entrypoint generating recipes, loot tables, and mining tags. */
public class ExpandedStorageDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ModRecipeProvider::new);
        pack.addProvider(ModLootTableProvider::new);
        pack.addProvider(ModBlockTagProvider::new);
    }
}
