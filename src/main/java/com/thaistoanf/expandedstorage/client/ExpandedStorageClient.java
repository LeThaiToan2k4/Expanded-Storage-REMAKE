package com.thaistoanf.expandedstorage.client;

import com.thaistoanf.expandedstorage.core.ChestTier;
import com.thaistoanf.expandedstorage.core.ClientConfig;
import com.thaistoanf.expandedstorage.registry.ModBlockEntities;
import com.thaistoanf.expandedstorage.registry.ModScreenHandlers;
import com.thaistoanf.expandedstorage.screen.ExpandedChestScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

/** Client entrypoint registering the container screen and block entity renderers (3D item rendering uses 1.21.4+ special item models). */
@Environment(EnvType.CLIENT)
public class ExpandedStorageClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientConfig.get();
        MenuScreens.register(ModScreenHandlers.EXPANDED_CHEST, ExpandedChestScreen::new);

        for (ChestTier tier : ChestTier.values()) {
            BlockEntityRenderers.register(ModBlockEntities.get(tier), ExpandedChestBlockEntityRenderer::new);
        }
    }
}
