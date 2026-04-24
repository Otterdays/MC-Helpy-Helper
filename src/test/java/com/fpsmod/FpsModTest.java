package com.fpsmod;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FpsModTest {
    @Test
    void modIdStaysStableForMetadataAndLogging() {
        assertEquals("fpsmod", FpsMod.MOD_ID);
    }
}
