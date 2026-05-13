package com.otterdays.helphelper.client;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Locale;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.otterdays.helphelper.HelpHelperLayoutMath;
import com.otterdays.helphelper.network.OpenHelpPayload.CommandEntry;

/** Nearly full-window help browser with search, filtering, and flexible command actions. */
public final class HelpHelperScreen extends Screen {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final HelpHelperConfig CFG = HelpHelperConfig.get();
    private static final List<String> CATEGORY_ORDER = List.of("Server/Modded", "Chat", "Social", "World",
        "Worldgen", "Player", "Movement", "Build", "Inventory", "Storage", "Entities", "Utility", "Debug",
        "Admin", "Server", "Visual", "Transport", "Advanced");
    private final List<CommandCatalog.CommandRow> allCommands;
    private final List<String> categories;
    private final Map<String, Integer> categoryCounts;
    private final Set<String> favoriteCommands;
    private final List<String> recentCommands;

    private EditBox searchBox;
    private List<CommandCatalog.CommandRow> visible = List.of();
    private List<PresetHit> presetHits = List.of();
    private QuickFilter quickFilter = QuickFilter.ALL;
    private SortMode sortMode = SortMode.TOP;
    private String selectedCategory = "";
    private ClickAction clickAction = ClickAction.RUN;
    private boolean compactRows;
    private int selectedIndex = -1;
    private boolean draggingScrollbar;
    private boolean showHelpOverlay;
    private int hoveredIndex = -1;
    private String feedbackMessage = "";
    private long feedbackUntilMillis;
    private double scrollbarGrabOffset;
    private Path stateFile;

    private int listLeft;
    private int listTop;
    private int listRight;
    private int listBottom;
    private int lineHeight;
    private double scrollPixels;

    public HelpHelperScreen(List<CommandEntry> commands) {
        super(Component.literal("Help"));
        this.stateFile = defaultStateFile();
        Map<String, CommandCatalog.CommandRow> uniqueCommands = new LinkedHashMap<>();
        for (CommandEntry command : commands) {
            CommandCatalog.CommandRow row = CommandCatalog.row(command.command(), command.root(),
                command.syntax(), command.originHint());
            uniqueCommands.putIfAbsent(row.command(), row);
        }
        this.allCommands = List.copyOf(uniqueCommands.values());
        this.categoryCounts = collectCategories(allCommands);
        this.categories = new ArrayList<>(categoryCounts.keySet());
        UiState state = loadState();
        this.favoriteCommands = new LinkedHashSet<>(state.favorites == null ? List.of() : state.favorites);
        this.recentCommands = new ArrayList<>(state.recents == null ? List.of() : state.recents);
        this.clickAction = state.action == null ? ClickAction.RUN : state.action;
        this.compactRows = state.compact;
        this.selectedCategory = categories.contains(state.category) ? state.category : "";
        this.quickFilter = state.quickFilter == null ? QuickFilter.ALL : state.quickFilter;
        this.sortMode = state.sortMode == null ? SortMode.TOP : state.sortMode;
    }

    @Override
    protected void init() {
        clearWidgets();
        stateFile = stateFile == null ? defaultStateFile() : stateFile;
        scrollPixels = 0.0;

        lineHeight = Math.max(CFG.lineHeight(compactRows), minecraft.font.lineHeight + CFG.verticalPadding(compactRows));
        computeListLayout();

        int controlH = Math.max(CFG.controlHeight, minecraft.font.lineHeight + 10);
        int headerBlockHeight = (minecraft.font.lineHeight * 2) + 34;
        int topControls = margin() + headerBlockHeight;
        int primaryBottom = addPrimaryControls(topControls, controlH);
        int quickY = primaryBottom + 6;
        int quickBottom = addQuickFilterButtons(quickY, controlH);
        int modeBottom = addModeButtons(quickBottom + 3, controlH);
        int categoryBottom = addCategoryButtons(modeBottom + 3, controlH);
        adjustListTopForControls(categoryBottom);
        applyFilter(searchBox.getValue());
    }

    private int margin() {
        return Math.max(CFG.margin, Math.min(width, height) / 42);
    }

    private Path defaultStateFile() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("helphelper").resolve("ui.json");
    }

    private void computeListLayout() {
        HelpHelperLayoutMath.Layout layout = HelpHelperLayoutMath.plan(width, height, CFG.margin,
            minecraft.font.lineHeight, CFG.controlHeight, CFG.detailPanelWidth, CFG.detailPanelGap);
        listLeft = layout.listLeft();
        listTop = layout.listTop();
        listRight = layout.listRight();
        listBottom = layout.listBottom();
        clampScrollForViewport();
    }

    private int addPrimaryControls(int y, int controlH) {
        int gap = CFG.buttonGap;
        int controlAreaW = Math.max(80, listRight - listLeft);
        int minSearchW = Math.min(CFG.searchMinWidth, Math.max(80, controlAreaW / 3));
        int actionW = Math.min(CFG.actionButtonWidth, Math.max(72, controlAreaW / 5));
        int densityW = CFG.densityButtonWidth;
        int closeW = CFG.closeButtonWidth;
        int inlineMinimum = minSearchW + gap + 56 + gap + 56 + gap + 50;
        if (controlAreaW >= inlineMinimum) {
            int targetRightW = Math.max(120, controlAreaW - minSearchW - gap);
            int rightTotalW = actionW + densityW + closeW + (gap * 2);
            if (rightTotalW > targetRightW) {
                double scale = targetRightW / (double) rightTotalW;
                actionW = Math.max(56, (int) Math.floor(actionW * scale));
                densityW = Math.max(56, (int) Math.floor(densityW * scale));
                closeW = Math.max(50, (int) Math.floor(closeW * scale));
            }
            int closeLeft = listRight - closeW;
            int densityLeft = closeLeft - gap - densityW;
            int actionLeft = densityLeft - gap - actionW;
            int searchW = Math.max(minSearchW, actionLeft - listLeft - gap);
            addSearchBox(listLeft, y, searchW, controlH);
            addActionModeButton(actionLeft, y, actionW, controlH);
            addDensityButton(densityLeft, y, densityW, controlH);
            addDoneButton(closeLeft, y, closeW, controlH);
            return y + controlH;
        }

        addSearchBox(listLeft, y, controlAreaW, controlH);
        int buttonY = y + controlH + 4;
        int minButtonTotal = 56 + 56 + 50 + (gap * 2);
        if (controlAreaW >= minButtonTotal) {
            int total = actionW + densityW + closeW + (gap * 2);
            if (total > controlAreaW) {
                double scale = (controlAreaW - (gap * 2)) / (double) (actionW + densityW + closeW);
                actionW = Math.max(56, (int) Math.floor(actionW * scale));
                densityW = Math.max(56, (int) Math.floor(densityW * scale));
                closeW = Math.max(50, controlAreaW - actionW - densityW - (gap * 2));
            }
            int actionLeft = listRight - (actionW + densityW + closeW + (gap * 2));
            int densityLeft = actionLeft + actionW + gap;
            int closeLeft = densityLeft + densityW + gap;
            addActionModeButton(actionLeft, buttonY, actionW, controlH);
            addDensityButton(densityLeft, buttonY, densityW, controlH);
            addDoneButton(closeLeft, buttonY, closeW, controlH);
            return buttonY + controlH;
        }

        int pairGap = gap;
        int pairW = Math.max(58, (controlAreaW - pairGap) / 2);
        addActionModeButton(listLeft, buttonY, pairW, controlH);
        addDensityButton(listLeft + pairW + pairGap, buttonY, Math.max(58, controlAreaW - pairW - pairGap), controlH);
        int closeY = buttonY + controlH + 4;
        addDoneButton(listLeft, closeY, controlAreaW, controlH);
        return closeY + controlH;
    }

    private void addSearchBox(int x, int y, int width, int height) {
        searchBox = new EditBox(minecraft.font, x, y, width, height, Component.literal("Search"));
        searchBox.setResponder(this::applyFilter);
        searchBox.setHint(Component.literal(ChatFormatting.GRAY + "Search commands..."));
        addRenderableWidget(searchBox);
    }

    private void addActionModeButton(int x, int y, int width, int height) {
        addRenderableWidget(Button.builder(Component.literal(clickAction.label()), btn -> {
            clickAction = clickAction.next();
            rebuildWidgetsKeepingSearch();
            saveState();
        }).bounds(x, y, width, height).build());
    }

    private void addDensityButton(int x, int y, int width, int height) {
        addRenderableWidget(Button.builder(Component.literal(compactRows ? "Compact" : "Roomy"), btn -> {
            compactRows = !compactRows;
            rebuildWidgetsKeepingSearch();
            saveState();
        }).bounds(x, y, width, height).build());
    }

    private void addDoneButton(int x, int y, int width, int height) {
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> minecraft.setScreen(null))
            .bounds(x, y, width, height)
            .build());
    }

    private void applyFilter(String raw) {
        String query = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        visible = allCommands.stream()
            .filter(c -> quickFilter.matches(c, favoriteCommands, recentCommands))
            .filter(c -> selectedCategory.isEmpty() || c.category().equals(selectedCategory))
            .filter(c -> query.isEmpty() || c.matches(query) || (CFG.enableFuzzySearch && fuzzyScore(searchableText(c), query) >= 58))
            .sorted((a, b) -> compareRows(a, b, query))
            .toList();
        selectedIndex = visible.isEmpty() ? -1 : Math.max(0, Math.min(selectedIndex, visible.size() - 1));
        scrollPixels = 0.0;
        hoveredIndex = -1;
        draggingScrollbar = false;
        clampScrollForViewport();
    }

    private int minListHeight() {
        return 72;
    }

    private int topHintReserve() {
        return minecraft.font.lineHeight + 10;
    }

    private int maxControlBottom() {
        int reserve = minListHeight() + topHintReserve();
        return Math.max(margin() + 56, height - margin() - reserve);
    }

    private void adjustListTopForControls(int controlsBottom) {
        int desiredTop = controlsBottom + topHintReserve();
        int maxTop = Math.max(margin() + 60, height - margin() - minListHeight());
        listTop = Math.min(Math.max(listTop, desiredTop), maxTop);
        listBottom = Math.max(listTop + minListHeight(), height - margin());
        clampScrollForViewport();
    }

    private int addCategoryButtons(int y, int buttonH) {
        int x = listLeft;
        int gap = 5;
        int bottomLimit = maxControlBottom();
        x = addCategoryButton("All", "", allCommands.size(), x, y, buttonH) + gap;
        int rowY = y;
        for (String category : categories) {
            if (x >= listRight - 56) {
                rowY += buttonH + 4;
                x = listLeft;
            }
            if (rowY + buttonH > bottomLimit) {
                return Math.max(y + buttonH, rowY - 4);
            }
            int needed = Math.max(CFG.minCategoryWidth, minecraft.font.width(category) + 24);
            if (x > listLeft && x + needed > listRight) {
                rowY += buttonH + 4;
                x = listLeft;
            }
            if (rowY + buttonH > bottomLimit) {
                return Math.max(y + buttonH, rowY - 4);
            }
            x = addCategoryButton(category, category, categoryCounts.getOrDefault(category, 0), x, rowY, buttonH) + gap;
        }
        return rowY + buttonH;
    }

    private int addQuickFilterButtons(int y, int buttonH) {
        int x = listLeft;
        int gap = 5;
        int rowY = y;
        int bottomLimit = maxControlBottom();
        for (QuickFilter filter : QuickFilter.values()) {
            String label = filter.label();
            int count = filter.count(allCommands, favoriteCommands, recentCommands);
            String text = (quickFilter == filter ? "[" + label + "]" : label) + " " + count;
            int w = Math.min(Math.max(CFG.minCategoryWidth, minecraft.font.width(text) + 18),
                Math.max(CFG.minCategoryWidth, listRight - x));
            if (x > listLeft && x + w > listRight) {
                rowY += buttonH + 4;
                x = listLeft;
            }
            if (rowY + buttonH > bottomLimit) {
                return Math.max(y + buttonH, rowY - 4);
            }
            w = Math.min(Math.max(CFG.minCategoryWidth, minecraft.font.width(text) + 18),
                Math.max(CFG.minCategoryWidth, listRight - x));
            addRenderableWidget(Button.builder(Component.literal(text), btn -> {
                quickFilter = filter;
                rebuildWidgetsKeepingSearch();
                saveState();
            }).bounds(x, rowY, w, buttonH).build());
            x += w + gap;
        }
        return rowY + buttonH;
    }

    private int addModeButtons(int y, int buttonH) {
        int x = listLeft;
        int gap = 5;
        int rowY = y;
        int bottomLimit = maxControlBottom();
        for (SortMode mode : SortMode.values()) {
            String text = sortMode == mode ? "[" + mode.label() + "]" : mode.label();
            int w = Math.min(Math.max(CFG.minCategoryWidth, minecraft.font.width(text) + 18),
                Math.max(CFG.minCategoryWidth, listRight - x));
            if (x > listLeft && x + w > listRight) {
                rowY += buttonH + 4;
                x = listLeft;
            }
            if (rowY + buttonH > bottomLimit) {
                return Math.max(y + buttonH, rowY - 4);
            }
            w = Math.min(Math.max(CFG.minCategoryWidth, minecraft.font.width(text) + 18),
                Math.max(CFG.minCategoryWidth, listRight - x));
            addRenderableWidget(Button.builder(Component.literal(text), btn -> {
                sortMode = mode;
                rebuildWidgetsKeepingSearch();
                saveState();
            }).bounds(x, rowY, w, buttonH).build());
            x += w + gap;
        }
        int clearW = Math.min(72, Math.max(CFG.minCategoryWidth, listRight - x));
        if (clearW >= CFG.minCategoryWidth) {
            if (x > listLeft && x + clearW > listRight) {
                rowY += buttonH + 4;
                x = listLeft;
                clearW = Math.min(72, Math.max(CFG.minCategoryWidth, listRight - x));
            }
            if (rowY + buttonH > bottomLimit) {
                return Math.max(y + buttonH, rowY - 4);
            }
            addRenderableWidget(Button.builder(Component.literal("Clear"), btn -> {
                quickFilter = QuickFilter.ALL;
                sortMode = SortMode.TOP;
                selectedCategory = "";
                if (searchBox != null) {
                    searchBox.setValue("");
                }
                rebuildWidgetsKeepingSearch();
                saveState();
            }).bounds(x, rowY, clearW, buttonH).build());
        }
        return rowY + buttonH;
    }

    private int addCategoryButton(String label, String category, int count, int x, int y, int h) {
        boolean selected = selectedCategory.equals(category);
        int available = Math.max(CFG.minCategoryWidth, Math.min(CFG.maxCategoryWidth, listRight - x));
        String labelText = ellipsize(label, Math.max(8, available - 18 - minecraft.font.width(" " + count)));
        String text = (selected ? "> " : "") + labelText + " " + count;
        int w = Math.min(Math.max(CFG.minCategoryWidth, minecraft.font.width(text) + 18), available);
        addRenderableWidget(Button.builder(Component.literal(text), btn -> {
            selectedCategory = category;
            rebuildWidgetsKeepingSearch();
            saveState();
        }).bounds(x, y, w, h).build());
        return x + w;
    }

    private void rebuildWidgetsKeepingSearch() {
        String saved = searchBox == null ? "" : searchBox.getValue();
        rebuildWidgets();
        if (searchBox != null) {
            searchBox.setValue(saved);
            applyFilter(saved);
        }
    }

    @Override
    public void resize(int resizeWidth, int resizeHeight) {
        String saved = searchBox == null ? "" : searchBox.getValue();
        super.resize(resizeWidth, resizeHeight);
        if (searchBox != null) {
            searchBox.setValue(saved);
            applyFilter(saved);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
        drawBackdrop(gfx);
        super.extractRenderState(gfx, mouseX, mouseY, partialTick);
        drawCommandRows(gfx, mouseX, mouseY);
        drawDetailsPanel(gfx, mouseX, mouseY);
        if (showHelpOverlay) {
            drawShortcutOverlay(gfx);
        }
    }

    private void drawBackdrop(GuiGraphicsExtractor gfx) {
        int m = margin();
        int fh = minecraft.font.lineHeight;
        // Header band height scales with font so it never clips the title or counter lines
        int headerH = Math.max(38, fh * 3 + 8);

        gfx.fill(m, m, width - m, height - m, ARGB.color(228, 12, 15, 22));
        gfx.fillGradient(m + 14, m + 6, width - m - 14, m + headerH,
            ARGB.color(255, 72, 64, 42), ARGB.color(255, 28, 32, 50));
        gfx.fillGradient(m + 14, m + headerH, width - m - 14, m + headerH + 10,
            ARGB.color(120, 255, 226, 120), ARGB.color(0, 255, 226, 120));

        gfx.centeredText(minecraft.font, Component.literal("Help Helper").withStyle(ChatFormatting.GOLD),
            width / 2, m + 8, ARGB.color(255, 255, 226, 120));

        // Counter and filter line separated by exactly one line-height so they never overlap
        String counter = ChatFormatting.YELLOW.toString() + visible.size() + ChatFormatting.GRAY + " / "
            + allCommands.size() + ChatFormatting.WHITE + " commands";
        gfx.text(minecraft.font, counter, listLeft, m + fh + 16, ARGB.color(255, 200, 200, 200));
        String filterLine = ellipsize(activeFilterText(), Math.max(60, listRight - listLeft));
        gfx.text(minecraft.font, filterLine, listLeft, m + fh * 2 + 20, ARGB.color(255, 145, 188, 206));

        // Action hint above the list — anchored to listTop rather than a fixed pixel offset
        String actionHint = (clickAction == ClickAction.RUN && selectedIndex >= 0 && selectedIndex < visible.size()
            && visible.get(selectedIndex).info().risky())
                ? clickAction.helpText() + ChatFormatting.YELLOW + "  (risky - will open chat for review)"
                : clickAction.helpText();
        String shortcuts = "C mode  D density  F favorite  / help  O config";
        int shortcutsW = minecraft.font.width(shortcuts);
        int hintMax = Math.max(40, listRight - listLeft - shortcutsW - 10);
        gfx.text(minecraft.font, Component.literal(ellipsize(actionHint, hintMax)).withStyle(ChatFormatting.GRAY),
            listLeft, listTop - fh - 4, ARGB.color(255, 180, 186, 204));
        if (shortcutsW < listRight - listLeft - 24) {
            gfx.text(minecraft.font, shortcuts, Math.max(listLeft, listRight - shortcutsW), listTop - fh - 4,
                ARGB.color(255, 130, 136, 150));
        }

        if (!feedbackMessage.isEmpty() && System.currentTimeMillis() < feedbackUntilMillis) {
            String feedback = ellipsize(feedbackMessage, Math.max(40, listRight - listLeft));
            gfx.centeredText(minecraft.font, Component.literal(feedback).withStyle(ChatFormatting.YELLOW),
                (listLeft + listRight) / 2, listTop - (fh * 2) - 6, ARGB.color(255, 255, 226, 120));
        }
        // List box: subtle background fill, border, and L-shaped gold corner accents
        int bx0 = listLeft - 4, by0 = listTop - 4, bx1 = listRight + 4, by1 = listBottom + 4;
        gfx.fill(bx0, by0, bx1, by1, ARGB.color(52, 20, 28, 48));
        outlineRect(gfx, bx0, by0, bx1, by1, ARGB.color(200, 90, 100, 150));
        int ca = 6;
        int cc = ARGB.color(210, 255, 214, 88);
        gfx.fill(bx0,      by0,      bx0 + ca, by0 + 1,  cc);
        gfx.fill(bx0,      by0,      bx0 + 1,  by0 + ca, cc);
        gfx.fill(bx1 - ca, by0,      bx1,      by0 + 1,  cc);
        gfx.fill(bx1 - 1,  by0,      bx1,      by0 + ca, cc);
        gfx.fill(bx0,      by1 - 1,  bx0 + ca, by1,      cc);
        gfx.fill(bx0,      by1 - ca, bx0 + 1,  by1,      cc);
        gfx.fill(bx1 - ca, by1 - 1,  bx1,      by1,      cc);
        gfx.fill(bx1 - 1,  by1 - ca, bx1,      by1,      cc);
    }

    private void drawCommandRows(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        if (visible.isEmpty()) {
            gfx.centeredText(minecraft.font, Component.literal("No commands for current filters").withStyle(ChatFormatting.GRAY),
                width / 2, listTop + Math.max(10, (listBottom - listTop) / 2 - minecraft.font.lineHeight),
                ARGB.color(255, 180, 186, 204));
            gfx.centeredText(minecraft.font, Component.literal("Clear search or change filter").withStyle(ChatFormatting.DARK_GRAY),
                width / 2, listTop + Math.max(24, (listBottom - listTop) / 2 + 4),
                ARGB.color(255, 126, 134, 154));
            return;
        }

        gfx.enableScissor(listLeft, listTop, listRight, listBottom);
        try {
            for (int i = 0; i < visible.size(); i++) {
                double y = listTop + (double) i * lineHeight - scrollPixels;
                if (y + lineHeight <= listTop || y >= listBottom) {
                    continue;
                }
                int yi = (int) Math.round(y);
                boolean selected = i == selectedIndex;
                boolean hovered = i == hoveredIndex;
                CommandCatalog.CommandRow row = visible.get(i);
                // Zebra base tints — even rows darker, odd rows a hair lighter
                boolean even = (i % 2 == 0);
                int baseNormal = even ? ARGB.color(68, 22, 28, 44) : ARGB.color(40, 255, 255, 255);
                int baseRisky  = even ? ARGB.color(72, 48, 32, 22) : ARGB.color(48, 255, 210, 170);
                int rowBg;
                if (selected) {
                    rowBg = row.info().risky() ? ARGB.color(162, 104, 66, 46) : ARGB.color(164, 70, 94, 156);
                } else if (hovered) {
                    rowBg = row.info().risky() ? ARGB.color(124, 86, 50, 36) : ARGB.color(126, 52, 70, 108);
                } else {
                    rowBg = row.info().risky() ? baseRisky : baseNormal;
                }
                gfx.fill(listLeft, yi, contentRight(), yi + lineHeight - 1, rowBg);

                // Subtle separator line at top of every row except the first
                if (i > 0) {
                    gfx.fill(listLeft + 6, yi, contentRight() - 4, yi + 1, ARGB.color(24, 190, 205, 240));
                }

                // Category color bar; selected rows also get a thin gold accent next to it
                gfx.fill(listLeft, yi, listLeft + 3, yi + lineHeight - 1, categoryColor(row.category()));
                if (selected) {
                    gfx.fill(listLeft + 3, yi, listLeft + 5, yi + lineHeight - 1, ARGB.color(160, 255, 212, 88));
                }

                int badgeRight = contentRight() - 4;
                int textRight = badgeRight - CFG.badgeWidth - 4;
                int textX = listLeft + (favoriteCommands.contains(row.command()) ? 16 : 10);
                String text = ellipsize(row.command(), textRight - textX);
                int ty = yi + Math.max(1, (lineHeight - minecraft.font.lineHeight) / 2);

                int textColor = row.info().risky() ? ARGB.color(255, 255, 204, 164)
                    : (row.info().vanilla() ? ARGB.color(255, 164, 238, 226) : ARGB.color(255, 214, 198, 255));
                MutableComponent styled = Component.literal(text).withStyle(ChatFormatting.UNDERLINE);
                gfx.text(minecraft.font, styled, textX, ty, textColor, true);
                if (favoriteCommands.contains(row.command())) {
                    gfx.text(minecraft.font, "*", listLeft + 7, ty, ARGB.color(255, 255, 226, 120));
                }
                int badgeInset = Math.max(2, (lineHeight - (minecraft.font.lineHeight + 6)) / 2);
                drawRowBadge(gfx, row, badgeRight - CFG.badgeWidth, yi + badgeInset, badgeRight, yi + lineHeight - badgeInset);
            }
        } finally {
            gfx.disableScissor();
        }
        drawScrollbar(gfx);
    }

    private void drawRowBadge(GuiGraphicsExtractor gfx, CommandCatalog.CommandRow row, int x0, int y0, int x1, int y1) {
        String label = row.info().risky() ? "!" : (row.info().vanilla() ? "V" : "M");
        int bg  = row.info().risky() ? ARGB.color(140, 120, 56, 28)
            : (row.info().vanilla() ? ARGB.color(115, 28, 82, 78) : ARGB.color(115, 66, 52, 112));
        int bdr = row.info().risky() ? ARGB.color(190, 205, 108, 48)
            : (row.info().vanilla() ? ARGB.color(160, 48, 158, 148) : ARGB.color(160, 140, 108, 210));
        int fg  = row.info().risky() ? ARGB.color(255, 255, 194, 130)
            : (row.info().vanilla() ? ARGB.color(255, 166, 244, 226) : ARGB.color(255, 220, 206, 255));
        gfx.fill(x0, y0, x1, y1, bg);
        outlineRect(gfx, x0, y0, x1, y1, bdr);
        gfx.centeredText(minecraft.font, label, (x0 + x1) / 2, y0 + Math.max(1, (y1 - y0 - minecraft.font.lineHeight) / 2),
            fg);
    }

    private String activeFilterText() {
        String category = selectedCategory.isEmpty() ? "All categories" : selectedCategory;
        return "Scope " + quickFilter.label() + "  Sort " + sortMode.label() + "  Cat " + category;
    }

    private int categoryColor(String category) {
        return CFG.categoryColor(category);
    }

    private void drawDetailsPanel(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        presetHits = List.of();
        int panelLeft = listRight + CFG.detailPanelGap;
        int panelRight = width - margin();
        if (panelRight - panelLeft < 140 || visible.isEmpty()) {
            return;
        }

        int panelTop = listTop;
        int panelBottom = listBottom;
        gfx.fill(panelLeft, panelTop, panelRight, panelBottom, CFG.parseColor(CFG.colorPanelBackground));
        outlineRect(gfx, panelLeft, panelTop, panelRight, panelBottom, CFG.parseColor(CFG.colorScrollbar));
        // Subtle gold accent gradient along the top edge of the panel
        gfx.fillGradient(panelLeft + 1, panelTop + 1, panelRight - 1, panelTop + 3,
            ARGB.color(130, 255, 212, 88), ARGB.color(0, 255, 212, 88));

        CommandCatalog.CommandRow row = visible.get(selectedIndex >= 0 ? selectedIndex : 0);
        CommandCatalog.CommandInfo info = row.info();
        int x = panelLeft + 8;
        int y = panelTop + 9;
        int maxW = panelRight - x - 8;
        int footerTop = Math.max(y + 72, panelBottom - 72);

        // Category color bar on left edge of panel
        gfx.fill(panelLeft, panelTop, panelLeft + 3, panelBottom, categoryColor(info.category()));
        gfx.text(minecraft.font, ellipsize(info.title(), maxW), x, y, CFG.parseColor(CFG.colorFavoriteStar), true);
        y += minecraft.font.lineHeight + 6;
        gfx.text(minecraft.font, ellipsize(row.command(), maxW), x, y, CFG.parseColor(CFG.colorRiskyText), false);
        y += minecraft.font.lineHeight + 8;
        // Separator line after the title/command header block
        gfx.fill(panelLeft + 4, y, panelRight - 4, y + 1, ARGB.color(80, 115, 138, 198));
        y += 7;
        gfx.text(minecraft.font, info.category(), x, y, categoryColor(info.category()));
        y += minecraft.font.lineHeight + 6;
        String source = info.risky() ? "Risky command" : (info.vanilla() ? "Vanilla command" : "Server/mod command");
        int sourceColor = info.risky() ? ARGB.color(255, 255, 170, 118)
            : (info.vanilla() ? ARGB.color(255, 166, 244, 226) : ARGB.color(255, 220, 206, 255));
        gfx.text(minecraft.font, source, x, y, sourceColor);
        y += minecraft.font.lineHeight + 8;
        if (info.risky()) {
            gfx.text(minecraft.font, "Fills chat by default", x, y, CFG.parseColor(CFG.colorSourceRisky));
            y += minecraft.font.lineHeight + 8;
        }

        for (String line : wrap(info.description(), maxW)) {
            if (y + minecraft.font.lineHeight >= footerTop) {
                break;
            }
            gfx.text(minecraft.font, line, x, y, CFG.parseColor(CFG.colorDescription));
            y += minecraft.font.lineHeight + 2;
        }

        if (!info.aliases().isEmpty() && y + minecraft.font.lineHeight < footerTop) {
            y += 6;
            gfx.text(minecraft.font, "Aliases: " + String.join(", ", info.aliases()), x, y,
                CFG.parseColor(CFG.colorSourceModded));
            y += minecraft.font.lineHeight + 6;
        }

        if (!info.presets().isEmpty()) {
            y = Math.max(y + 4, footerTop);
            // Section separator + left accent bar before "Templates" label
            gfx.fill(panelLeft + 4, y - 3, panelRight - 4, y - 2, ARGB.color(80, 115, 138, 198));
            gfx.fill(x, y, x + 2, y + minecraft.font.lineHeight + 2, ARGB.color(200, 255, 212, 88));
            gfx.text(minecraft.font, "  Templates", x, y, ARGB.color(255, 255, 226, 120));
            y += minecraft.font.lineHeight + 5;
            List<PresetHit> hits = new ArrayList<>();
            int presetH = minecraft.font.lineHeight + 8;
            int presetGap = 3;
            for (String preset : info.presets()) {
                if (y + presetH > panelBottom - 6) {
                    break;
                }
                int px0 = x, py0 = y, px1 = panelRight - 8, py1 = y + presetH;
                boolean hovered = mouseX >= px0 && mouseX < px1 && mouseY >= py0 && mouseY < py1;
                gfx.fill(px0, py0, px1, py1, hovered ? ARGB.color(148, 82, 92, 124) : ARGB.color(82, 38, 42, 60));
                outlineRect(gfx, px0, py0, px1, py1,
                    hovered ? ARGB.color(185, 90, 102, 158) : ARGB.color(95, 62, 74, 116));
                int presetTextY = py0 + Math.max(2, (presetH - minecraft.font.lineHeight) / 2);
                gfx.text(minecraft.font, ellipsize(preset, maxW - 12), px0 + 5, presetTextY, ARGB.color(255, 210, 245, 255));
                hits.add(new PresetHit(preset, px0, py0, px1, py1));
                y += presetH + presetGap;
            }
            presetHits = hits;
        }
    }

    private void drawShortcutOverlay(GuiGraphicsExtractor gfx) {
        int lineGap = 7;
        int contentLines = 5;
        int panelW = Math.min(330, width - margin() * 4);
        int panelH = Math.min(height - margin() * 2, 36 + contentLines * (minecraft.font.lineHeight + lineGap));
        int x0 = (width - panelW) / 2;
        int y0 = Math.max(margin() + 20, (height - panelH) / 2);
        int x1 = x0 + panelW;
        int y1 = y0 + panelH;
        gfx.fill(x0, y0, x1, y1, ARGB.color(238, 10, 13, 20));
        outlineRect(gfx, x0, y0, x1, y1, ARGB.color(220, 255, 212, 88));
        gfx.centeredText(minecraft.font, Component.literal("Help Helper Keys").withStyle(ChatFormatting.GOLD),
            width / 2, y0 + 10, ARGB.color(255, 255, 226, 120));
        int y = y0 + 30;
        List<String> lines = List.of(
            "Click row: select   Double-click/Enter: act",
            "C: action mode   D: row density   F: favorite",
            "Arrows/Page/Home/End: navigate list",
            "/: close this overlay   O: config",
            "Risky run: confirm, edit, or cancel"
        );
        for (String line : lines) {
            gfx.text(minecraft.font, line, x0 + 14, y, ARGB.color(255, 212, 220, 238));
            y += minecraft.font.lineHeight + lineGap;
        }
    }
    private void drawScrollbar(GuiGraphicsExtractor gfx) {
        if (!hasScrollableContent()) {
            return;
        }
        int trackLeft = scrollbarLeft();
        int thumbH = scrollbarThumbHeight();
        int thumbTop = scrollbarThumbTop();
        int thumbBottom = Math.min(listBottom, thumbTop + thumbH);
        gfx.fill(trackLeft + 1, listTop + 1, listRight - 1, listBottom - 1, ARGB.color(75, 6, 8, 14));
        int thumbColor = draggingScrollbar ? ARGB.color(225, 158, 182, 244) : ARGB.color(192, 128, 148, 212);
        gfx.fill(trackLeft + 1, thumbTop, listRight - 1, thumbBottom, thumbColor);
    }

    private String ellipsize(String cmd, int maxPx) {
        if (maxPx <= minecraft.font.width("...")) {
            return "";
        }
        if (minecraft.font.width(cmd) <= maxPx) {
            return cmd;
        }
        String ellipsis = "...";
        int avail = Math.max(maxPx - minecraft.font.width(ellipsis), minecraft.font.width(" "));
        int end = cmd.length();
        while (end > 1 && minecraft.font.width(cmd.substring(0, end)) > avail) {
            end--;
        }
        return cmd.substring(0, end).trim() + ellipsis;
    }

    private static void outlineRect(GuiGraphicsExtractor gfx, int x0, int y0, int x1, int y1, int color) {
        gfx.horizontalLine(x0, x1, y0, color);
        gfx.horizontalLine(x0, x1, y1, color);
        gfx.verticalLine(x0, y0, y1, color);
        gfx.verticalLine(x1, y0, y1, color);
    }

    private boolean isOverList(double mx, double my) {
        return mx >= listLeft && mx < listRight && my >= listTop && my < listBottom;
    }

    private boolean hasScrollableContent() {
        return maxScroll() > 0.0;
    }

    private int viewportHeight() {
        return Math.max(0, listBottom - listTop);
    }

    private int scrollbarLeft() {
        return listRight - CFG.scrollbarWidth;
    }

    private int contentRight() {
        return hasScrollableContent() ? scrollbarLeft() - CFG.scrollbarGap : listRight - CFG.scrollbarGap;
    }

    private boolean isOverContent(double mx, double my) {
        return mx >= listLeft && mx < contentRight() && my >= listTop && my < listBottom;
    }

    private boolean isOverScrollbar(double mx, double my) {
        return hasScrollableContent() && mx >= scrollbarLeft() && mx < listRight && my >= listTop && my < listBottom;
    }

    private int scrollbarThumbHeight() {
        int viewport = viewportHeight();
        int total = Math.max(1, visible.size() * lineHeight);
        return Math.max(18, (int) Math.round(viewport * (viewport / (double) total)));
    }

    private int scrollbarThumbTop() {
        double max = maxScroll();
        if (max <= 0.0) {
            return listTop;
        }
        int viewport = viewportHeight();
        int thumbH = scrollbarThumbHeight();
        return listTop + (int) Math.round((viewport - thumbH) * (scrollPixels / max));
    }

    private boolean isOverScrollbarThumb(double mx, double my) {
        if (!isOverScrollbar(mx, my)) {
            return false;
        }
        int top = scrollbarThumbTop();
        return my >= top && my < top + scrollbarThumbHeight();
    }

    private void scrollBy(double deltaPixels) {
        scrollPixels += deltaPixels;
        clampScrollForViewport();
    }

    private void scrollTo(double targetPixels) {
        scrollPixels = targetPixels;
        clampScrollForViewport();
    }

    private double wheelStepPixels() {
        return Math.max(18.0, lineHeight * (compactRows ? 2.3 : 1.85));
    }

    private void pageScroll(double direction) {
        scrollBy(direction * viewportHeight() * CFG.pageScrollFactor);
    }

    private void dragScrollbarTo(double mouseY) {
        int thumbH = scrollbarThumbHeight();
        int viewport = viewportHeight();
        int usable = Math.max(1, viewport - thumbH);
        double thumbTop = mouseY - scrollbarGrabOffset;
        double ratio = (thumbTop - listTop) / usable;
        scrollTo(ratio * maxScroll());
    }

    private void ensureSelectionVisible() {
        if (selectedIndex < 0 || selectedIndex >= visible.size()) {
            return;
        }
        double rowTop = (double) selectedIndex * lineHeight;
        double rowBottom = rowTop + lineHeight;
        double viewportTop = scrollPixels;
        double viewportBottom = scrollPixels + viewportHeight();
        if (rowTop < viewportTop) {
            scrollTo(rowTop);
        } else if (rowBottom > viewportBottom) {
            scrollTo(rowBottom - viewportHeight());
        }
    }

    private void moveSelection(int delta) {
        if (visible.isEmpty()) {
            selectedIndex = -1;
            return;
        }
        if (selectedIndex < 0) {
            selectedIndex = 0;
        } else {
            selectedIndex = Math.max(0, Math.min(visible.size() - 1, selectedIndex + delta));
        }
        ensureSelectionVisible();
    }

    private int rowIndexAt(double mx, double my) {
        if (!isOverContent(mx, my)) {
            return -1;
        }
        int rel = (int) (my - listTop + scrollPixels);
        if (rel < 0) {
            return -1;
        }
        return rel / lineHeight;
    }

    private double maxScroll() {
        int viewport = Math.max(0, listBottom - listTop);
        int total = visible.size() * lineHeight;
        return Math.max(0, total - viewport);
    }

    private void clampScrollForViewport() {
        scrollPixels = Math.max(0.0, Math.min(scrollPixels, maxScroll()));
    }

    private void runCommandRow(int index) {
        if (minecraft.player == null || index < 0 || index >= visible.size()) {
            return;
        }
        CommandCatalog.CommandRow row = visible.get(index);
        // Risky commands open chat for review instead of running immediately, even in RUN mode.
        // This is intentional: the detail panel labels them "Fills chat by default" so the user knows.
        if (row.info().risky() && clickAction == ClickAction.RUN && CFG.confirmRiskyCommands) {
            minecraft.setScreen(new RiskyCommandConfirmScreen(this, row.command()));
            return;
        }
        ClickAction action = row.info().risky() && clickAction == ClickAction.RUN ? ClickAction.FILL_CHAT : clickAction;
        runCommand(row.command(), action);
    }

    private void markRecent(String command) {
        // Remove any existing occurrence so we don't end up with duplicates during the add.
        recentCommands.remove(command);
        recentCommands.add(0, command);
        while (recentCommands.size() > Math.max(1, CFG.maxRecentCommands)) {
            recentCommands.remove(recentCommands.size() - 1);
        }
        saveState();
    }

    private void showFeedback(String message) {
        feedbackMessage = message;
        feedbackUntilMillis = System.currentTimeMillis() + 1800L;
    }

    private void runCommand(String command, ClickAction action) {
        if (minecraft.player == null) {
            return;
        }
        switch (action) {
            case RUN -> {
                markRecent(command);
                Screen.clickCommandAction(minecraft.player, command, this);
                minecraft.setScreen(null);
            }
            case COPY -> {
                minecraft.keyboardHandler.setClipboard(command);
                showFeedback("Copied " + command);
                markRecent(command);
            }
            case FILL_CHAT -> {
                showFeedback("Opened in chat");
                minecraft.setScreen(new ChatScreen(command, true));
                markRecent(command);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (super.mouseClicked(event, doubled)) {
            return true;
        }

        if (event.button() == 0 && isOverScrollbar(event.x(), event.y())) {
            if (isOverScrollbarThumb(event.x(), event.y())) {
                draggingScrollbar = true;
                scrollbarGrabOffset = event.y() - scrollbarThumbTop();
                setDragging(true);
            } else if (event.y() < scrollbarThumbTop()) {
                pageScroll(-1.0);
            } else {
                pageScroll(1.0);
            }
            return true;
        }

        for (PresetHit hit : presetHits) {
            if (event.button() == 0 && hit.contains(event.x(), event.y())) {
                runCommand(hit.command(), ClickAction.FILL_CHAT);
                return true;
            }
        }

        int row = rowIndexAt(event.x(), event.y());
        if (row >= 0 && row < visible.size()) {
            selectedIndex = row;
            ensureSelectionVisible();
            if (doubled) {
                runCommandRow(row);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
        if (isOverList(mouseX, mouseY) && hasScrollableContent()) {
            scrollBy(-scrollDeltaY * wheelStepPixels());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        int row = rowIndexAt(mouseX, mouseY);
        hoveredIndex = row >= 0 && row < visible.size() ? row : -1;
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (draggingScrollbar && event.button() == 0) {
            dragScrollbarTo(event.y());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggingScrollbar && event.button() == 0) {
            draggingScrollbar = false;
            setDragging(false);
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (super.keyPressed(event)) {
            return true;
        }
        int key = event.key();
        if (key == CFG.keyHelpOverlay) {
            showHelpOverlay = !showHelpOverlay;
            return true;
        }
        if (key == CFG.keyConfigScreen) {
            minecraft.setScreen(new HelpHelperConfigScreen(this));
            return true;
        }
        if (key == CFG.keyDown) {
            moveSelection(1);
            return true;
        }
        if (key == CFG.keyUp) {
            moveSelection(-1);
            return true;
        }
        if (key == CFG.keyPageUp) {
            pageScroll(-1.0);
            return true;
        }
        if (key == CFG.keyPageDown) {
            pageScroll(1.0);
            return true;
        }
        if (key == CFG.keyHome) {
            scrollTo(0.0);
            selectedIndex = visible.isEmpty() ? -1 : 0;
            return true;
        }
        if (key == CFG.keyEnd) {
            scrollTo(maxScroll());
            selectedIndex = visible.isEmpty() ? -1 : visible.size() - 1;
            return true;
        }
        if (key == CFG.keyEnter || key == CFG.keyKpEnter) {
            runCommandRow(selectedIndex >= 0 ? selectedIndex : 0);
            return true;
        }
        if (key == CFG.keyCycleAction) {
            clickAction = clickAction.next();
            rebuildWidgetsKeepingSearch();
            saveState();
            return true;
        }
        if (key == CFG.keyToggleCompact) {
            compactRows = !compactRows;
            rebuildWidgetsKeepingSearch();
            saveState();
            return true;
        }
        if (key == CFG.keyToggleFavorite) {
            toggleFavorite();
            return true;
        }
        return false;
    }

    private void toggleFavorite() {
        if (selectedIndex < 0 || selectedIndex >= visible.size()) {
            return;
        }
        String command = visible.get(selectedIndex).command();
        if (favoriteCommands.contains(command)) {
            favoriteCommands.remove(command);
            showFeedback("Removed favorite");
        } else if (favoriteCommands.size() < Math.max(1, CFG.maxFavorites)) {
            favoriteCommands.add(command);
            showFeedback("Added favorite");
        } else {
            showFeedback("Favorite limit reached");
        }
        saveState();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        saveState();
    }

    private static Map<String, Integer> collectCategories(List<CommandCatalog.CommandRow> commands) {
        Map<String, Integer> raw = new LinkedHashMap<>();
        for (CommandCatalog.CommandRow command : commands) {
            raw.merge(command.category(), 1, Integer::sum);
        }
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String category : CATEGORY_ORDER) {
            Integer count = raw.remove(category);
            if (count != null) {
                counts.put(category, count);
            }
        }
        for (Map.Entry<String, Integer> entry : raw.entrySet()) {
            counts.put(entry.getKey(), entry.getValue());
        }
        return counts;
    }

    private String searchableText(CommandCatalog.CommandRow row) {
        return (row.command() + " " + row.root() + " " + row.syntax() + " " + row.originHint() + " "
            + row.info().title() + " " + row.info().category() + " " + row.info().description() + " "
            + String.join(" ", row.info().aliases())).toLowerCase(Locale.ROOT);
    }

    private int fuzzyScore(String haystack, String needle) {
        if (needle.isBlank()) {
            return 100;
        }
        int h = 0;
        int n = 0;
        int streak = 0;
        int score = 0;
        while (h < haystack.length() && n < needle.length()) {
            if (haystack.charAt(h) == needle.charAt(n)) {
                streak++;
                score += 12 + Math.min(18, streak * 3);
                n++;
            } else {
                streak = 0;
                score -= 1;
            }
            h++;
        }
        if (n < needle.length()) {
            return 0;
        }
        int densityPenalty = Math.max(0, haystack.length() - needle.length()) / 8;
        return Math.max(1, Math.min(100, score - densityPenalty));
    }
    private int score(CommandCatalog.CommandRow row, String query) {
        if (query.isEmpty()) {
            int base = 0;
            if (favoriteCommands.contains(row.command())) base += 1000;
            int recentIndex = recentCommands.indexOf(row.command());
            if (recentIndex >= 0) base += 500 - (recentIndex * 10);
            if (row.info().vanilla()) base += 20;
            if (row.info().risky()) base -= 5;
            return base;
        }

        int score = 0;
        String cmd = row.command().toLowerCase(Locale.ROOT);
        String root = row.root().toLowerCase(Locale.ROOT);
        String title = row.info().title().toLowerCase(Locale.ROOT);
        String category = row.info().category().toLowerCase(Locale.ROOT);
        String desc = row.info().description().toLowerCase(Locale.ROOT);
        String syntax = row.syntax().toLowerCase(Locale.ROOT);
        String origin = row.originHint().toLowerCase(Locale.ROOT);

        if (cmd.startsWith("/" + query)) score += 500;
        if (root.equals(query)) score += 450;
        if (title.startsWith(query)) score += 350;
        if (category.startsWith(query)) score += 220;
        if (cmd.contains(query)) score += 150;
        if (title.contains(query)) score += 120;
        if (desc.contains(query)) score += 40;
        if (syntax.contains(query)) score += 90;
        if (origin.contains(query)) score += 55;
        if (CFG.enableFuzzySearch) score += fuzzyScore(searchableText(row), query) / 2;
        if (row.info().aliases().stream().anyMatch(a -> a.toLowerCase(Locale.ROOT).contains(query))) score += 100;
        if (favoriteCommands.contains(row.command())) score += 60;
        int recentIndex = recentCommands.indexOf(row.command());
        if (recentIndex >= 0) score += 50 - recentIndex;
        if (row.info().risky()) score -= 2;
        return score;
    }

    private int compareRows(CommandCatalog.CommandRow a, CommandCatalog.CommandRow b, String query) {
        return switch (sortMode) {
            case TOP -> Integer.compare(score(b, query), score(a, query));
            case NAME -> a.command().compareToIgnoreCase(b.command());
            case RECENT -> {
                int recent = Integer.compare(recentRank(a), recentRank(b));
                yield recent != 0 ? recent : Integer.compare(score(b, query), score(a, query));
            }
            case VANILLA -> {
                int vanilla = Boolean.compare(!a.info().vanilla(), !b.info().vanilla());
                yield vanilla != 0 ? vanilla : Integer.compare(score(b, query), score(a, query));
            }
        };
    }

    private int recentRank(CommandCatalog.CommandRow row) {
        int index = recentCommands.indexOf(row.command());
        return index < 0 ? Integer.MAX_VALUE : index;
    }

    private UiState loadState() {
        if (stateFile == null || !Files.exists(stateFile)) {
            return new UiState();
        }
        try {
            String json = Files.readString(stateFile, StandardCharsets.UTF_8);
            UiState state = GSON.fromJson(json, UiState.class);
            return state == null ? new UiState() : state;
        } catch (Exception e) {
            return new UiState();
        }
    }

    private void saveState() {
        if (stateFile == null) {
            return;
        }
        try {
            Files.createDirectories(stateFile.getParent());
            UiState state = new UiState();
            state.favorites = new ArrayList<>(favoriteCommands);
            state.recents = new ArrayList<>(recentCommands);
            state.action = clickAction;
            state.compact = compactRows;
            state.category = selectedCategory;
            state.quickFilter = quickFilter;
            state.sortMode = sortMode;
            Files.writeString(stateFile, GSON.toJson(state), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private static final class UiState {
        List<String> favorites = new ArrayList<>();
        List<String> recents = new ArrayList<>();
        ClickAction action = ClickAction.RUN;
        boolean compact;
        String category = "";
        QuickFilter quickFilter = QuickFilter.ALL;
        SortMode sortMode = SortMode.TOP;
    }

    private List<String> wrap(String text, int maxPx) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (minecraft.font.width(candidate) <= maxPx || line.isEmpty()) {
                line = new StringBuilder(candidate);
            } else {
                lines.add(line.toString());
                line = new StringBuilder(word);
            }
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }


    private final class RiskyCommandConfirmScreen extends Screen {
        private final HelpHelperScreen parent;
        private final String command;

        private RiskyCommandConfirmScreen(HelpHelperScreen parent, String command) {
            super(Component.literal("Confirm risky command"));
            this.parent = parent;
            this.command = command;
        }

        @Override
        protected void init() {
            clearWidgets();
            int buttonW = 82;
            int y = height / 2 + 36;
            int x = width / 2 - buttonW - 88;
            addRenderableWidget(Button.builder(Component.literal("Run"), btn -> parent.runCommand(command, ClickAction.RUN))
                .bounds(x, y, buttonW, 22).build());
            addRenderableWidget(Button.builder(Component.literal("Edit"), btn -> parent.runCommand(command, ClickAction.FILL_CHAT))
                .bounds(width / 2 - buttonW / 2, y, buttonW, 22).build());
            addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> minecraft.setScreen(parent))
                .bounds(width / 2 + 88, y, buttonW, 22).build());
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTick) {
            parent.extractBackground(gfx, mouseX, mouseY, partialTick);
            int panelW = Math.min(360, width - 36);
            int panelH = Math.min(height - 36, Math.max(124, minecraft.font.lineHeight * 4 + 66));
            int x0 = (width - panelW) / 2;
            int y0 = (height - panelH) / 2;
            int x1 = x0 + panelW;
            int y1 = y0 + panelH;
            gfx.fill(x0, y0, x1, y1, ARGB.color(242, 16, 12, 14));
            outlineRect(gfx, x0, y0, x1, y1, ARGB.color(230, 255, 170, 118));
            gfx.centeredText(minecraft.font, Component.literal("Risky command").withStyle(ChatFormatting.GOLD),
                width / 2, y0 + 12, ARGB.color(255, 255, 204, 164));
            gfx.centeredText(minecraft.font, Component.literal(ellipsize(command, panelW - 28)),
                width / 2, y0 + 34, ARGB.color(255, 232, 220, 208));
            gfx.centeredText(minecraft.font, Component.literal("Run now, edit in chat, or cancel."),
                width / 2, y0 + 55, ARGB.color(255, 184, 190, 204));
            super.extractRenderState(gfx, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
    private record PresetHit(String command, int x0, int y0, int x1, int y1) {
        boolean contains(double x, double y) {
            return x >= x0 && x < x1 && y >= y0 && y < y1;
        }
    }

    private enum ClickAction {
        RUN("Run", "Click a command to run it. Press C to change action, D to change density."),
        COPY("Copy", "Click a command to copy it to the clipboard."),
        FILL_CHAT("Fill", "Click a command to open chat with it ready to edit.");

        private final String label;
        private final String helpText;

        ClickAction(String label, String helpText) {
            this.label = label;
            this.helpText = helpText;
        }

        String label() {
            return label;
        }

        String helpText() {
            return helpText;
        }

        ClickAction next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private enum QuickFilter {
        ALL("All"),
        FAVORITES("Fav"),
        RECENT("Recent"),
        VANILLA("Vanilla"),
        MODDED("Modded"),
        SAFE("Safe"),
        RISKY("Risky");

        private final String label;

        QuickFilter(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }

        boolean matches(CommandCatalog.CommandRow row, Set<String> favorites, List<String> recents) {
            return switch (this) {
                case ALL -> true;
                case FAVORITES -> favorites.contains(row.command());
                case RECENT -> recents.contains(row.command());
                case VANILLA -> row.info().vanilla();
                case MODDED -> !row.info().vanilla();
                case SAFE -> !row.info().risky();
                case RISKY -> row.info().risky();
            };
        }

        int count(List<CommandCatalog.CommandRow> rows, Set<String> favorites, List<String> recents) {
            int total = 0;
            for (CommandCatalog.CommandRow row : rows) {
                if (matches(row, favorites, recents)) {
                    total++;
                }
            }
            return total;
        }
    }

    private enum SortMode {
        TOP("Top"),
        NAME("A-Z"),
        RECENT("Recent"),
        VANILLA("Vanilla");

        private final String label;

        SortMode(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }
    }
}
