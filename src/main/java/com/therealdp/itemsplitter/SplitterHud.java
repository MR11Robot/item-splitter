package com.therealdp.itemsplitter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

public class SplitterHud {

    private static int hudPage = 0;
    private static final int BINDS_PER_HUD_PAGE = 5;

    public static void render(DrawContext context, HandledScreen<?> screen) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        List<SplitterConfig.SplitBind> binds = SplitterConfig.get().binds;
        if (binds.isEmpty()) return;

        int screenX = getScreenX(screen);
        int screenY = getScreenY(screen);
        int screenWidth = getScreenWidth(screen);

        int totalPages = (int) Math.ceil((double) binds.size() / BINDS_PER_HUD_PAGE);
        hudPage = Math.min(hudPage, Math.max(0, totalPages - 1));

        int start = hudPage * BINDS_PER_HUD_PAGE;
        int end = Math.min(start + BINDS_PER_HUD_PAGE, binds.size());

        List<Text> lines = new ArrayList<>();
        lines.add(Text.literal("§6§lSplitter Keys"));
        lines.add(Text.literal("§8─────────────"));

        for (int i = start; i < end; i++) {
            SplitterConfig.SplitBind bind = binds.get(i);
            lines.add(Text.literal("§b" + bind.keyDisplay + " §7> §f" + bind.amount));
        }

        if (totalPages > 1) {
            lines.add(Text.literal("§8─────────────"));
            lines.add(Text.literal("§7Page " + (hudPage + 1) + "/" + totalPages));
        }

        int tooltipX = screenX + screenWidth + 12;
        int tooltipY = screenY + 10;

        if (tooltipX + 130 > client.getWindow().getScaledWidth()) {
            tooltipX = screenX - 140;
        }

        context.drawTooltip(client.textRenderer, lines, tooltipX, tooltipY);
    }

    public static void nextHudPage() {
        List<SplitterConfig.SplitBind> binds = SplitterConfig.get().binds;
        int totalPages = (int) Math.ceil((double) binds.size() / BINDS_PER_HUD_PAGE);
        if (totalPages > 0) hudPage = (hudPage + 1) % totalPages;
    }

    public static void prevHudPage() {
        List<SplitterConfig.SplitBind> binds = SplitterConfig.get().binds;
        int totalPages = (int) Math.ceil((double) binds.size() / BINDS_PER_HUD_PAGE);
        if (totalPages > 0) hudPage = (hudPage - 1 + totalPages) % totalPages;
    }

    private static int getScreenX(HandledScreen<?> screen) {
        try {
            var field = HandledScreen.class.getDeclaredField("x");
            field.setAccessible(true);
            return (int) field.get(screen);
        } catch (Exception e) {
            return MinecraftClient.getInstance().getWindow().getScaledWidth() / 2 - 88;
        }
    }

    private static int getScreenY(HandledScreen<?> screen) {
        try {
            var field = HandledScreen.class.getDeclaredField("y");
            field.setAccessible(true);
            return (int) field.get(screen);
        } catch (Exception e) {
            return MinecraftClient.getInstance().getWindow().getScaledHeight() / 2 - 83;
        }
    }

    private static int getScreenWidth(HandledScreen<?> screen) {
        try {
            var field = HandledScreen.class.getDeclaredField("backgroundWidth");
            field.setAccessible(true);
            return (int) field.get(screen);
        } catch (Exception e) {
            return 176;
        }
    }
}