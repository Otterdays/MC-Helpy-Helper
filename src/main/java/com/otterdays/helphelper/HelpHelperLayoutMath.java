package com.otterdays.helphelper;

public final class HelpHelperLayoutMath {
    private HelpHelperLayoutMath() {
    }

    public static Layout plan(int width, int height, int margin, int fontLineHeight, int controlHeight,
        int detailPanelWidth, int detailPanelGap) {
        int m = Math.max(margin, Math.min(width, height) / 42);
        int controlH = Math.max(controlHeight, fontLineHeight + 10);
        int headerBlockHeight = (fontLineHeight * 2) + 34;
        int listLeft = m;
        int available = Math.max(40, width - (m * 2));
        int details = available >= 460 ? Math.min(detailPanelWidth, available / 3) : 0;
        int listRight = Math.max(listLeft + 40, width - m - details - (details > 0 ? detailPanelGap : 0));
        int controlRowsReserve = 5;
        int topSpace = headerBlockHeight + (controlRowsReserve * controlH) + 20;
        int maxTopSpace = Math.max(140, height - m - 84);
        topSpace = Math.min(topSpace, maxTopSpace);
        int listTop = m + topSpace;
        int listBottom = Math.max(listTop + 60, height - m);
        int detailLeft = listRight + detailPanelGap;
        int detailRight = width - m;
        return new Layout(listLeft, listTop, listRight, listBottom, detailLeft, detailRight, details > 0);
    }

    public record Layout(int listLeft, int listTop, int listRight, int listBottom,
        int detailLeft, int detailRight, boolean detailsVisible) {
        public boolean listUsable() {
            return listRight - listLeft >= 40 && listBottom - listTop >= 60;
        }

        public boolean detailsSeparated() {
            return !detailsVisible || detailLeft >= listRight;
        }
    }
}
