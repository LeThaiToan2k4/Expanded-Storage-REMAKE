package com.thaistoanf.expandedstorage.block;

import com.thaistoanf.expandedstorage.block.entity.ExpandedChestBlockEntity;
import com.thaistoanf.expandedstorage.compat.NetworkCompat;
import com.thaistoanf.expandedstorage.compat.StorageCompat;
import com.thaistoanf.expandedstorage.core.ChestTier;
import com.thaistoanf.expandedstorage.screen.ExpandedChestScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Tiered chest block inheriting full vanilla {@link ChestBlock} mechanics:
 * same-tier double chest merging, Shift-placement isolation, waterlogging,
 * cat/solid-block obstruction checks, hoppers, and redstone comparators.
 */
public class ExpandedChestBlock extends ChestBlock {
    private final ChestTier tier;
    private final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>> screenRetriever;

    public ExpandedChestBlock(
            ChestTier tier,
            BlockBehaviour.Properties settings,
            Supplier<BlockEntityType<? extends ChestBlockEntity>> entityTypeSupplier
    ) {
        super(entityTypeSupplier, SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE, settings);
        this.tier = tier;
        this.screenRetriever = new DoubleBlockCombiner.Combiner<>() {
            @Override
            public Optional<MenuProvider> acceptDouble(ChestBlockEntity first, ChestBlockEntity second) {
                Container doubleInventory = new CompoundContainer(first, second);
                Component title = first.hasCustomName()
                        ? first.getDisplayName()
                        : (second.hasCustomName()
                        ? second.getDisplayName()
                        : Component.translatable("container.expandedstorage." + tier.blockId() + "_double"));
                return Optional.of(NetworkCompat.createExtendedFactory(
                        tier,
                        true,
                        title,
                        (syncId, playerInventory, player) -> {
                            if (first.canOpen(player) && second.canOpen(player)) {
                                first.unpackLootTable(player);
                                second.unpackLootTable(player);
                                return new ExpandedChestScreenHandler(syncId, playerInventory, doubleInventory, tier, true);
                            }
                            return null;
                        }
                ));
            }

            @Override
            public Optional<MenuProvider> acceptSingle(ChestBlockEntity single) {
                return Optional.of(NetworkCompat.createExtendedFactory(
                        tier,
                        false,
                        single.getDisplayName(),
                        (syncId, playerInventory, player) -> {
                            if (single.canOpen(player)) {
                                single.unpackLootTable(player);
                                return new ExpandedChestScreenHandler(syncId, playerInventory, single, tier, false);
                            }
                            return null;
                        }
                ));
            }

            @Override
            public Optional<MenuProvider> acceptNone() {
                return Optional.empty();
            }
        };
    }

    public ChestTier getTier() {
        return tier;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExpandedChestBlockEntity(this.tier, pos, state);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        return this.combine(state, world, pos, false).apply(this.screenRetriever).orElse(null);
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, Direction direction) {
        return StorageCompat.calculateComparatorSignal(ChestBlock.getContainer(this, state, world, pos, false));
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        // Server chests require zero per-tick updates; client chests skip work whenever closed and idle.
        return world.isClientSide() ? createTickerHelper(type, this.blockEntityType(), ExpandedChestBlockEntity::clientTick) : null;
    }
}
