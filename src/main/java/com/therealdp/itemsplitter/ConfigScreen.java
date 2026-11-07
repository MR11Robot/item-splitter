package com.therealdp.itemsplitter;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {

    private EditBox amountField;
    private boolean listeningForKey = false;
    private String listeningKeyDisplay = "";
    private int pendingKeyCode = -1;
    private int currentPage = 0;
    private boolean hasConflict = false;
    private static final int BINDS_PER_PAGE = 8;
    private static final int ROW_HEIGHT = 24;
    private static final int BTN_HEIGHT = 20;
    private static final int KEY_BTN_WIDTH = 120;
    private static final int AMOUNT_WIDTH = 50;
    private static final int DELETE_WIDTH = 20;
    private static final int GAP = 5;

    public ConfigScreen() {
        super(Component.literal("Item Splitter Config"));
    }

    @Override
    protected void init() {
        hasConflict = false;
        int centerX = this.width / 2;

        int totalRowWidth = KEY_BTN_WIDTH + GAP + AMOUNT_WIDTH + GAP + DELETE_WIDTH;
        int rowStartX = centerX - totalRowWidth / 2;

        int controlsY = 20;
        int bindsStartY = controlsY + BTN_HEIGHT + 15;

        String keyBtnText = listeningForKey
                ? "§eListening..."
                : (listeningKeyDisplay.isEmpty() ? "[ Set Key ]" : "§b" + listeningKeyDisplay);

        this.addRenderableWidget(Button.builder(
                Component.literal(keyBtnText),
                button -> {
                    listeningForKey = true;
                    listeningKeyDisplay = "";
                    pendingKeyCode = -1;
                    this.clearWidgets();
                    this.init();
                }
        ).bounds(rowStartX, controlsY, KEY_BTN_WIDTH, BTN_HEIGHT).build());

        amountField = new EditBox(
                this.font,
                rowStartX + KEY_BTN_WIDTH + GAP, controlsY,
                AMOUNT_WIDTH, BTN_HEIGHT,
                Component.literal("Amt")
        );
        amountField.setHint(Component.literal("Amt"));
        this.addRenderableWidget(amountField);

        this.addRenderableWidget(Button.builder(
                Component.literal("§aAdd"),
                button -> addBind()
        ).bounds(rowStartX + KEY_BTN_WIDTH + GAP + AMOUNT_WIDTH + GAP, controlsY, DELETE_WIDTH + 20, BTN_HEIGHT).build());

        renderBindButtons(rowStartX, bindsStartY);

        List<SplitterConfig.SplitBind> binds = SplitterConfig.get().binds;
        int totalPages = (int) Math.ceil((double) binds.size() / BINDS_PER_PAGE);
        if (totalPages > 1) {
            int pageY = this.height - 28;
            this.addRenderableWidget(Button.builder(
                    Component.literal("< Prev"),
                    button -> {
                        if (currentPage > 0) { currentPage--; this.clearWidgets(); this.init(); }
                    }
            ).bounds(centerX - 65, pageY, 55, BTN_HEIGHT).build());

            this.addRenderableWidget(Button.builder(
                    Component.literal("Next >"),
                    button -> {
                        if (currentPage < totalPages - 1) { currentPage++; this.clearWidgets(); this.init(); }
                    }
            ).bounds(centerX + 10, pageY, 55, BTN_HEIGHT).build());
        }
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (listeningForKey) {
            if (input.isEscape()) {
                listeningForKey = false;
                listeningKeyDisplay = "";
                pendingKeyCode = -1;
            } else {
                pendingKeyCode = input.key();
                String raw = InputConstants.getKey(input).getName()
                        .replace("key.keyboard.", "")
                        .replace("keypad.", "Numpad ")
                        .replace(".", " ");
                listeningKeyDisplay = raw.substring(0, 1).toUpperCase() + raw.substring(1);
                listeningForKey = false;
            }
            this.clearWidgets();
            this.init();
            return true;
        }
        return super.keyPressed(input);
    }

    private String getConflictingBind(int keyCode) {
        Minecraft client = Minecraft.getInstance();
        if (client.options == null) return null;
        for (net.minecraft.client.KeyMapping binding : client.options.keyMappings) {
            if (binding.getDefaultKey().getValue() == keyCode ||
                    binding.saveString().equals(
                            InputConstants.Type.KEYBOARD.getOrCreate(keyCode).getName())) {
                return binding.getTranslatedKeyMessage().getString();
            }
        }
        return null;
    }

    private void addBind() {
        try {
            int amount = Integer.parseInt(amountField.getValue().trim());
            if (amount <= 0 || pendingKeyCode == -1) return;

            String conflict = getConflictingBind(pendingKeyCode);
            if (conflict != null) {
                hasConflict = true;
                listeningKeyDisplay = "§c✗ " + conflict;
                SplitterSound.playFail();
                this.clearWidgets();
                this.init();
                return;
            }

            hasConflict = false;
            SplitterConfig.get().binds.removeIf(b -> b.keyCode == pendingKeyCode);
            SplitterConfig.get().binds.add(new SplitterConfig.SplitBind(pendingKeyCode, listeningKeyDisplay, amount));
            SplitterConfig.save();

            pendingKeyCode = -1;
            listeningKeyDisplay = "";
            int totalPages = (int) Math.ceil((double) SplitterConfig.get().binds.size() / BINDS_PER_PAGE);
            currentPage = Math.max(0, totalPages - 1);

            this.clearWidgets();
            this.init();
        } catch (NumberFormatException e) {
            // ignore
        }
    }

    private void renderBindButtons(int startX, int startY) {
        List<SplitterConfig.SplitBind> binds = SplitterConfig.get().binds;
        int start = currentPage * BINDS_PER_PAGE;
        int end = Math.min(start + BINDS_PER_PAGE, binds.size());

        for (int i = start; i < end; i++) {
            final int index = i;
            SplitterConfig.SplitBind bind = binds.get(i);
            int row = i - start;
            int rowY = startY + (row * ROW_HEIGHT);

            this.addRenderableWidget(Button.builder(
                    Component.literal("§b" + bind.keyDisplay),
                    button -> {}
            ).bounds(startX, rowY, KEY_BTN_WIDTH, BTN_HEIGHT).build());

            EditBox editField = new EditBox(
                    this.font,
                    startX + KEY_BTN_WIDTH + GAP, rowY,
                    AMOUNT_WIDTH, BTN_HEIGHT,
                    Component.literal("" + bind.amount)
            );
            editField.setValue("" + bind.amount);
            editField.setResponder(text -> {
                try {
                    int newAmount = Integer.parseInt(text.trim());
                    if (newAmount > 0) { bind.amount = newAmount; SplitterConfig.save(); }
                } catch (NumberFormatException ignored) {}
            });
            this.addRenderableWidget(editField);

            this.addRenderableWidget(Button.builder(
                    Component.literal("§cX"),
                    button -> {
                        SplitterConfig.get().binds.remove(index);
                        SplitterConfig.save();
                        if (currentPage > 0 && currentPage * BINDS_PER_PAGE >= SplitterConfig.get().binds.size()) {
                            currentPage--;
                        }
                        this.clearWidgets();
                        this.init();
                    }
            ).bounds(startX + KEY_BTN_WIDTH + GAP + AMOUNT_WIDTH + GAP, rowY, DELETE_WIDTH, BTN_HEIGHT).build());
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;

        // status message
        if (hasConflict) {
            context.centeredText(this.font,
                    Component.literal("§c⚠ Key already used by: " + listeningKeyDisplay.replace("§c✗ ", "")),
                    centerX, this.height - 65, 0xFFFFFF);
        } else if (pendingKeyCode != -1 && !listeningForKey) {
            context.centeredText(this.font,
                    Component.literal("§a✔ Key set! Enter amount and press Add"),
                    centerX, this.height - 65, 0xFFFFFF);
        } else {
            context.centeredText(this.font,
                    Component.literal("§7Press §eSet Key §7then enter amount"),
                    centerX, this.height - 65, 0xFFFFFF);
        }

        // pagination
        List<SplitterConfig.SplitBind> binds = SplitterConfig.get().binds;
        int totalPages = (int) Math.ceil((double) binds.size() / BINDS_PER_PAGE);
        if (totalPages > 1) {
            context.centeredText(this.font,
                    Component.literal("§7" + (currentPage + 1) + " / " + totalPages),
                    centerX, this.height - 50, 0xFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}