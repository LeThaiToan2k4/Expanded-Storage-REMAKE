package com.thaistoanf.expandedstorage.compat;

import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;

/**
 * Isolates inventory serialization ({@link ReadView} / {@link WriteView} in 1.21.6+)
 * and container utility helpers across Minecraft versions.
 */
public final class StorageCompat {
    private StorageCompat() {
    }

    public static DefaultedList<ItemStack> createStackList(int size) {
        return DefaultedList.ofSize(size, ItemStack.EMPTY);
    }

    public static void readInventory(ReadView view, DefaultedList<ItemStack> stacks) {
        Inventories.readData(view, stacks);
    }

    public static void writeInventory(WriteView view, DefaultedList<ItemStack> stacks) {
        Inventories.writeData(view, stacks);
    }

    public static int calculateComparatorSignal(Inventory inventory) {
        return ScreenHandler.calculateComparatorOutput(inventory);
    }
}
