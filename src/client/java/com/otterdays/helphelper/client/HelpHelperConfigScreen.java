package com.otterdays.helphelper.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public final class HelpHelperConfigScreen extends Screen {
    private final Screen parent;
    private final HelpHelperConfig config;

    public HelpHelperConfigScreen(Screen parent) {
        super(Component.literal("Help Helper Config"));
        this.parent = parent;
        this.config = HelpHelperConfig.get();
    }

    @Override
    protected void init() {
        clearWidgets();
        int x0 = panelLeft();
        int y0 = panelTop();
        int contentX = x0 + 12;
        int contentW = panelWidth() - 24;
        int buttonH = controlHeight();
        int rowGap = 4;
        int y = y0 + 30;

        addToggle(contentX, y, contentW, buttonH, "Confirm risky", config.confirmRiskyCommands,
            value -> config.confirmRiskyCommands = value);
        y += buttonH + rowGap;
        addToggle(contentX, y, contentW, buttonH, "Fuzzy search", config.enableFuzzySearch,
            value -> config.enableFuzzySearch = value);
        y += buttonH + rowGap;
        addToggle(contentX, y, contentW, buttonH, "Syntax preview", config.showSyntaxPreview,
            value -> config.showSyntaxPreview = value);
        y += buttonH + rowGap;
        addStepper(contentX, y, contentW, buttonH, "Recents", config.maxRecentCommands, 1, 40,
            value -> config.maxRecentCommands = value);
        y += buttonH + rowGap;
        addStepper(contentX, y, contentW, buttonH, "Favorites", config.maxFavorites, 1, 500,
            value -> config.maxFavorites = value);
        y += buttonH + 10;

        int doneW = Math.min(120, contentW);
        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> closeAndSave())
            .bounds(x0 + (panelWidth() - doneW) / 2, y, doneW, buttonH).build());
    }

    private int panelWidth() {
        return Math.min(300, width - 28);
    }

    private int controlHeight() {
        return Math.max(22, minecraft.font.lineHeight + 10);
    }

    private int panelHeight() {
        int buttonH = controlHeight();
        int contentH = (buttonH * 6) + (4 * 4) + 44;
        return Math.min(height - 28, Math.max(210, contentH));
    }

    private int panelLeft() {
        return (width - panelWidth()) / 2;
    }

    private int panelTop() {
        return Math.max(14, height / 2 - panelHeight() / 2);
    }

    private void addToggle(int x, int y, int width, int height, String label, boolean value, BooleanSetter setter) {
        addRenderableWidget(Button.builder(Component.literal(label + ": " + (value ? "On" : "Off")), btn -> {
            setter.set(!value);
            config.save();
            rebuildWidgets();
        }).bounds(x, y, width, height).build());
    }

    private void addStepper(int x, int y, int width, int height, String label, int value, int min, int max,
        IntSetter setter) {
        int gap = 4;
        int stepW = Math.min(Math.max(26, height), Math.max(26, (width - 80) / 4));
        int labelW = Math.max(60, width - (stepW * 2) - (gap * 2));
        addRenderableWidget(Button.builder(Component.literal("-"), btn -> {
            setter.set(Math.max(min, value - 1));
            config.save();
            rebuildWidgets();
        }).bounds(x, y, stepW, height).build());
        addRenderableWidget(Button.builder(Component.literal(label + ": " + value), btn -> { })
            .bounds(x + stepW + gap, y, labelW, height).build());
        addRenderableWidget(Button.builder(Component.literal("+"), btn -> {
            setter.set(Math.min(max, value + 1));
            config.save();
            rebuildWidgets();
        }).bounds(x + stepW + gap + labelW + gap, y, stepW, height).build());
    }

    private void closeAndSave() {
        config.save();
        minecraft.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        int panelW = panelWidth();
        int panelH = panelHeight();
        int x0 = panelLeft();
        int y0 = panelTop();
        int x1 = x0 + panelW;
        int y1 = y0 + panelH;
        gfx.fill(x0, y0, x1, y1, ARGB.color(226, 12, 15, 22));
        outlineRect(gfx, x0, y0, x1, y1, ARGB.color(190, 90, 100, 150));
        gfx.centeredText(minecraft.font, Component.literal("Help Helper Config").withStyle(ChatFormatting.GOLD),
            width / 2, y0 + 12, ARGB.color(255, 255, 226, 120));
        super.extractRenderState(gfx, mouseX, mouseY, partialTick);
    }

    private static void outlineRect(GuiGraphicsExtractor gfx, int x0, int y0, int x1, int y1, int color) {
        gfx.horizontalLine(x0, x1, y0, color);
        gfx.horizontalLine(x0, x1, y1, color);
        gfx.verticalLine(x0, y0, y1, color);
        gfx.verticalLine(x1, y0, y1, color);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @FunctionalInterface
    private interface BooleanSetter {
        void set(boolean value);
    }

    @FunctionalInterface
    private interface IntSetter {
        void set(int value);
    }
}



