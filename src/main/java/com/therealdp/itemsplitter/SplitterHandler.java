package com.therealdp.itemsplitter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import com.therealdp.itemsplitter.mixin.HandledScreenMixin;

public class SplitterHandler {

    public static void handleSplitByKey(int keyCode) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        SplitterConfig.SplitBind bind = null;
        for (SplitterConfig.SplitBind b : SplitterConfig.get().binds) {
            if (b.keyCode == keyCode) {
                bind = b;
                break;
            }
        }

        if (bind == null) return;
        int amount = bind.amount;

        if (!(client.currentScreen instanceof HandledScreen<?> handledScreen)) return;

        Slot hoveredSlot = ((HandledScreenMixin) handledScreen).getFocusedSlot();
        if (hoveredSlot == null || !hoveredSlot.hasStack()) return;

        ItemStack stack = hoveredSlot.getStack();
        if (stack.getCount() <= amount) return;

        ScreenHandler handler = client.player.currentScreenHandler;
        boolean inContainer = !(hoveredSlot.inventory instanceof PlayerInventory);
        int sourceSlotIndex = handler.slots.indexOf(hoveredSlot);
        if (sourceSlotIndex == -1) return;

        int remaining = stack.getCount();

        for (Slot slot : handler.slots) {
            if (remaining <= amount) break;
            if (slot == hoveredSlot) continue;
            if (!slot.getStack().isEmpty()) continue;

            boolean slotInContainer = !(slot.inventory instanceof PlayerInventory);
            if (slotInContainer != inContainer) continue;

            int targetSlotIndex = handler.slots.indexOf(slot);
            if (targetSlotIndex == -1) continue;

            client.interactionManager.clickSlot(handler.syncId, sourceSlotIndex, 0, SlotActionType.PICKUP, client.player);

            for (int i = 0; i < amount; i++) {
                client.interactionManager.clickSlot(handler.syncId, targetSlotIndex, 1, SlotActionType.PICKUP, client.player);
            }

            client.interactionManager.clickSlot(handler.syncId, sourceSlotIndex, 0, SlotActionType.PICKUP, client.player);

            remaining -= amount;
        }
    }
}