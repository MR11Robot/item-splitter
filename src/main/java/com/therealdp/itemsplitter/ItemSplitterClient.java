package com.therealdp.itemsplitter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.lwjgl.glfw.GLFW;
import java.util.HashMap;
import java.util.Map;

public class ItemSplitterClient implements ClientModInitializer {

    private static KeyBinding configKey;
    private static KeyBinding hudNextPage;
    private static KeyBinding hudPrevPage;
    private static final Map<Integer, Boolean> keyWasDown = new HashMap<>();
    private static boolean hudNextWasDown = false;
    private static boolean hudPrevWasDown = false;

    @Override
    public void onInitializeClient() {
        SplitterConfig.load();

        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.itemsplitter.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                KeyBinding.Category.MISC
        ));

        hudNextPage = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.itemsplitter.hud_next",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_ADD,
                KeyBinding.Category.MISC
        ));

        hudPrevPage = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.itemsplitter.hud_prev",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_SUBTRACT,
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

                boolean nextDown = InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_KP_ADD);
                boolean prevDown = InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_KP_SUBTRACT);

                if (nextDown && !hudNextWasDown) SplitterHud.nextHudPage();
                if (prevDown && !hudPrevWasDown) SplitterHud.prevHudPage();

                hudNextWasDown = nextDown;
                hudPrevWasDown = prevDown;
            }
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?> handledScreen) {
                SplitterHud.render(context, handledScreen);
            }
        });
    }
}