package com.thaistoanf.expandedstorage.client;

import com.thaistoanf.expandedstorage.block.entity.ExpandedChestBlockEntity;
import com.thaistoanf.expandedstorage.compat.RenderCompat;
import com.thaistoanf.expandedstorage.registry.ModBlocks;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.LightmapCoordinatesRetriever;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;

/**
 * Renders single and double tiered chests using the vanilla chest geometry and
 * sprites batched inside {@code minecraft:textures/atlas/chest.png}.
 */
@Environment(EnvType.CLIENT)
public class ExpandedChestBlockEntityRenderer implements BlockEntityRenderer<ExpandedChestBlockEntity> {
    private static final String BASE = "bottom";
    private static final String LID = "lid";
    private static final String LATCH = "lock";

    private final ModelPart singleLid;
    private final ModelPart singleBase;
    private final ModelPart singleLatch;
    private final ModelPart doubleLeftLid;
    private final ModelPart doubleLeftBase;
    private final ModelPart doubleLeftLatch;
    private final ModelPart doubleRightLid;
    private final ModelPart doubleRightBase;
    private final ModelPart doubleRightLatch;

    public ExpandedChestBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        ModelPart single = ctx.getLayerModelPart(EntityModelLayers.CHEST);
        this.singleBase = single.getChild(BASE);
        this.singleLid = single.getChild(LID);
        this.singleLatch = single.getChild(LATCH);

        ModelPart left = ctx.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_LEFT);
        this.doubleLeftBase = left.getChild(BASE);
        this.doubleLeftLid = left.getChild(LID);
        this.doubleLeftLatch = left.getChild(LATCH);

        ModelPart right = ctx.getLayerModelPart(EntityModelLayers.DOUBLE_CHEST_RIGHT);
        this.doubleRightBase = right.getChild(BASE);
        this.doubleRightLid = right.getChild(LID);
        this.doubleRightLatch = right.getChild(LATCH);
    }

    @Override
    public void render(
            ExpandedChestBlockEntity entity,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay
    ) {
        World world = entity.getWorld();
        boolean inWorld = world != null;
        BlockState state = inWorld
                ? entity.getCachedState()
                : ModBlocks.get(entity.getTier()).getDefaultState().with(ChestBlock.FACING, Direction.SOUTH);

        ChestType chestType = state.contains(ChestBlock.CHEST_TYPE) ? state.get(ChestBlock.CHEST_TYPE) : ChestType.SINGLE;
        Block block = state.getBlock();
        if (!(block instanceof AbstractChestBlock<?> abstractChest)) {
            return;
        }

        matrices.push();
        float rotation = state.get(ChestBlock.FACING).getPositiveHorizontalDegrees();
        matrices.translate(0.5f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-rotation));
        matrices.translate(-0.5f, -0.5f, -0.5f);

        @SuppressWarnings("unchecked")
        DoubleBlockProperties.PropertySource<? extends ChestBlockEntity> source = inWorld
                ? ((AbstractChestBlock<ChestBlockEntity>) abstractChest).getBlockEntitySource(state, world, entity.getPos(), true)
                : DoubleBlockProperties.PropertyRetriever::getFallback;

        Float2FloatFunction progressFn = source.apply(ChestBlock.getAnimationProgressRetriever(entity));
        float openFactor = 1.0f - progressFn.get(tickDelta);
        openFactor = 1.0f - openFactor * openFactor * openFactor;

        Int2IntFunction lightFn = source.apply(new LightmapCoordinatesRetriever<>());
        int packedLight = lightFn.applyAsInt(light);

        SpriteIdentifier sprite = RenderCompat.getChestSprite(entity.getTier(), chestType);
        VertexConsumer vertices = RenderCompat.getChestVertexConsumer(vertexConsumers, sprite);

        if (chestType != ChestType.SINGLE) {
            if (chestType == ChestType.LEFT) {
                RenderCompat.renderChestParts(
                        matrices, vertices,
                        this.doubleLeftLid, this.doubleLeftLatch, this.doubleLeftBase,
                        openFactor, packedLight, overlay
                );
            } else {
                RenderCompat.renderChestParts(
                        matrices, vertices,
                        this.doubleRightLid, this.doubleRightLatch, this.doubleRightBase,
                        openFactor, packedLight, overlay
                );
            }
        } else {
            RenderCompat.renderChestParts(
                    matrices, vertices,
                    this.singleLid, this.singleLatch, this.singleBase,
                    openFactor, packedLight, overlay
            );
        }

        matrices.pop();
    }
}
