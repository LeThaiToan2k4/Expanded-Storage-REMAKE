package com.thaistoanf.expandedstorage.client;

import com.thaistoanf.expandedstorage.block.entity.ExpandedChestBlockEntity;
import com.thaistoanf.expandedstorage.core.ChestTier;
import com.thaistoanf.expandedstorage.core.ClientConfig;
import com.thaistoanf.expandedstorage.registry.ModBlockEntities;
import com.thaistoanf.expandedstorage.registry.ModBlocks;
import com.thaistoanf.expandedstorage.registry.ModScreenHandlers;
import com.thaistoanf.expandedstorage.screen.ExpandedChestScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.block.ChestBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.EnumMap;
import java.util.Map;

/** Client entrypoint registering the container screen, block entity renderers, and 3D item renderers. */
@Environment(EnvType.CLIENT)
public class ExpandedStorageClient implements ClientModInitializer {
    private static final Map<ChestTier, ExpandedChestBlockEntity> ITEM_RENDER_ENTITIES = new EnumMap<>(ChestTier.class);

    @Override
    public void onInitializeClient() {
        ClientConfig.get();
        HandledScreens.register(ModScreenHandlers.EXPANDED_CHEST, ExpandedChestScreen::new);

        for (ChestTier tier : ChestTier.values()) {
            BlockEntityRendererFactories.register(ModBlockEntities.get(tier), ExpandedChestBlockEntityRenderer::new);

            ExpandedChestBlockEntity renderEntity = new ExpandedChestBlockEntity(
                    tier,
                    BlockPos.ORIGIN,
                    ModBlocks.get(tier).getDefaultState().with(ChestBlock.FACING, Direction.SOUTH)
            );
            ITEM_RENDER_ENTITIES.put(tier, renderEntity);

            BuiltinItemRendererRegistry.INSTANCE.register(
                    ModBlocks.getItem(tier),
                    (stack, mode, matrices, vertexConsumers, light, overlay) ->
                            MinecraftClient.getInstance().getBlockEntityRenderDispatcher().renderEntity(
                                    ITEM_RENDER_ENTITIES.get(tier), matrices, vertexConsumers, light, overlay
                            )
            );
        }
    }
}
