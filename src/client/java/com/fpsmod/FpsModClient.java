package com.fpsmod;

import com.fpsmod.client.FpsHudOverlay;
import com.fpsmod.client.FpsHudScreenButton;
import net.fabricmc.api.ClientModInitializer;

public class FpsModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FpsHudOverlay.register();
        FpsHudScreenButton.register();
    }
}
