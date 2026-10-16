package com.friendlist;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FriendScreen extends Screen {
    private static final int W = 180;
    private static final int H = 236;
    private static final int ROWS = 6;
    private static final int ROW_H = 20;

    private static final int[] SWATCHES = {
            0xA06540, 0xB02E26, 0xF9801D, 0xFED83D, 0x80C71F,
            0x169C9C, 0x3C44AA, 0x8932B8, 0xF38BAA, 0xF9FFFE
    };
    private static final int SW = 14;
    private static final int SW_GAP = 2;
    private static final Pattern HEX = Pattern.compile("[0-9A-Fa-f]{6}");

    private int left;
    private int top;
    private int scroll = 0;
    private TextFieldWidget input;
    private TextFieldWidget hex;
    private final List<ButtonWidget> removeButtons = new ArrayList<>();

    public FriendScreen() {
        super(Text.translatable("gui.friendlist.title"));
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;

        input = new TextFieldWidget(textRenderer, left + 8, top + 22, 112, 18,
                Text.translatable("gui.friendlist.hint"));
        input.setMaxLength(16);
        input.setPlaceholder(Text.translatable("gui.friendlist.hint"));
        addDrawableChild(input);
        setInitialFocus(input);

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.friendlist.add"), b -> addFromInput())
                .dimensions(left + 8 + 112 + 4, top + 21, 48, 20)
                .build());

        hex = new TextFieldWidget(textRenderer, left + 8, top + 212, 72, 18,
                Text.translatable("gui.friendlist.color"));
        hex.setMaxLength(7);
        hex.setText(String.format("%06X", FriendManager.getColor()));
        hex.setChangedListener(this::onHexChanged);
        addDrawableChild(hex);

        rebuildList();
    }

    private void onHexChanged(String s) {
        if (s.startsWith("#")) s = s.substring(1);
        if (HEX.matcher(s).matches()) {
            FriendManager.setColor(Integer.parseInt(s, 16));
        }
    }

    private void addFromInput() {
        if (FriendManager.add(input.getText())) {
            input.setText("");
        }
        rebuildList();
    }

    private void rebuildList() {
        for (ButtonWidget b : removeButtons) {
            remove(b);
        }
        removeButtons.clear();

        List<String> names = FriendManager.list();
        int maxScroll = Math.max(0, names.size() - ROWS);
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        for (int i = 0; i < ROWS; i++) {
            int idx = scroll + i;
            if (idx >= names.size()) break;
            String name = names.get(idx);
            ButtonWidget btn = ButtonWidget.builder(Text.translatable("gui.friendlist.remove"), b -> {
                        FriendManager.remove(name);
                        rebuildList();
                    })
                    .dimensions(left + W - 8 - 18, top + 48 + i * ROW_H + 1, 18, 18)
                    .build();
            removeButtons.add(btn);
            addDrawableChild(btn);
        }
    }

    private void drawColorBox(DrawContext ctx, int x, int y, int size, int rgb, boolean selected) {
        ctx.fill(x - 1, y - 1, x + size + 1, y + size + 1, selected ? 0xFFFFFFFF : 0xFF101010);
        ctx.fill(x, y, x + size, y + size, 0xFF000000 | rgb);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Border + dark gray panel
        ctx.fill(left - 1, top - 1, left + W + 1, top + H + 1, 0xFF151515);
        ctx.fill(left, top, left + W, top + H, 0xF0303030);

        ctx.drawTextWithShadow(textRenderer, title, left + 8, top + 7, 0xFFFFFF);

        List<String> names = FriendManager.list();
        if (names.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, Text.translatable("gui.friendlist.empty"),
                    left + 10, top + 54, 0x808080);
        }
        for (int i = 0; i < ROWS; i++) {
            int idx = scroll + i;
            if (idx >= names.size()) break;
            int y = top + 48 + i * ROW_H;
            if (i % 2 == 0) {
                ctx.fill(left + 4, y, left + W - 4, y + ROW_H, 0x18FFFFFF);
            }
            ctx.drawTextWithShadow(textRenderer, names.get(idx), left + 10, y + 6, 0xE0E0E0);
        }

        // Color section
        ctx.fill(left + 6, top + 172, left + W - 6, top + 173, 0x40FFFFFF);
        ctx.drawTextWithShadow(textRenderer, Text.translatable("gui.friendlist.color"), left + 8, top + 178, 0xFFFFFF);

        int current = FriendManager.getColor();
        for (int i = 0; i < SWATCHES.length; i++) {
            int x = left + 8 + i * (SW + SW_GAP);
            drawColorBox(ctx, x, top + 190, SW, SWATCHES[i], SWATCHES[i] == current);
        }
        drawColorBox(ctx, left + 8 + 72 + 6, top + 212, 18, current, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = 0; i < SWATCHES.length; i++) {
                int x = left + 8 + i * (SW + SW_GAP);
                int y = top + 190;
                if (mouseX >= x && mouseX < x + SW && mouseY >= y && mouseY < y + SW) {
                    FriendManager.setColor(SWATCHES[i]);
                    hex.setText(String.format("%06X", SWATCHES[i]));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scroll -= (int) Math.signum(amount);
        rebuildList();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Enter / Numpad Enter
        if ((keyCode == 257 || keyCode == 335) && input.isFocused()) {
            addFromInput();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
