package com.otterdays.helphelper.client;

import com.otterdays.helphelper.network.OpenHelpPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class HelpHelperClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(OpenHelpPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> context.client().setScreen(new HelpHelperScreen(payload.commands())));
        });
    }
}
