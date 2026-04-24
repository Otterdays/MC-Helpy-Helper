package com.fpsmod.client;

import com.fpsmod.FpsMod;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class FpsHudOverlay {
    private static final Identifier OVERLAY_ID = Identifier.fromNamespaceAndPath(FpsMod.MOD_ID, "fps_overlay");
    private static final int PAD = 6;
    private static final int BUTTON_W = 52;
    private static final int BUTTON_H = 14;

    private static final FpsHudConfig CONFIG = new FpsHudConfig();
    private static volatile boolean showHud = true;
    private static volatile int displayedFps;
    private static volatile long lastFpsSampleMs;
    private static volatile int buttonScreenX;
    private static volatile int buttonScreenY;
    private static volatile int buttonScreenW = BUTTON_W;
    private static volatile int buttonScreenH = BUTTON_H;

    private FpsHudOverlay() {
    }

    public static void register() {
        showHud = CONFIG.loadShowHud(true);

        HudElementRegistry.attachElementBefore(
            VanillaHudElements.MISC_OVERLAYS,
            OVERLAY_ID,
            FpsHudOverlay::render
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) {
                return;
            }

            long now = Util.getMillis();
            if (now - lastFpsSampleMs >= 1000L) {
                lastFpsSampleMs = now;
                displayedFps = client.getFps();
            }

            handleClick(client);
        });
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null || client.options.hideGui) {
            return;
        }

        int x = PAD;
        int y = PAD;

        boolean hovered = isMouseOverButton(client, x, y);
        int buttonBg = hovered ? ARGB.colorFromFloat(0.55f, 0.12f, 0.12f, 0.12f)
            : ARGB.colorFromFloat(0.45f, 0.08f, 0.08f, 0.08f);
        int border = ARGB.colorFromFloat(1.0f, 0.31f, 0.31f, 0.31f);

        graphics.fill(x, y, x + BUTTON_W, y + BUTTON_H, buttonBg);
        graphics.outline(x, y, BUTTON_W, BUTTON_H, border);

        String buttonLabel = showHud ? "Hide FPS" : "Show FPS";
        int labelColor = ARGB.color(255, 224, 224, 224);
        graphics.text(client.font, buttonLabel, x + 4, y + 3, labelColor, true);

        buttonScreenX = x;
        buttonScreenY = y;
        buttonScreenW = BUTTON_W;
        buttonScreenH = BUTTON_H;

        if (!showHud) {
            return;
        }

        String fpsLine = "FPS: " + displayedFps;
        int textX = x + BUTTON_W + 6;
        int textY = y + 3;
        int shadow = ARGB.color(255, 0, 0, 0);
        graphics.text(client.font, fpsLine, textX + 1, textY + 1, shadow, false);
        int fpsColor = ARGB.color(255, 200, 255, 200);
        graphics.text(client.font, fpsLine, textX, textY, fpsColor, false);
    }

    private static boolean isMouseOverButton(Minecraft client, int x, int y) {
        var mouse = client.mouseHandler;
        double scaledX = MouseHandler.getScaledXPos(client.getWindow(), mouse.xpos());
        double scaledY = MouseHandler.getScaledYPos(client.getWindow(), mouse.ypos());
        return scaledX >= x && scaledX <= x + BUTTON_W && scaledY >= y && scaledY <= y + BUTTON_H;
    }

    private static void handleClick(Minecraft client) {
        if (client.screen != null) {
            return;
        }

        var mouse = client.mouseHandler;
        if (!mouse.isLeftPressed()) {
            return;
        }

        int x = buttonScreenX;
        int y = buttonScreenY;
        int w = buttonScreenW;
        int h = buttonScreenH;

        double scaledX = MouseHandler.getScaledXPos(client.getWindow(), mouse.xpos());
        double scaledY = MouseHandler.getScaledYPos(client.getWindow(), mouse.ypos());

        if (scaledX >= x && scaledX <= x + w && scaledY >= y && scaledY <= y + h) {
            showHud = !showHud;
            CONFIG.saveShowHud(showHud);
            FpsMod.LOGGER.info("{} 🔁 FPS HUD {}", FpsMod.MOD_ID, showHud ? "enabled" : "disabled");
        }
    }
}
