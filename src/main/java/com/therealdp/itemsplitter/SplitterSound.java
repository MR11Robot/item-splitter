package com.therealdp.itemsplitter;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;

public class SplitterSound {

    public static void playSuccess() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        client.player.playSound(
                SoundEvents.ITEM_PICKUP,
                0.5f,
                1.2f
        );
    }

    public static void playFail() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        client.player.playSound(
                SoundEvents.VILLAGER_NO,
                1.0f,
                1.0f
        );
    }
}