package com.thaistoanf.expandedstorage.screen;

import com.thaistoanf.expandedstorage.compat.NetworkCompat;
import com.thaistoanf.expandedstorage.core.ChestTier;
import com.thaistoanf.expandedstorage.core.GuiMode;
import com.thaistoanf.expandedstorage.core.LayoutCalculator;
import com.thaistoanf.expandedstorage.registry.ModScreenHandlers;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared container handler for all single and double tiered chests (36 to 270 slots).
 * Relies exclusively on vanilla dirty-slot synchronization without custom full-inventory packets.
 */
public class ExpandedChestScreenHandler extends AbstractContainerMenu {
    private final Container inventory;
    private final ChestTier tier;
    private final boolean doubleChest;
    private final int chestSize;
    private final List<ToggleableSlot> chestSlots;
    private final List<Slot> playerSlots;

    public ExpandedChestScreenHandler(int syncId, Inventory playerInventory, NetworkCompat.ChestOpenData data) {
        this(
                syncId,
                playerInventory,
                new SimpleContainer(data.tier().slots(data.doubleChest())),
                data.tier(),
                data.doubleChest()
        );
    }

    public ExpandedChestScreenHandler(
            int syncId,
            Inventory playerInventory,
            Container inventory,
            ChestTier tier,
            boolean doubleChest
    ) {
        super(ModScreenHandlers.EXPANDED_CHEST, syncId);
        this.tier = tier;
        this.doubleChest = doubleChest;
        this.chestSize = tier.slots(doubleChest);
        checkContainerSize(inventory, this.chestSize);
        this.inventory = inventory;
        inventory.startOpen(playerInventory.player);

        LayoutCalculator.Layout defaultLayout = LayoutCalculator.compute(
                tier, doubleChest, GuiMode.ADAPTIVE, 0, 0, 0
        );

        this.chestSlots = new ArrayList<>(this.chestSize);
        for (int i = 0; i < this.chestSize; i++) {
            LayoutCalculator.SlotPos pos = defaultLayout.chestSlots().get(i);
            ToggleableSlot slot = new ToggleableSlot(inventory, i, pos.x(), pos.y());
            this.chestSlots.add(slot);
            this.addSlot(slot);
        }

        this.playerSlots = new ArrayList<>(36);
        // 27 main player inventory slots (indices 9..35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int listIdx = row * 9 + col;
                LayoutCalculator.SlotPos pos = defaultLayout.playerSlots().get(listIdx);
                Slot slot = new Slot(playerInventory, col + row * 9 + 9, pos.x(), pos.y());
                this.playerSlots.add(slot);
                this.addSlot(slot);
            }
        }
        // 9 hotbar slots (indices 0..8)
        for (int col = 0; col < 9; col++) {
            LayoutCalculator.SlotPos pos = defaultLayout.playerSlots().get(27 + col);
            Slot slot = new Slot(playerInventory, col, pos.x(), pos.y());
            this.playerSlots.add(slot);
            this.addSlot(slot);
        }
    }

    public Container getInventory() {
        return inventory;
    }

    public ChestTier getTier() {
        return tier;
    }

    public boolean isDoubleChest() {
        return doubleChest;
    }

    public int getChestSize() {
        return chestSize;
    }

    /** Updates client-side slot coordinates and active flags for the active GUI layout or tab. */
    public void applyLayout(LayoutCalculator.Layout layout) {
        List<LayoutCalculator.SlotPos> chestCoords = layout.chestSlots();
        for (int i = 0; i < this.chestSlots.size() && i < chestCoords.size(); i++) {
            LayoutCalculator.SlotPos pos = chestCoords.get(i);
            ToggleableSlot slot = this.chestSlots.get(i);
            slot.x = pos.x();
            slot.y = pos.y();
            slot.setActive(pos.visible());
        }

        List<LayoutCalculator.SlotPos> playerCoords = layout.playerSlots();
        for (int i = 0; i < this.playerSlots.size() && i < playerCoords.size(); i++) {
            LayoutCalculator.SlotPos pos = playerCoords.get(i);
            Slot slot = this.playerSlots.get(i);
            slot.x = pos.x();
            slot.y = pos.y();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack transferred = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            transferred = stack.copy();
            if (slotIndex < this.chestSize) {
                if (!this.moveItemStackTo(stack, this.chestSize, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, this.chestSize, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return transferred;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.inventory.stopOpen(player);
    }

    /** Slot whose visibility can be toggled on the client for Smart Tabbed pages. */
    public static class ToggleableSlot extends Slot {
        private boolean active = true;

        public ToggleableSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        @Override
        public boolean isActive() {
            return this.active;
        }
    }
}
