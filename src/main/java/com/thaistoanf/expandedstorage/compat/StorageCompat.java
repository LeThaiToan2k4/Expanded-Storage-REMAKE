package com.thaistoanf.expandedstorage.compat;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Isolates inventory serialization ({@link ValueInput} / {@link ValueOutput} in
 * 26.1+)
 * and container utility helpers across Minecraft versions.
 */
public final class StorageCompat {
    private StorageCompat() {
    }

    public static NonNullList<ItemStack> createStackList(int size) {
        return NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public static void readInventory(ValueInput view, NonNullList<ItemStack> stacks) {
        ContainerHelper.loadAllItems(view, stacks);
    }

    public static void writeInventory(ValueOutput view, NonNullList<ItemStack> stacks) {
        ContainerHelper.saveAllItems(view, stacks);
    }

    public static int calculateComparatorSignal(Container inventory) {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(inventory);
    }
}
