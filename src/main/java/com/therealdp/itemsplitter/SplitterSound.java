package com.therealdp.itemsplitter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class SplitterSound {

    public static void playSuccess() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        client.player.playSound(
                SoundEvents.ENTITY_ITEM_PICKUP,
                0.5f,
                1.2f
        );
    }

    public static void playFail() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        client.player.playSound(
                SoundEvents.ENTITY_VILLAGER_NO,
                1.0f,
                1.0f
        );
    }
}