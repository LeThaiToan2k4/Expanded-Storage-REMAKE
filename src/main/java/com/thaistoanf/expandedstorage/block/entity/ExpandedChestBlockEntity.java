package com.thaistoanf.expandedstorage.block.entity;

import com.thaistoanf.expandedstorage.block.ExpandedChestBlock;
import com.thaistoanf.expandedstorage.compat.StorageCompat;
import com.thaistoanf.expandedstorage.core.ChestTier;
import com.thaistoanf.expandedstorage.registry.ModBlockEntities;
import com.thaistoanf.expandedstorage.screen.ExpandedChestScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ChestLidAnimator;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Tiered chest block entity with zero server tick overhead, dormant-when-closed
 * client lid animation,
 * and accurate viewer tracking for {@link ExpandedChestScreenHandler}.
 */
public class ExpandedChestBlockEntity extends ChestBlockEntity {
    private static final int VIEWER_COUNT_EVENT = 1;

    private final ChestTier tier;
    private final ChestLidAnimator customLidAnimator = new ChestLidAnimator();
    private boolean lidOpen = false;

    private final ViewerCountManager viewerManager = new ViewerCountManager() {
        @Override
        protected void onContainerOpen(World world, BlockPos pos, BlockState state) {
            playChestSound(world, pos, state, SoundEvents.BLOCK_CHEST_OPEN);
        }

        @Override
        protected void onContainerClose(World world, BlockPos pos, BlockState state) {
            playChestSound(world, pos, state, SoundEvents.BLOCK_CHEST_CLOSE);
        }

        @Override
        protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount,
                int newViewerCount) {
            ExpandedChestBlockEntity.this.onViewerCountUpdate(world, pos, state, oldViewerCount, newViewerCount);
        }

        @Override
        protected boolean isPlayerViewing(PlayerEntity player) {
            if (player.currentScreenHandler instanceof ExpandedChestScreenHandler handler) {
                Inventory inv = handler.getInventory();
                return inv == ExpandedChestBlockEntity.this
                        || (inv instanceof DoubleInventory doubleInv
                                && doubleInv.isPart(ExpandedChestBlockEntity.this));
            }
            return false;
        }
    };

    public ExpandedChestBlockEntity(ChestTier tier, BlockPos pos, BlockState state) {
        super(ModBlockEntities.get(tier), pos, state);
        this.tier = tier;
        this.setHeldStacks(StorageCompat.createStackList(tier.singleSlots()));
    }

    public ExpandedChestBlockEntity(BlockPos pos, BlockState state) {
        this(resolveTier(state), pos, state);
    }

    private static ChestTier resolveTier(BlockState state) {
        if (state != null && state.getBlock() instanceof ExpandedChestBlock chestBlock) {
            return chestBlock.getTier();
        }
        return ChestTier.COPPER;
    }

    public ChestTier getTier() {
        return tier;
    }

    @Override
    public int size() {
        return tier.singleSlots();
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("container.expandedstorage." + tier.blockId());
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        DefaultedList<ItemStack> stacks = StorageCompat.createStackList(this.size());
        if (!this.readLootTable(view)) {
            StorageCompat.readInventory(view, stacks);
        }
        this.setHeldStacks(stacks);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        if (!this.writeLootTable(view)) {
            StorageCompat.writeInventory(view, this.getHeldStacks());
        }
    }

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (type == VIEWER_COUNT_EVENT) {
            this.lidOpen = data > 0;
            this.customLidAnimator.setOpen(this.lidOpen);
            return true;
        }
        return super.onSyncedBlockEvent(type, data);
    }

    @Override
    public float getAnimationProgress(float tickDelta) {
        return this.customLidAnimator.getProgress(tickDelta);
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, ChestBlockEntity entity) {
        if (!(entity instanceof ExpandedChestBlockEntity chest)) {
            return;
        }
        // Zero idle tick work when chest is closed and lid animation has fully settled
        // at 0.
        if (!chest.lidOpen
                && chest.viewerManager.getViewerCount() == 0
                && chest.customLidAnimator.getProgress(1.0f) <= 0.0f
                && chest.customLidAnimator.getProgress(0.0f) <= 0.0f) {
            return;
        }
        chest.customLidAnimator.step();
    }

    @Override
    public void onOpen(PlayerEntity player) {
        if (!this.removed && !player.isSpectator() && this.getWorld() != null) {
            this.viewerManager.openContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    @Override
    public void onClose(PlayerEntity player) {
        if (!this.removed && !player.isSpectator() && this.getWorld() != null) {
            this.viewerManager.closeContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    @Override
    public void onScheduledTick() {
        if (!this.removed && this.getWorld() != null) {
            this.viewerManager.updateViewerCount(this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new ExpandedChestScreenHandler(syncId, playerInventory, this, this.tier, false);
    }

    private static void playChestSound(World world, BlockPos pos, BlockState state, SoundEvent soundEvent) {
        ChestType chestType = state.get(ChestBlock.CHEST_TYPE);
        if (chestType == ChestType.LEFT) {
            return;
        }
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        if (chestType == ChestType.RIGHT) {
            Direction neighborDir = ChestBlock.getFacing(state);
            x += neighborDir.getOffsetX() * 0.5;
            z += neighborDir.getOffsetZ() * 0.5;
        }
        world.playSound(
                null, x, y, z,
                soundEvent, SoundCategory.BLOCKS,
                0.5f, world.random.nextFloat() * 0.1f + 0.9f);
    }
}
