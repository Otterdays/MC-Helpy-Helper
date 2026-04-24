package com.fpsmod.client;

import com.fpsmod.FpsMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class FpsHudConfig {
    private static final String KEY_SHOW = "showFpsHud";

    private final Path file;

    FpsHudConfig() {
        this.file = FabricLoader.getInstance().getConfigDir()
            .resolve(FpsMod.MOD_ID)
            .resolve("hud.properties");
    }

    boolean loadShowHud(boolean defaultValue) {
        if (!Files.isRegularFile(file)) {
            return defaultValue;
        }

        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            props.load(reader);
        } catch (IOException e) {
            FpsMod.LOGGER.warn("{} ⚠️ Failed to read HUD config, using defaults.", FpsMod.MOD_ID, e);
            return defaultValue;
        }

        return Boolean.parseBoolean(props.getProperty(KEY_SHOW, Boolean.toString(defaultValue)));
    }

    void saveShowHud(boolean showHud) {
        try {
            Files.createDirectories(file.getParent());
        } catch (IOException e) {
            FpsMod.LOGGER.warn("{} ⚠️ Failed to create config directory.", FpsMod.MOD_ID, e);
            return;
        }

        Properties props = new Properties();
        props.setProperty(KEY_SHOW, Boolean.toString(showHud));
        try (Writer writer = Files.newBufferedWriter(file)) {
            props.store(writer, "FPS HUD settings");
        } catch (IOException e) {
            FpsMod.LOGGER.warn("{} ⚠️ Failed to write HUD config.", FpsMod.MOD_ID, e);
        }
    }
}
