package com.fpsmod;

import com.fpsmod.client.FpsHudOverlay;
import net.fabricmc.api.ClientModInitializer;

public class FpsModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FpsHudOverlay.register();
    }
}
