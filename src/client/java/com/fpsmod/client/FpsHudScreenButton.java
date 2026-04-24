package com.fpsmod.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FpsHudScreenButton {
    private static final int X = 6;
    private static final int Y = 6;
    private static final int WIDTH = 70;
    private static final int HEIGHT = 20;

    private FpsHudScreenButton() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (client.level == null || client.player == null) {
                return;
            }
            addToggleButton(screen);
        });
    }

    private static void addToggleButton(Screen screen) {
        Button button = Button.builder(currentLabel(), btn -> {
            FpsHudOverlay.toggleHud();
            btn.setMessage(currentLabel());
        }).bounds(X, Y, WIDTH, HEIGHT).build();

        Screens.getWidgets(screen).add(button);
    }

    private static Component currentLabel() {
        return Component.literal(FpsHudOverlay.isHudShown() ? "Hide FPS" : "Show FPS");
    }
}
