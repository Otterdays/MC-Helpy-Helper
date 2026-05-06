package com.otterdays.helphelper.client;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;

/** Nearly full-window help browser with search and clickable rows. */
public final class HelpHelperScreen extends Screen {
    private final List<String> allCommands;

    private EditBox searchBox;
    private List<String> visible = List.of();

    private int listLeft;
    private int listTop;
    private int listRight;
    private int listBottom;
    private int lineHeight;
    private double scrollPixels;

    public HelpHelperScreen(List<String> commands) {
        super(Component.literal("Help"));
        this.allCommands = new ArrayList<>(commands);
    }

    @Override
    protected void init() {
        clearWidgets();
        visible = List.copyOf(allCommands);
        scrollPixels = 0.0;

        lineHeight = Math.max(14, minecraft.font.lineHeight + 6);
        computeListLayout();

        int searchH = Math.max(lineHeight + 4, 24);
        int closeLeft = Math.max(listLeft + 160, width - margin() - 124);
        int searchW = Math.max(140, closeLeft - listLeft - 16);

        searchBox = new EditBox(minecraft.font, listLeft, listTop - searchH - 10, searchW, searchH,
            Component.literal("Search"));
        searchBox.setResponder(this::applyFilter);
        searchBox.setHint(Component.literal(ChatFormatting.GRAY + "Search commands…"));
        addRenderableWidget(searchBox);

        Button close =
            Button.builder(Component.translatable("gui.done"), btn -> minecraft.setScreen(null)).bounds(closeLeft,
                    listTop - searchH - 10, 118,
                    searchH)
                .build();
        addRenderableWidget(close);
    }

    private int margin() {
        return Math.max(12, Math.min(width, height) / 42);
    }

    private void computeListLayout() {
        int m = margin();
        listLeft = m;
        listRight = Math.max(listLeft + 40, width - m);
        listTop = m + minecraft.font.lineHeight + 96;
        listBottom = Math.max(listTop + 60, height - m);

        clampScrollForViewport();
    }

    private void applyFilter(String raw) {
        String query = raw == null ? "" : raw.trim().toLowerCase();
        visible = query.isEmpty()
            ? List.copyOf(allCommands)
            : allCommands.stream().filter(c -> c.toLowerCase().contains(query)).toList();
        scrollPixels = 0.0;
        clampScrollForViewport();
    }

    @Override
    public void resize(int resizeWidth, int resizeHeight) {
        String saved = searchBox == null ? "" : searchBox.getValue();
        super.resize(resizeWidth, resizeHeight);
        if (searchBox != null) {
            searchBox.setValue(saved);
            applyFilter(saved);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        drawBackdrop(gfx);
        super.extractRenderState(gfx, mouseX, mouseY, partialTick);
        drawCommandRows(gfx, mouseX, mouseY);
    }

    private void drawBackdrop(GuiGraphicsExtractor gfx) {
        int m = margin();

        gfx.fill(m, m, width - m, height - m, ARGB.color(220, 14, 16, 24));
        gfx.fillGradient(m + 14, m + 8, width - m - 14, Math.min(height, m + 40), ARGB.color(255, 56, 60, 86),
            ARGB.color(255, 26, 30, 50));

        gfx.centeredText(
            minecraft.font,
            Component.literal("Help Helper").withStyle(ChatFormatting.GOLD),
            width / 2,
            m + 10,
            ARGB.color(255, 255, 226, 120));

        String counter =
            ChatFormatting.YELLOW.toString() + visible.size() + ChatFormatting.GRAY + " / " + allCommands.size()
                + ChatFormatting.WHITE + " commands";
        gfx.text(minecraft.font, counter, listLeft, m + minecraft.font.lineHeight + 42, ARGB.color(255, 200, 200, 200));

        outlineRect(gfx, listLeft - 4, listTop - 54, listRight + 4, listBottom + 4, ARGB.color(200, 90, 100, 150));
    }

    private void drawCommandRows(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        gfx.enableScissor(listLeft, listTop, listRight, listBottom);
        try {
            for (int i = 0; i < visible.size(); i++) {
                double y = listTop + (double) i * lineHeight - scrollPixels;
                if (y + lineHeight <= listTop || y >= listBottom) {
                    continue;
                }
                int yi = (int) Math.round(y);
                boolean hovered =
                    mouseX >= listLeft && mouseX < listRight && mouseY >= yi && mouseY < yi + lineHeight - 1;
                int rowBg =
                    hovered ? ARGB.color(110, 60, 80, 120) : ARGB.color(72, 32, 40, 72);
                gfx.fill(listLeft + 2, yi, listRight - 6, yi + lineHeight - 3, rowBg);

                String text = ellipsize(visible.get(i), listRight - listLeft - 24);
                int ty = yi + Math.max(0, (lineHeight - minecraft.font.lineHeight - 4) / 2);

                MutableComponent styled =
                    Component.literal(text).withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.UNDERLINE);
                gfx.text(minecraft.font, styled, listLeft + 8, ty, ARGB.color(255, 210, 245, 255), true);
            }
        } finally {
            gfx.disableScissor();
        }
    }

    private String ellipsize(String cmd, int maxPx) {
        if (minecraft.font.width(cmd) <= maxPx) {
            return cmd;
        }
        String ellipsis = "...";
        int avail = Math.max(maxPx - minecraft.font.width(ellipsis), minecraft.font.width(" "));
        int end = cmd.length();
        while (end > 1 && minecraft.font.width(cmd.substring(0, end)) > avail) {
            end--;
        }
        return cmd.substring(0, end).trim() + ellipsis;
    }

    private static void outlineRect(GuiGraphicsExtractor gfx, int x0, int y0, int x1, int y1, int color) {
        gfx.horizontalLine(x0, x1, y0, color);
        gfx.horizontalLine(x0, x1, y1, color);
        gfx.verticalLine(x0, y0, y1, color);
        gfx.verticalLine(x1, y0, y1, color);
    }

    private boolean isOverList(double mx, double my) {
        return mx >= listLeft && mx < listRight && my >= listTop && my < listBottom;
    }

    private int rowIndexAt(double mx, double my) {
        if (!isOverList(mx, my)) {
            return -1;
        }
        int rel = (int) (my - listTop + scrollPixels);
        if (rel < 0) {
            return -1;
        }
        return rel / lineHeight;
    }

    private double maxScroll() {
        int viewport = Math.max(0, listBottom - listTop);
        int total = visible.size() * lineHeight;
        return Math.max(0, total - viewport);
    }

    private void clampScrollForViewport() {
        scrollPixels = Math.max(0.0, Math.min(scrollPixels, maxScroll()));
    }

    private void runCommandRow(int index) {
        if (minecraft.player == null || index < 0 || index >= visible.size()) {
            return;
        }
        Screen.clickCommandAction(minecraft.player, visible.get(index), this);
        minecraft.setScreen(null);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (super.mouseClicked(event, doubled)) {
            return true;
        }

        int row = rowIndexAt(event.x(), event.y());
        if (row >= 0 && row < visible.size()) {
            runCommandRow(row);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
        if (isOverList(mouseX, mouseY) && maxScroll() > 0.0) {
            scrollPixels -= scrollDeltaY * lineHeight / 10.0;
            clampScrollForViewport();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
