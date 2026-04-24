package com.fpsmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FpsMod implements ModInitializer {
    public static final String MOD_ID = "fpsmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final String EMOJI_DEBUG = "🔍";
    private static final String EMOJI_OK = "✅";
    private static final String EMOJI_WARN = "⚠️";

    @Override
    public void onInitialize() {
        logDebug("Starting mod initialization...");

        FabricLoader loader = FabricLoader.getInstance();
        String gameVersion = loader.getModContainer("minecraft")
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");

        String modVersion = loader.getModContainer(MOD_ID)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("dev");

        logDebug("Environment: java=" + System.getProperty("java.version")
            + ", minecraft=" + gameVersion
            + ", modVersion=" + modVersion);

        // Keep this startup heartbeat obvious so template users can quickly confirm load order.
        LOGGER.info("{} {} loaded and ready.", EMOJI_OK, MOD_ID);

        if (!loader.isDevelopmentEnvironment()) {
            LOGGER.warn("{} Running outside dev environment.", EMOJI_WARN);
        } else {
            logDebug("Running in development environment.");
        }
    }

    private static void logDebug(String message) {
        LOGGER.info("{} {}", EMOJI_DEBUG, message);
    }
}
