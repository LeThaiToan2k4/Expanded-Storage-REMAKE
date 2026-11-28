package com.thaistoanf.expandedstorage.client;

import com.thaistoanf.expandedstorage.block.entity.ExpandedChestBlockEntity;
import com.thaistoanf.expandedstorage.compat.RenderCompat;
import com.thaistoanf.expandedstorage.core.ChestTier;
import com.thaistoanf.expandedstorage.registry.ModBlocks;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.LightmapCoordinatesRetriever;
import net.minecraft.client.render.block.entity.model.ChestBlockModel;
import net.minecraft.client.render.block.entity.state.ChestBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Renders single and double tiered chests in Minecraft 1.21.9+ using
 * {@link ChestBlockModel},
 * {@link OrderedRenderCommandQueue}, and sprites batched inside
 * {@code minecraft:textures/atlas/chest.png}.
 */
@Environment(EnvType.CLIENT)
public class ExpandedChestBlockEntityRenderer
        implements BlockEntityRenderer<ExpandedChestBlockEntity, ExpandedChestBlockEntityRenderer.RenderState> {
    private final SpriteHolder materials;
    private final ChestBlockModel singleChest;
    private final ChestBlockModel doubleChestLeft;
    private final ChestBlockModel doubleChestRight;

    public static class RenderState extends ChestBlockEntityRenderState {
        public ChestTier tier = ChestTier.COPPER;
    }

    public ExpandedChestBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.materials = ctx.spriteHolder();
        this.singleChest = new ChestBlockModel(ctx.getLayerModelPart(EntityModelLayers.CHEST));
        this.doubleChestLeft = new ChestBlockModel(ctx.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_LEFT));
        this.doubleChestRight = new ChestBlockModel(ctx.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_RIGHT));
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void updateRenderState(
            ExpandedChestBlockEntity entity,
            RenderState state,
            float tickProgress,
            Vec3d cameraPos,
            ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickProgress, cameraPos, crumblingOverlay);
        state.tier = entity.getTier();

        World world = entity.getWorld();
        boolean inWorld = world != null;
        BlockState blockState = inWorld
                ? entity.getCachedState()
                : ModBlocks.get(entity.getTier()).getDefaultState().with(ChestBlock.FACING, Direction.SOUTH);

        state.chestType = blockState.contains(ChestBlock.CHEST_TYPE) ? blockState.get(ChestBlock.CHEST_TYPE)
                : ChestType.SINGLE;
        state.yaw = blockState.get(ChestBlock.FACING).getPositiveHorizontalDegrees();

        DoubleBlockProperties.PropertySource<? extends ChestBlockEntity> source = (world != null
                && blockState.getBlock() instanceof ChestBlock chestBlock)
                        ? chestBlock.getBlockEntitySource(blockState, world, entity.getPos(), true)
                        : DoubleBlockProperties.PropertyRetriever::getFallback;

        Float2FloatFunction progressFn = source.apply(ChestBlock.getAnimationProgressRetriever(entity));
        state.lidAnimationProgress = progressFn.get(tickProgress);

        if (state.chestType != ChestType.SINGLE) {
            Int2IntFunction lightFn = source.apply(new LightmapCoordinatesRetriever<>());
            state.lightmapCoordinates = lightFn.applyAsInt(state.lightmapCoordinates);
        }
    }

    @Override
    public void render(
            RenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState cameraState) {
        matrices.push();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-state.yaw));
        matrices.translate(-0.5f, -0.5f, -0.5f);

        float openFactor = 1.0f - state.lidAnimationProgress;
        openFactor = 1.0f - openFactor * openFactor * openFactor;

        SpriteIdentifier spriteId = RenderCompat.getChestSprite(state.tier, state.chestType);
        ChestBlockModel model = switch (state.chestType) {
            case LEFT -> this.doubleChestLeft;
            case RIGHT -> this.doubleChestRight;
            default -> this.singleChest;
        };

        RenderCompat.submitChestModel(
                queue,
                model,
                openFactor,
                matrices,
                this.materials,
                spriteId,
                state.lightmapCoordinates,
                state.crumblingOverlay);

        matrices.pop();
    }
}
