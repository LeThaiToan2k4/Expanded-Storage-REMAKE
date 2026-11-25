package com.thaistoanf.expandedstorage.compat;

import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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

    public static void readInventory(NbtCompound nbt, DefaultedList<ItemStack> stacks) {
        Inventories.readNbt(nbt, stacks);
    }

    public static void writeInventory(NbtCompound nbt, DefaultedList<ItemStack> stacks) {
        Inventories.writeNbt(nbt, stacks);
    }

    public static int calculateComparatorSignal(Inventory inventory) {
        return ScreenHandler.calculateComparatorOutput(inventory);
    }
}
