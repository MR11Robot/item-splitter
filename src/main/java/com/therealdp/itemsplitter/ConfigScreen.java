package com.therealdp.itemsplitter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import java.util.List;

public class ConfigScreen extends Screen {

    private TextFieldWidget amountField;
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
        super(Text.literal("Item Splitter Config"));
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

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal(keyBtnText),
                button -> {
                    listeningForKey = true;
                    listeningKeyDisplay = "";
                    pendingKeyCode = -1;
                    this.clearChildren();
                    this.init();
                }
        ).dimensions(rowStartX, controlsY, KEY_BTN_WIDTH, BTN_HEIGHT).build());

        amountField = new TextFieldWidget(
                this.textRenderer,
                rowStartX + KEY_BTN_WIDTH + GAP, controlsY,
                AMOUNT_WIDTH, BTN_HEIGHT,
                Text.literal("Amt")
        );
        amountField.setPlaceholder(Text.literal("Amt"));
        this.addDrawableChild(amountField);

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§aAdd"),
                button -> addBind()
        ).dimensions(rowStartX + KEY_BTN_WIDTH + GAP + AMOUNT_WIDTH + GAP, controlsY, DELETE_WIDTH + 20, BTN_HEIGHT).build());

        renderBindButtons(rowStartX, bindsStartY);

        List<SplitterConfig.SplitBind> binds = SplitterConfig.get().binds;
        int totalPages = (int) Math.ceil((double) binds.size() / BINDS_PER_PAGE);
        if (totalPages > 1) {
            int pageY = this.height - 28;
            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("< Prev"),
                    button -> {
                        if (currentPage > 0) { currentPage--; this.clearChildren(); this.init(); }
                    }
            ).dimensions(centerX - 65, pageY, 55, BTN_HEIGHT).build());

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Next >"),
                    button -> {
                        if (currentPage < totalPages - 1) { currentPage++; this.clearChildren(); this.init(); }
                    }
            ).dimensions(centerX + 10, pageY, 55, BTN_HEIGHT).build());
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (listeningForKey) {
            if (input.isEscape()) {
                listeningForKey = false;
                listeningKeyDisplay = "";
                pendingKeyCode = -1;
            } else {
                pendingKeyCode = input.key();
                String raw = InputUtil.fromKeyCode(input).getTranslationKey()
                        .replace("key.keyboard.", "")
                        .replace("keypad.", "Numpad ")
                        .replace(".", " ");
                listeningKeyDisplay = raw.substring(0, 1).toUpperCase() + raw.substring(1);
                listeningForKey = false;
            }
            this.clearChildren();
            this.init();
            return true;
        }
        return super.keyPressed(input);
    }

    private String getConflictingBind(int keyCode) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options == null) return null;
        for (net.minecraft.client.option.KeyBinding binding : client.options.allKeys) {
            if (binding.getDefaultKey().getCode() == keyCode ||
                    binding.getBoundKeyTranslationKey().equals(
                            InputUtil.Type.KEYSYM.createFromCode(keyCode).getTranslationKey())) {
                return binding.getBoundKeyLocalizedText().getString();
            }
        }
        return null;
    }

    private void addBind() {
        try {
            int amount = Integer.parseInt(amountField.getText().trim());
            if (amount <= 0 || pendingKeyCode == -1) return;

            String conflict = getConflictingBind(pendingKeyCode);
            if (conflict != null) {
                hasConflict = true;
                listeningKeyDisplay = "§c✗ " + conflict;
                SplitterSound.playFail();
                this.clearChildren();
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

            this.clearChildren();
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

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("§b" + bind.keyDisplay),
                    button -> {}
            ).dimensions(startX, rowY, KEY_BTN_WIDTH, BTN_HEIGHT).build());

            TextFieldWidget editField = new TextFieldWidget(
                    this.textRenderer,
                    startX + KEY_BTN_WIDTH + GAP, rowY,
                    AMOUNT_WIDTH, BTN_HEIGHT,
                    Text.literal("" + bind.amount)
            );
            editField.setText("" + bind.amount);
            editField.setChangedListener(text -> {
                try {
                    int newAmount = Integer.parseInt(text.trim());
                    if (newAmount > 0) { bind.amount = newAmount; SplitterConfig.save(); }
                } catch (NumberFormatException ignored) {}
            });
            this.addDrawableChild(editField);

            this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("§cX"),
                    button -> {
                        SplitterConfig.get().binds.remove(index);
                        SplitterConfig.save();
                        if (currentPage > 0 && currentPage * BINDS_PER_PAGE >= SplitterConfig.get().binds.size()) {
                            currentPage--;
                        }
                        this.clearChildren();
                        this.init();
                    }
            ).dimensions(startX + KEY_BTN_WIDTH + GAP + AMOUNT_WIDTH + GAP, rowY, DELETE_WIDTH, BTN_HEIGHT).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);

        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;

        // status message
        if (hasConflict) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("§c⚠ Key already used by: " + listeningKeyDisplay.replace("§c✗ ", "")),
                    centerX, this.height - 65, 0xFFFFFF);
        } else if (pendingKeyCode != -1 && !listeningForKey) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("§a✔ Key set! Enter amount and press Add"),
                    centerX, this.height - 65, 0xFFFFFF);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("§7Press §eSet Key §7then enter amount"),
                    centerX, this.height - 65, 0xFFFFFF);
        }

        // pagination
        List<SplitterConfig.SplitBind> binds = SplitterConfig.get().binds;
        int totalPages = (int) Math.ceil((double) binds.size() / BINDS_PER_PAGE);
        if (totalPages > 1) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("§7" + (currentPage + 1) + " / " + totalPages),
                    centerX, this.height - 50, 0xFFFFFF);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}