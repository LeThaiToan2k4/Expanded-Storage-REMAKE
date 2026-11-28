package com.thaistoanf.expandedstorage.block.entity;

import com.thaistoanf.expandedstorage.block.ExpandedChestBlock;
import com.thaistoanf.expandedstorage.compat.StorageCompat;
import com.thaistoanf.expandedstorage.core.ChestTier;
import com.thaistoanf.expandedstorage.registry.ModBlockEntities;
import com.thaistoanf.expandedstorage.screen.ExpandedChestScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

/**
 * Tiered chest block entity with zero server tick overhead, dormant-when-closed
 * client lid animation, and accurate viewer tracking for {@link ExpandedChestScreenHandler}.
 */
public class ExpandedChestBlockEntity extends ChestBlockEntity {
    private static final int VIEWER_COUNT_EVENT = 1;

    private final ChestTier tier;
    private final ChestLidController customLidAnimator = new ChestLidController();
    private boolean lidOpen = false;

    private final ContainerOpenersCounter viewerManager = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level world, BlockPos pos, BlockState state) {
            playChestSound(world, pos, state, SoundEvents.CHEST_OPEN);
        }

        @Override
        protected void onClose(Level world, BlockPos pos, BlockState state) {
            playChestSound(world, pos, state, SoundEvents.CHEST_CLOSE);
        }

        @Override
        protected void openerCountChanged(Level world, BlockPos pos, BlockState state, int oldViewerCount,
                int newViewerCount) {
            ExpandedChestBlockEntity.this.signalOpenCount(world, pos, state, oldViewerCount, newViewerCount);
        }

        @Override
        public boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof ExpandedChestScreenHandler handler) {
                Container inv = handler.getInventory();
                return inv == ExpandedChestBlockEntity.this
                        || (inv instanceof CompoundContainer doubleInv
                                && doubleInv.contains(ExpandedChestBlockEntity.this));
            }
            return false;
        }
    };

    public ExpandedChestBlockEntity(ChestTier tier, BlockPos pos, BlockState state) {
        super(ModBlockEntities.get(tier), pos, state);
        this.tier = tier;
        this.setItems(StorageCompat.createStackList(tier.singleSlots()));
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
    public int getContainerSize() {
        return tier.singleSlots();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.expandedstorage." + tier.blockId());
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        NonNullList<ItemStack> stacks = StorageCompat.createStackList(this.getContainerSize());
        if (!this.tryLoadLootTable(view)) {
            StorageCompat.readInventory(view, stacks);
        }
        this.setItems(stacks);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        if (!this.trySaveLootTable(view)) {
            StorageCompat.writeInventory(view, this.getItems());
        }
    }

    @Override
    public boolean triggerEvent(int type, int data) {
        if (type == VIEWER_COUNT_EVENT) {
            this.lidOpen = data > 0;
            this.customLidAnimator.shouldBeOpen(this.lidOpen);
            return true;
        }
        return super.triggerEvent(type, data);
    }

    @Override
    public float getOpenNess(float tickDelta) {
        return this.customLidAnimator.getOpenness(tickDelta);
    }

    public static void clientTick(Level world, BlockPos pos, BlockState state, ChestBlockEntity entity) {
        if (!(entity instanceof ExpandedChestBlockEntity chest)) {
            return;
        }
        // Zero idle tick work when chest is closed and lid animation has fully settled at 0.
        if (!chest.lidOpen
                && chest.viewerManager.getOpenerCount() == 0
                && chest.customLidAnimator.getOpenness(1.0f) <= 0.0f
                && chest.customLidAnimator.getOpenness(0.0f) <= 0.0f) {
            return;
        }
        chest.customLidAnimator.tickLid();
    }

    @Override
    public void startOpen(ContainerUser user) {
        if (!this.remove && !user.getLivingEntity().isSpectator() && this.getLevel() != null) {
            this.viewerManager.incrementOpeners(
                    user.getLivingEntity(),
                    this.getLevel(),
                    this.getBlockPos(),
                    this.getBlockState(),
                    user.getContainerInteractionRange()
            );
        }
    }

    @Override
    public void stopOpen(ContainerUser user) {
        if (!this.remove && !user.getLivingEntity().isSpectator() && this.getLevel() != null) {
            this.viewerManager.decrementOpeners(
                    user.getLivingEntity(),
                    this.getLevel(),
                    this.getBlockPos(),
                    this.getBlockState()
            );
        }
    }

    @Override
    public List<ContainerUser> getEntitiesWithContainerOpen() {
        if (this.getLevel() == null) {
            return List.of();
        }
        return this.viewerManager.getEntitiesWithContainerOpen(this.getLevel(), this.getBlockPos());
    }

    @Override
    public void recheckOpen() {
        if (!this.remove && this.getLevel() != null) {
            this.viewerManager.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return new ExpandedChestScreenHandler(syncId, playerInventory, this, this.tier, false);
    }

    private static void playChestSound(Level world, BlockPos pos, BlockState state, SoundEvent soundEvent) {
        ChestType chestType = state.getValue(ChestBlock.TYPE);
        if (chestType == ChestType.LEFT) {
            return;
        }
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        if (chestType == ChestType.RIGHT) {
            Direction neighborDir = ChestBlock.getConnectedDirection(state);
            x += neighborDir.getStepX() * 0.5;
            z += neighborDir.getStepZ() * 0.5;
        }
        world.playSound(
                null, x, y, z,
                soundEvent, SoundSource.BLOCKS,
                0.5f, world.getRandom().nextFloat() * 0.1f + 0.9f);
    }
}
