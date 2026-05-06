package com.otterdays.helphelper;

import com.otterdays.helphelper.network.OpenHelpPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelpHelper implements ModInitializer {
    public static final String MOD_ID = "helphelper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(OpenHelpPayload.TYPE, OpenHelpPayload.CODEC);
        HelpHelperCommands.register();
        LOGGER.info("Help Helper registered /help GUI channel");
    }
}
