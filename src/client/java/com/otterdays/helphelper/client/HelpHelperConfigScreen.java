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
        int center = width / 2;
        int y = Math.max(44, height / 2 - 78);
        addToggle(center, y, "Confirm risky", config.confirmRiskyCommands, value -> config.confirmRiskyCommands = value);
        y += 26;
        addToggle(center, y, "Fuzzy search", config.enableFuzzySearch, value -> config.enableFuzzySearch = value);
        y += 26;
        addToggle(center, y, "Syntax preview", config.showSyntaxPreview, value -> config.showSyntaxPreview = value);
        y += 26;
        addStepper(center, y, "Recents", config.maxRecentCommands, 1, 40, value -> config.maxRecentCommands = value);
        y += 26;
        addStepper(center, y, "Favorites", config.maxFavorites, 1, 500, value -> config.maxFavorites = value);
        y += 34;
        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> closeAndSave())
            .bounds(center - 50, y, 100, 22).build());
    }

    private void addToggle(int center, int y, String label, boolean value, BooleanSetter setter) {
        addRenderableWidget(Button.builder(Component.literal(label + ": " + (value ? "On" : "Off")), btn -> {
            setter.set(!value);
            config.save();
            rebuildWidgets();
        }).bounds(center - 90, y, 180, 22).build());
    }

    private void addStepper(int center, int y, String label, int value, int min, int max, IntSetter setter) {
        addRenderableWidget(Button.builder(Component.literal("-"), btn -> {
            setter.set(Math.max(min, value - 1));
            config.save();
            rebuildWidgets();
        }).bounds(center - 90, y, 26, 22).build());
        addRenderableWidget(Button.builder(Component.literal(label + ": " + value), btn -> { })
            .bounds(center - 58, y, 116, 22).build());
        addRenderableWidget(Button.builder(Component.literal("+"), btn -> {
            setter.set(Math.min(max, value + 1));
            config.save();
            rebuildWidgets();
        }).bounds(center + 64, y, 26, 22).build());
    }

    private void closeAndSave() {
        config.save();
        minecraft.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        int panelW = Math.min(260, width - 28);
        int panelH = Math.min(230, height - 28);
        int x0 = (width - panelW) / 2;
        int y0 = Math.max(14, height / 2 - panelH / 2);
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



