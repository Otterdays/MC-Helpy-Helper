package com.otterdays.helphelper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class HelpHelperLayoutTest {
    @Test
    void viewportMatrixKeepsCommandListUsableAndDetailsSeparated() {
        int[][] viewports = {
            {320, 240}, {427, 240}, {640, 360}, {854, 480}, {1280, 720}, {1920, 1080}
        };
        int[] fontHeights = {9, 14, 20};
        for (int[] viewport : viewports) {
            for (int fontHeight : fontHeights) {
                HelpHelperLayoutMath.Layout layout = HelpHelperLayoutMath.plan(
                    viewport[0], viewport[1], 12, fontHeight, 22, 190, 10);
                assertTrue(layout.listUsable(), "list must stay usable at " + viewport[0] + "x" + viewport[1]);
                assertTrue(layout.detailsSeparated(), "details must not overlap list at " + viewport[0] + "x" + viewport[1]);
            }
        }
    }
}
