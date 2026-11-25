package com.thaistoanf.expandedstorage;

import com.thaistoanf.expandedstorage.compat.IdCompat;
import com.thaistoanf.expandedstorage.registry.ModBlockEntities;
import com.thaistoanf.expandedstorage.registry.ModBlocks;
import com.thaistoanf.expandedstorage.registry.ModItemGroups;
import com.thaistoanf.expandedstorage.registry.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Main common entrypoint for Expanded Storage. */
public class ExpandedStorage implements ModInitializer {
    public static final String MOD_ID = IdCompat.MOD_ID;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModBlockEntities.register();
        ModScreenHandlers.register();
        ModItemGroups.register();
        LOGGER.info("Expanded Storage initialized (6 chest tiers registered).");
    }
}
