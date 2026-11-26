package com.thaistoanf.expandedstorage.block;

import com.thaistoanf.expandedstorage.block.entity.ExpandedChestBlockEntity;
import com.thaistoanf.expandedstorage.compat.NetworkCompat;
import com.thaistoanf.expandedstorage.compat.StorageCompat;
import com.thaistoanf.expandedstorage.core.ChestTier;
import com.thaistoanf.expandedstorage.screen.ExpandedChestScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Tiered chest block inheriting full vanilla {@link ChestBlock} mechanics:
 * same-tier double chest merging, Shift-placement isolation, waterlogging,
 * cat/solid-block obstruction checks, hoppers, and redstone comparators.
 */
public class ExpandedChestBlock extends ChestBlock {
    private final ChestTier tier;
    private final DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Optional<NamedScreenHandlerFactory>> screenRetriever;

    public ExpandedChestBlock(
            ChestTier tier,
            Settings settings,
            Supplier<BlockEntityType<? extends ChestBlockEntity>> entityTypeSupplier
    ) {
        super(settings, entityTypeSupplier);
        this.tier = tier;
        this.screenRetriever = new DoubleBlockProperties.PropertyRetriever<>() {
            @Override
            public Optional<NamedScreenHandlerFactory> getFromBoth(ChestBlockEntity first, ChestBlockEntity second) {
                Inventory doubleInventory = new DoubleInventory(first, second);
                Text title = first.hasCustomName()
                        ? first.getDisplayName()
                        : (second.hasCustomName()
                        ? second.getDisplayName()
                        : Text.translatable("container.expandedstorage." + tier.blockId() + "_double"));
                return Optional.of(NetworkCompat.createExtendedFactory(
                        tier,
                        true,
                        title,
                        (syncId, playerInventory, player) -> {
                            if (first.checkUnlocked(player) && second.checkUnlocked(player)) {
                                first.generateLoot(player);
                                second.generateLoot(player);
                                return new ExpandedChestScreenHandler(syncId, playerInventory, doubleInventory, tier, true);
                            }
                            return null;
                        }
                ));
            }

            @Override
            public Optional<NamedScreenHandlerFactory> getFrom(ChestBlockEntity single) {
                return Optional.of(NetworkCompat.createExtendedFactory(
                        tier,
                        false,
                        single.getDisplayName(),
                        (syncId, playerInventory, player) -> {
                            if (single.checkUnlocked(player)) {
                                single.generateLoot(player);
                                return new ExpandedChestScreenHandler(syncId, playerInventory, single, tier, false);
                            }
                            return null;
                        }
                ));
            }

            @Override
            public Optional<NamedScreenHandlerFactory> getFallback() {
                return Optional.empty();
            }
        };
    }

    public ChestTier getTier() {
        return tier;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ExpandedChestBlockEntity(this.tier, pos, state);
    }

    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return this.getBlockEntitySource(state, world, pos, false).apply(this.screenRetriever).orElse(null);
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return StorageCompat.calculateComparatorSignal(ChestBlock.getInventory(this, state, world, pos, false));
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        // Server chests require zero per-tick updates; client chests skip work whenever closed and idle.
        return world.isClient ? validateTicker(type, this.getExpectedEntityType(), ExpandedChestBlockEntity::clientTick) : null;
    }
}
