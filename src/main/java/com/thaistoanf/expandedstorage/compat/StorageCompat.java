package com.thaistoanf.expandedstorage.compat;

import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;

/**
 * Isolates NBT inventory serialization and container utility helpers across Minecraft versions
 * (e.g. {@code Inventories.readNbt} gaining {@code WrapperLookup} in 1.20.5+).
 */
public final class StorageCompat {
    private StorageCompat() {
    }

    public static DefaultedList<ItemStack> createStackList(int size) {
        return DefaultedList.ofSize(size, ItemStack.EMPTY);
    }

    public static void readInventory(
            NbtCompound nbt,
            DefaultedList<ItemStack> stacks,
            RegistryWrapper.WrapperLookup registries
    ) {
        Inventories.readNbt(nbt, stacks, registries);
    }

    public static void writeInventory(
            NbtCompound nbt,
            DefaultedList<ItemStack> stacks,
            RegistryWrapper.WrapperLookup registries
    ) {
        Inventories.writeNbt(nbt, stacks, registries);
    }

    public static int calculateComparatorSignal(Inventory inventory) {
        return ScreenHandler.calculateComparatorOutput(inventory);
    }
}
