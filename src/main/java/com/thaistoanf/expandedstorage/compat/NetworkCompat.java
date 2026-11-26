package com.thaistoanf.expandedstorage.compat;

import com.thaistoanf.expandedstorage.core.ChestTier;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Isolates {@link ExtendedScreenHandlerFactory} and opening payload serialization
 * using {@code ExtendedScreenHandlerType<T, D>} + {@link PacketCodec} in 1.20.5+.
 */
public final class NetworkCompat {
    private NetworkCompat() {
    }

    /** Lightweight opening metadata sent once when opening a chest; slots sync via vanilla ScreenHandler. */
    public record ChestOpenData(ChestTier tier, boolean doubleChest) {
        public static final PacketCodec<RegistryByteBuf, ChestOpenData> PACKET_CODEC = PacketCodec.of(
                (value, buf) -> writeOpenData(buf, value),
                NetworkCompat::readOpenData
        );
    }

    @FunctionalInterface
    public interface ClientHandlerFactory<T extends ScreenHandler> {
        T create(int syncId, PlayerInventory playerInventory, ChestOpenData data);
    }

    @FunctionalInterface
    public interface ServerMenuCreator {
        ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player);
    }

    public static <T extends ScreenHandler> ScreenHandlerType<T> createScreenHandlerType(ClientHandlerFactory<T> factory) {
        return new ExtendedScreenHandlerType<>(factory::create, ChestOpenData.PACKET_CODEC);
    }

    public static NamedScreenHandlerFactory createExtendedFactory(
            ChestTier tier,
            boolean doubleChest,
            Text displayName,
            ServerMenuCreator creator
    ) {
        return new ExtendedScreenHandlerFactory<ChestOpenData>() {
            @Override
            public ChestOpenData getScreenOpeningData(ServerPlayerEntity player) {
                return new ChestOpenData(tier, doubleChest);
            }

            @Override
            public Text getDisplayName() {
                return displayName;
            }

            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                return creator.createMenu(syncId, playerInventory, player);
            }
        };
    }

    public static void writeOpenData(PacketByteBuf buf, ChestOpenData data) {
        buf.writeVarInt(data.tier().ordinal());
        buf.writeBoolean(data.doubleChest());
    }

    public static ChestOpenData readOpenData(PacketByteBuf buf) {
        ChestTier tier = ChestTier.byIndex(buf.readVarInt());
        boolean doubleChest = buf.readBoolean();
        return new ChestOpenData(tier, doubleChest);
    }
}
