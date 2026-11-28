package com.thaistoanf.expandedstorage.compat;

import com.thaistoanf.expandedstorage.core.ChestTier;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * Isolates {@link ExtendedMenuProvider} and opening payload serialization
 * using {@code ExtendedMenuType<T, D>} + {@link StreamCodec} in 26.1+.
 */
public final class NetworkCompat {
    private NetworkCompat() {
    }

    /** Lightweight opening metadata sent once when opening a chest; slots sync via vanilla AbstractContainerMenu. */
    public record ChestOpenData(ChestTier tier, boolean doubleChest) {
        public static final StreamCodec<RegistryFriendlyByteBuf, ChestOpenData> STREAM_CODEC = StreamCodec.of(
                NetworkCompat::writeOpenData,
                NetworkCompat::readOpenData
        );
    }

    @FunctionalInterface
    public interface ClientHandlerFactory<T extends AbstractContainerMenu> {
        T create(int syncId, Inventory playerInventory, ChestOpenData data);
    }

    @FunctionalInterface
    public interface ServerMenuCreator {
        AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player);
    }

    public static <T extends AbstractContainerMenu> MenuType<T> createScreenHandlerType(ClientHandlerFactory<T> factory) {
        return new ExtendedMenuType<>(factory::create, ChestOpenData.STREAM_CODEC);
    }

    public static MenuProvider createExtendedFactory(
            ChestTier tier,
            boolean doubleChest,
            Component displayName,
            ServerMenuCreator creator
    ) {
        return new ExtendedMenuProvider<ChestOpenData>() {
            @Override
            public ChestOpenData getScreenOpeningData(ServerPlayer player) {
                return new ChestOpenData(tier, doubleChest);
            }

            @Override
            public Component getDisplayName() {
                return displayName;
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                return creator.createMenu(syncId, playerInventory, player);
            }
        };
    }

    public static void writeOpenData(FriendlyByteBuf buf, ChestOpenData data) {
        buf.writeVarInt(data.tier().ordinal());
        buf.writeBoolean(data.doubleChest());
    }

    public static ChestOpenData readOpenData(FriendlyByteBuf buf) {
        ChestTier tier = ChestTier.byIndex(buf.readVarInt());
        boolean doubleChest = buf.readBoolean();
        return new ChestOpenData(tier, doubleChest);
    }
}
