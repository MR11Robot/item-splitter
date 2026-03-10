package com.therealdp.itemsplitter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.lwjgl.glfw.GLFW;
import java.util.HashMap;
import java.util.Map;

public class ItemSplitterClient implements ClientModInitializer {

    private static KeyBinding configKey;
    private static final Map<Integer, Boolean> keyWasDown = new HashMap<>();

    @Override
    public void onInitializeClient() {
        SplitterConfig.load();

        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.itemsplitter.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (configKey.wasPressed() && client.player != null) {
                client.setScreen(new ConfigScreen());
            }

            if (client.player != null && client.currentScreen instanceof HandledScreen<?>) {
                for (SplitterConfig.SplitBind bind : SplitterConfig.get().binds) {
                    int keyCode = bind.keyCode;
                    boolean isDown = InputUtil.isKeyPressed(client.getWindow(), keyCode);
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