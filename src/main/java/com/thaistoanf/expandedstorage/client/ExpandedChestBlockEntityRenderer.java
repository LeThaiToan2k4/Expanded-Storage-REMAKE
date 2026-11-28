package com.thaistoanf.expandedstorage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaistoanf.expandedstorage.block.entity.ExpandedChestBlockEntity;
import com.thaistoanf.expandedstorage.compat.RenderCompat;
import com.thaistoanf.expandedstorage.core.ChestTier;
import com.thaistoanf.expandedstorage.registry.ModBlocks;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;

/**
 * Renders single and double tiered chests in Minecraft 26.1+ using {@link ChestModel},
 * {@link SubmitNodeCollector}, and sprites batched inside {@code minecraft:textures/atlas/chest.png}.
 */
@Environment(EnvType.CLIENT)
public class ExpandedChestBlockEntityRenderer
        implements BlockEntityRenderer<ExpandedChestBlockEntity, ExpandedChestBlockEntityRenderer.RenderState> {
    private final SpriteGetter materials;
    private final ChestModel singleChest;
    private final ChestModel doubleChestLeft;
    private final ChestModel doubleChestRight;

    public static class RenderState extends ChestRenderState {
        public ChestTier tier = ChestTier.COPPER;
    }

    public ExpandedChestBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.materials = ctx.sprites();
        this.singleChest = new ChestModel(ctx.bakeLayer(ModelLayers.CHEST));
        this.doubleChestLeft = new ChestModel(ctx.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT));
        this.doubleChestRight = new ChestModel(ctx.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT));
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            ExpandedChestBlockEntity entity,
            RenderState state,
            float tickProgress,
            Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderer.super.extractRenderState(entity, state, tickProgress, cameraPos, crumblingOverlay);
        state.tier = entity.getTier();

        Level world = entity.getLevel();
        boolean inWorld = world != null;
        BlockState blockState = inWorld
                ? entity.getBlockState()
                : ModBlocks.get(entity.getTier()).defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);

        state.type = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        state.facing = blockState.getValue(ChestBlock.FACING);

        DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> source = (world != null
                && blockState.getBlock() instanceof ChestBlock chestBlock)
                        ? chestBlock.combine(blockState, world, entity.getBlockPos(), true)
                        : DoubleBlockCombiner.Combiner::acceptNone;

        Float2FloatFunction progressFn = source.apply(ChestBlock.opennessCombiner(entity));
        state.open = progressFn.get(tickProgress);

        if (state.type != ChestType.SINGLE) {
            Int2IntFunction lightFn = source.apply(new BrightnessCombiner<>());
            state.lightCoords = lightFn.applyAsInt(state.lightCoords);
        }
    }

    @Override
    public void submit(
            RenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState
    ) {
        matrices.pushPose();
        matrices.mulPose(ChestRenderer.modelTransformation(state.facing));

        float openFactor = 1.0f - state.open;
        openFactor = 1.0f - openFactor * openFactor * openFactor;

        SpriteId spriteId = RenderCompat.getChestSprite(state.tier, state.type);
        ChestModel model = switch (state.type) {
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
                state.lightCoords,
                state.breakProgress
        );

        matrices.popPose();
    }
}
