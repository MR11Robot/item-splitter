package com.therealdp.itemsplitter;

import com.therealdp.itemsplitter.mixin.HandledScreenMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SplitterHandler {

    public static void handleSplitByKey(int keyCode) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        if (client.gameMode == null) return;

        SplitterConfig.SplitBind bind = null;
        for (SplitterConfig.SplitBind b : SplitterConfig.get().binds) {
            if (b.keyCode == keyCode) {
                bind = b;
                break;
            }
        }

        if (bind == null) return;
        int amount = bind.amount;

        if (!(client.gui.screen() instanceof AbstractContainerScreen<?> handledScreen)) return;

        Slot hoveredSlot = ((HandledScreenMixin) handledScreen).getFocusedSlot();
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        ItemStack stack = hoveredSlot.getItem();
        if (stack.getCount() <= amount) return;

        AbstractContainerMenu handler = client.player.containerMenu;
        boolean inContainer = !(hoveredSlot.container instanceof Inventory);
        int sourceSlotIndex = handler.slots.indexOf(hoveredSlot);
        if (sourceSlotIndex == -1) return;

        int remaining = stack.getCount();

        for (Slot slot : handler.slots) {
            if (remaining <= amount) break;
            if (slot == hoveredSlot) continue;
            if (!slot.getItem().isEmpty()) continue;
            // skip slots that cannot accept this item type (e.g. crafting output, armor slots)
            if (!slot.mayPlace(stack)) continue;
            if (!slot.isActive()) continue;

            // only split within the same inventory region (container <-> container, player <-> player)
            boolean slotInContainer = !(slot.container instanceof Inventory);
            if (slotInContainer != inContainer) continue;

            int targetSlotIndex = handler.slots.indexOf(slot);
            if (targetSlotIndex == -1) continue;

            client.gameMode.handleContainerInput(handler.containerId, sourceSlotIndex, 0, ContainerInput.PICKUP, client.player);

            for (int i = 0; i < amount; i++) {
                client.gameMode.handleContainerInput(handler.containerId, targetSlotIndex, 1, ContainerInput.PICKUP, client.player);
            }

            client.gameMode.handleContainerInput(handler.containerId, sourceSlotIndex, 0, ContainerInput.PICKUP, client.player);

            remaining -= amount;
        }
    }
}