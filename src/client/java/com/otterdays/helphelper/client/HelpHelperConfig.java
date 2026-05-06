package com.otterdays.helphelper.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.otterdays.helphelper.HelpHelper;
import net.minecraft.client.Minecraft;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Runtime configuration for HelpHelper GUI layout and behavior.
 * Loaded from config/helphelper/config.json on startup; falls back to defaults if missing or invalid.
 */
public final class HelpHelperConfig {

    private static final String CONFIG_FILE = "config.json";
    private static final String CONFIG_DIR = "helphelper";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ── Layout ─────────────────────────────────────────────────────────────────
    public int margin = 12;
    public int scrollbarWidth = 8;
    public int scrollbarGap = 2;
    public int badgeWidth = 22;
    public int minCategoryWidth = 46;
    public int maxCategoryWidth = 92;
    public int detailPanelWidth = 190;
    public int detailPanelGap = 10;
    public int compactLineHeight = 14;
    public int roomyLineHeight = 20;
    public int compactVerticalPadding = 5;
    public int roomyVerticalPadding = 10;
    public double pageScrollFactor = 0.88;

    // ── Control dimensions ──────────────────────────────────────────────────────
    public int controlHeight = 22;
    public int buttonGap = 5;
    public int closeButtonWidth = 66;
    public int densityButtonWidth = 78;
    public int actionButtonWidth = 104;
    public int searchMinWidth = 100;

    // ── Colors (ARGB format: 0xAABBGGRR) ───────────────────────────────────────
    // Each entry is a string like "0xFFaabbcc" for readability in JSON.
    public String colorRiskyText = "0xFFa4e8e2";      // cyan-ish for vanilla commands
    public String colorModdedText = "0xFFd6c6ff";     // lavender for mod commands
    public String colorRiskyWarning = "0xFFffcca4";    // orange for risky commands
    public String colorFavoriteStar = "0xFFffe278";    // gold for favorites
    public String colorDescription = "0xFFd2d6e2";     // muted blue-gray for description
    public String colorSourceVanilla = "0xFFa6f4e2";   // mint for vanilla source
    public String colorSourceModded = "0xFFdcc6ff";    // light purple for mod source
    public String colorSourceRisky = "0xFFffaa76";     // salmon for risky
    public String colorScrollbar = "0xB4 528560";      // semi-transparent border
    public String colorPanelBackground = "0x78162234"; // semi-transparent dark

    // ── Limits ─────────────────────────────────────────────────────────────────
    public int maxRecentCommands = 12;
    public int maxFavorites = 100;

    // ── Keyboard shortcuts ─────────────────────────────────────────────────────
    public int keyDown = 264;
    public int keyUp = 265;
    public int keyPageUp = 266;
    public int keyPageDown = 267;
    public int keyHome = 268;
    public int keyEnd = 269;
    public int keyEnter = 257;
    public int keyKpEnter = 335;
    public int keyCycleAction = 67;      // C
    public int keyToggleCompact = 68;     // D
    public int keyToggleFavorite = 70;    // F

    // ── Category color map (JSON-friendly: category -> color) ──────────────────
    public String[] categoryColors = {
        "Chat,Social,0xFF5eaeDC",    // blue
        "World,Worldgen,0xFF6abe74", // green
        "Player,Movement,Transport,0xFFeeb458", // yellow
        "Build,Inventory,Storage,0xFFd29660",    // orange
        "Entities,0xFFde7078",       // red
        "Utility,Debug,Advanced,0xFFb096e8",      // purple
        "Admin,Server,0xFFee7660",   // salmon
        "Visual,0xFF76d2be",         // teal
        "default,0xFF969aa2"         // gray fallback
    };

    // ── Cached singleton ───────────────────────────────────────────────────────
    private static HelpHelperConfig INSTANCE = null;

    /**
     * Load config once; subsequent calls return the same instance.
     */
    public static synchronized HelpHelperConfig get() {
        if (INSTANCE == null) {
            INSTANCE = loadOrCreate();
        }
        return INSTANCE;
    }

    /**
     * Force-reload from disk (useful for config-editing UIs in future).
     */
    public static synchronized void reload() {
        INSTANCE = loadOrCreate();
    }

    private static HelpHelperConfig loadOrCreate() {
        Path path = configPath();
        if (Files.isRegularFile(path)) {
            try {
                String json = Files.readString(path);
                HelpHelperConfig parsed = GSON.fromJson(json, HelpHelperConfig.class);
                if (parsed != null) {
                    HelpHelper.LOGGER.info("HelpHelper config loaded from {}", path);
                    return parsed;
                }
            } catch (IOException | JsonSyntaxException e) {
                HelpHelper.LOGGER.warn("Failed to parse config at {} — using defaults: {}", path, e.getMessage());
            }
        }
        HelpHelper.LOGGER.info("Using default HelpHelper config (no config file found at {})", path);
        return new HelpHelperConfig();
    }

    private static Path configPath() {
        return Minecraft.getInstance().gameDirectory.toPath()
            .resolve("config")
            .resolve(CONFIG_DIR)
            .resolve(CONFIG_FILE);
    }

    // ── Convenience helpers ──────────────────────────────────────────────────────

    public int lineHeight(boolean compact) {
        return compact ? compactLineHeight : roomyLineHeight;
    }

    public int verticalPadding(boolean compact) {
        return compact ? compactVerticalPadding : roomyVerticalPadding;
    }

    /** Parse a color string like "0xFFaabbcc" or "0xFF aabb cc" into an ARGB int. */
    public int parseColor(String colorStr) {
        if (colorStr == null || colorStr.isBlank()) {
            return 0xFF969aa2;
        }
        String hex = colorStr.replace(" ", "").replace("0x", "").replace("0X", "");
        try {
            return (int) Long.parseLong(hex, 16);
        } catch (NumberFormatException e) {
            return 0xFF969aa2;
        }
    }

    /** Returns ARGB color for a given category, using the categoryColors map. */
    public int categoryColor(String category) {
        if (categoryColors != null) {
            for (String entry : categoryColors) {
                String[] parts = entry.split(",", 3);
                if (parts.length >= 3) {
                    for (int i = 0; i < parts.length - 1; i++) {
                        if (parts[i].trim().equalsIgnoreCase(category)) {
                            return parseColor(parts[parts.length - 1]);
                        }
                    }
                }
            }
        }
        return parseColor("0xFF969aa2"); // fallback gray
    }

    /** Export current config as JSON string (for saving edits). */
    public String toJson() {
        return GSON.toJson(this);
    }
}