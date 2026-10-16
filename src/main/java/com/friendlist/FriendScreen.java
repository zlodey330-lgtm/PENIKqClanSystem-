package com.friendlist;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class FriendScreen extends Screen {
    private static final int W = 180;
    private static final int H = 176;
    private static final int ROWS = 6;
    private static final int ROW_H = 20;

    private int left;
    private int top;
    private int scroll = 0;
    private TextFieldWidget input;
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

        rebuildList();
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

        super.render(ctx, mouseX, mouseY, delta);
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
