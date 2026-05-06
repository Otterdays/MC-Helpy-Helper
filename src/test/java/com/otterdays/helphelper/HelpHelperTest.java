package com.otterdays.helphelper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class HelpHelperTest {
    @Test
    void modIdStaysStableForMetadataAndNetworking() {
        assertEquals("helphelper", HelpHelper.MOD_ID);
    }
}
