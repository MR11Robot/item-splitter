package com.therealdp.itemsplitter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.HashMap;
import java.util.Map;

public class ItemSplitterClient implements ClientModInitializer {

    private static KeyMapping configKey;
    private static final Map<Integer, Boolean> keyWasDown = new HashMap<>();

    @Override
    public void onInitializeClient() {
        SplitterConfig.load();

        configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.itemsplitter.config",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_K,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (configKey.consumeClick() && client.player != null) {
                client.gui.setScreen(new ConfigScreen());
            }

            if (client.player != null && client.gui.screen() instanceof AbstractContainerScreen<?>) {
                for (SplitterConfig.SplitBind bind : SplitterConfig.get().binds) {
                    int keyCode = bind.keyCode;
                    boolean isDown = InputConstants.isKeyDown(keyCode);
                    boolean wasDown = keyWasDown.getOrDefault(keyCode, false);
                    if (isDown && !wasDown) {
                        SplitterHandler.handleSplitByKey(keyCode);
                    }
                    keyWasDown.put(keyCode, isDown);
                }
            }
        });
    }
}