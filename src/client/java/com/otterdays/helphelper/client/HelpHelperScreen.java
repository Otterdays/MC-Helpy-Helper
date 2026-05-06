package com.otterdays.helphelper.client;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
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
import com.otterdays.helphelper.network.OpenHelpPayload.CommandEntry;

/** Nearly full-window help browser with search, filtering, and flexible command actions. */
public final class HelpHelperScreen extends Screen {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final HelpHelperConfig CFG = HelpHelperConfig.get();
    private static final List<String> CATEGORY_ORDER = List.of("Server/Modded", "Chat", "Social", "World",
        "Worldgen", "Player", "Movement", "Build", "Inventory", "Storage", "Entities", "Utility", "Debug",
        "Admin", "Server", "Visual", "Transport", "Advanced");
    private static final int SCROLLBAR_WIDTH = 8;
    private static final int SCROLLBAR_GAP = 2;
    private static final int DETAIL_WIDTH = 190;
    private static final int DETAIL_GAP = 10;
    private static final int BADGE_WIDTH = 22;
    private static final int MIN_CATEGORY_WIDTH = 46;
    private static final int MAX_CATEGORY_WIDTH = 92;
    private static final double PAGE_SCROLL_FACTOR = 0.88;

    // GLFW key constants used by keyPressed switch.
    private static final int KEY_DOWN = 264;
    private static final int KEY_UP = 265;
    private static final int KEY_PAGE_UP = 266;
    private static final int KEY_PAGE_DOWN = 267;
    private static final int KEY_HOME = 268;
    private static final int KEY_END = 269;
    private static final int KEY_ENTER = 257;
    private static final int KEY_KP_ENTER = 335;
    private static final int KEY_C = 67;
    private static final int KEY_D = 68;
    private static final int KEY_F = 70;

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
            CommandCatalog.CommandRow row = CommandCatalog.row(command.command(), command.root());
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
        int gap = CFG.buttonGap;
        int closeW = CFG.closeButtonWidth;
        int densityW = CFG.densityButtonWidth;
        int actionW = Math.min(CFG.actionButtonWidth, Math.max(72, (listRight - listLeft) / 5));
        int controlAreaW = Math.max(120, listRight - listLeft);
        int minSearchW = Math.min(CFG.searchMinWidth, Math.max(64, controlAreaW / 3));
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
        int searchW = Math.max(64, actionLeft - listLeft - gap);

        searchBox = new EditBox(minecraft.font, listLeft, topControls, searchW, controlH, Component.literal("Search"));
        searchBox.setResponder(this::applyFilter);
        searchBox.setHint(Component.literal(ChatFormatting.GRAY + "Search commands..."));
        addRenderableWidget(searchBox);

        addRenderableWidget(Button.builder(Component.literal(clickAction.label()), btn -> {
            clickAction = clickAction.next();
            rebuildWidgetsKeepingSearch();
            saveState();
        }).bounds(actionLeft, topControls, actionW, controlH).build());

        addRenderableWidget(Button.builder(Component.literal(compactRows ? "Compact" : "Roomy"), btn -> {
            compactRows = !compactRows;
            rebuildWidgetsKeepingSearch();
            saveState();
        }).bounds(densityLeft, topControls, densityW, controlH).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> minecraft.setScreen(null))
            .bounds(closeLeft, topControls, closeW, controlH)
            .build());

        int quickY = topControls + controlH + 6;
        addQuickFilterButtons(quickY, controlH);
        int modeY = quickY + controlH + 3;
        addModeButtons(modeY, controlH);
        addCategoryButtons(modeY + controlH + 3, controlH);
        applyFilter(searchBox.getValue());
    }

    private int margin() {
        return Math.max(CFG.margin, Math.min(width, height) / 42);
    }

    private Path defaultStateFile() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("helphelper").resolve("ui.json");
    }

    private void computeListLayout() {
        int m = margin();
        int controlH = Math.max(CFG.controlHeight, minecraft.font.lineHeight + 10);
        int headerBlockHeight = (minecraft.font.lineHeight * 2) + 34;
        listLeft = m;
        int available = Math.max(40, width - (m * 2));
        int details = available >= 460 ? Math.min(CFG.detailPanelWidth, available / 3) : 0;
        listRight = Math.max(listLeft + 40, width - m - details - (details > 0 ? CFG.detailPanelGap : 0));
        int controlRowsReserve = 5;
        int topSpace = headerBlockHeight + (controlRowsReserve * controlH) + 20;
        int maxTopSpace = Math.max(140, height - m - 84);
        topSpace = Math.min(topSpace, maxTopSpace);
        listTop = m + topSpace;
        listBottom = Math.max(listTop + 60, height - m);

        clampScrollForViewport();
    }

    private void applyFilter(String raw) {
        String query = raw == null ? "" : raw.trim().toLowerCase(java.util.Locale.ROOT);
        visible = allCommands.stream()
            .filter(c -> quickFilter.matches(c, favoriteCommands, recentCommands))
            .filter(c -> selectedCategory.isEmpty() || c.category().equals(selectedCategory))
            .filter(c -> query.isEmpty() || c.matches(query))
            .sorted((a, b) -> compareRows(a, b, query))
            .toList();
        selectedIndex = visible.isEmpty() ? -1 : Math.max(0, Math.min(selectedIndex, visible.size() - 1));
        scrollPixels = 0.0;
        draggingScrollbar = false;
        clampScrollForViewport();
    }

    private void addCategoryButtons(int y, int buttonH) {
        int x = listLeft;
        int gap = 5;
        x = addCategoryButton("All", "", allCommands.size(), x, y, buttonH) + gap;
        int rowY = y;
        for (String category : categories) {
            if (x >= listRight - 56) {
                rowY += buttonH + 4;
                x = listLeft;
            }
            if (rowY + buttonH >= listTop - 6) {
                return;
            }
            int needed = Math.max(CFG.minCategoryWidth, minecraft.font.width(category) + 24);
            if (x > listLeft && x + needed > listRight) {
                rowY += buttonH + 4;
                x = listLeft;
            }
            if (rowY + buttonH >= listTop - 6) {
                return;
            }
            x = addCategoryButton(category, category, categoryCounts.getOrDefault(category, 0), x, rowY, buttonH) + gap;
        }
    }

    private void addQuickFilterButtons(int y, int buttonH) {
        int x = listLeft;
        int gap = 5;
        for (QuickFilter filter : QuickFilter.values()) {
            String label = filter.label();
            int count = filter.count(allCommands, favoriteCommands, recentCommands);
            String text = (quickFilter == filter ? "[" + label + "]" : label) + " " + count;
            int w = Math.min(Math.max(CFG.minCategoryWidth, minecraft.font.width(text) + 18),
                Math.max(CFG.minCategoryWidth, listRight - x));
            if (x > listLeft && x + w > listRight) {
                y += buttonH + 4;
                x = listLeft;
            }
            if (y + buttonH >= listTop - 6) {
                return;
            }
            w = Math.min(Math.max(CFG.minCategoryWidth, minecraft.font.width(text) + 18),
                Math.max(CFG.minCategoryWidth, listRight - x));
            addRenderableWidget(Button.builder(Component.literal(text), btn -> {
                quickFilter = filter;
                rebuildWidgetsKeepingSearch();
                saveState();
            }).bounds(x, y, w, buttonH).build());
            x += w + gap;
        }
    }

    private void addModeButtons(int y, int buttonH) {
        int x = listLeft;
        int gap = 5;
        for (SortMode mode : SortMode.values()) {
            String text = sortMode == mode ? "[" + mode.label() + "]" : mode.label();
            int w = Math.min(Math.max(CFG.minCategoryWidth, minecraft.font.width(text) + 18),
                Math.max(CFG.minCategoryWidth, listRight - x));
            if (x > listLeft && x + w > listRight) {
                y += buttonH + 4;
                x = listLeft;
            }
            if (y + buttonH >= listTop - 6) {
                return;
            }
            w = Math.min(Math.max(CFG.minCategoryWidth, minecraft.font.width(text) + 18),
                Math.max(CFG.minCategoryWidth, listRight - x));
            addRenderableWidget(Button.builder(Component.literal(text), btn -> {
                sortMode = mode;
                rebuildWidgetsKeepingSearch();
                saveState();
            }).bounds(x, y, w, buttonH).build());
            x += w + gap;
        }
        int clearW = Math.min(72, Math.max(CFG.minCategoryWidth, listRight - x));
        if (clearW >= CFG.minCategoryWidth) {
            if (x > listLeft && x + clearW > listRight) {
                y += buttonH + 4;
                x = listLeft;
                clearW = Math.min(72, Math.max(CFG.minCategoryWidth, listRight - x));
            }
            if (y + buttonH >= listTop - 6) {
                return;
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
            }).bounds(x, y, clearW, buttonH).build());
        }
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
    }

    private void drawBackdrop(GuiGraphicsExtractor gfx) {
        int m = margin();

        gfx.fill(m, m, width - m, height - m, ARGB.color(228, 12, 15, 22));
        gfx.fillGradient(m + 14, m + 8, width - m - 14, Math.min(height, m + 38), ARGB.color(255, 72, 64, 42),
            ARGB.color(255, 28, 32, 50));
        gfx.fillGradient(m + 14, Math.min(height, m + 38), width - m - 14, Math.min(height, m + 46),
            ARGB.color(120, 255, 226, 120), ARGB.color(0, 255, 226, 120));

        gfx.centeredText(minecraft.font, Component.literal("Help Helper").withStyle(ChatFormatting.GOLD), width / 2,
            m + 8, ARGB.color(255, 255, 226, 120));

        String counter = ChatFormatting.YELLOW.toString() + visible.size() + ChatFormatting.GRAY + " / "
            + allCommands.size() + ChatFormatting.WHITE + " commands";
        gfx.text(minecraft.font, counter, listLeft, m + minecraft.font.lineHeight + 20, ARGB.color(255, 200, 200, 200));
        String filterLine = ellipsize(activeFilterText(), Math.max(60, listRight - listLeft));
        gfx.text(minecraft.font, filterLine, listLeft, m + minecraft.font.lineHeight + 30,
            ARGB.color(255, 145, 188, 206));
        String actionHint = (clickAction == ClickAction.RUN && selectedIndex >= 0 && selectedIndex < visible.size()
            && visible.get(selectedIndex).info().risky())
                ? clickAction.helpText() + ChatFormatting.YELLOW + "  (risky - will open chat for review)"
                : clickAction.helpText();
        String shortcuts = "C mode  D density  F favorite";
        int shortcutsW = minecraft.font.width(shortcuts);
        int hintMax = Math.max(40, listRight - listLeft - shortcutsW - 10);
        gfx.text(minecraft.font, Component.literal(ellipsize(actionHint, hintMax)).withStyle(ChatFormatting.GRAY),
            listLeft, listTop - 18, ARGB.color(255, 180, 186, 204));
        if (shortcutsW < listRight - listLeft - 24) {
            gfx.text(minecraft.font, shortcuts, Math.max(listLeft, listRight - shortcutsW), listTop - 18,
                ARGB.color(255, 130, 136, 150));
        }

        outlineRect(gfx, listLeft - 4, listTop - 4, listRight + 4, listBottom + 4, ARGB.color(200, 90, 100, 150));
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
                boolean hovered =
                    mouseX >= listLeft && mouseX < contentRight() && mouseY >= yi && mouseY < yi + lineHeight - 1;
                CommandCatalog.CommandRow row = visible.get(i);
                int rowBg = selected ? ARGB.color(164, 70, 94, 156)
                    : (hovered ? ARGB.color(126, 52, 70, 108) : ARGB.color(78, 24, 30, 48));
                if (row.info().risky()) {
                    rowBg = selected ? ARGB.color(162, 104, 66, 46)
                        : (hovered ? ARGB.color(124, 86, 50, 36) : ARGB.color(82, 48, 34, 28));
                }
                gfx.fill(listLeft + 2, yi, contentRight(), yi + lineHeight - 3, rowBg);
                gfx.fill(listLeft + 2, yi, listLeft + 5, yi + lineHeight - 3, categoryColor(row.category()));

                int badgeRight = contentRight() - 4;
                int textRight = badgeRight - CFG.badgeWidth - 4;
                int textX = listLeft + (favoriteCommands.contains(row.command()) ? 16 : 10);
                String text = ellipsize(row.command(), textRight - textX);
                int ty = yi + Math.max(0, (lineHeight - minecraft.font.lineHeight - 4) / 2);

                int textColor = row.info().risky() ? ARGB.color(255, 255, 204, 164)
                    : (row.info().vanilla() ? ARGB.color(255, 164, 238, 226) : ARGB.color(255, 214, 198, 255));
                MutableComponent styled = Component.literal(text).withStyle(ChatFormatting.UNDERLINE);
                gfx.text(minecraft.font, styled, textX, ty, textColor, true);
                if (favoriteCommands.contains(row.command())) {
                    gfx.text(minecraft.font, "*", listLeft + 7, ty, ARGB.color(255, 255, 226, 120));
                }
                drawRowBadge(gfx, row, badgeRight - CFG.badgeWidth, yi + 3, badgeRight, yi + lineHeight - 6);
            }
        } finally {
            gfx.disableScissor();
        }
        drawScrollbar(gfx);
    }

    private void drawRowBadge(GuiGraphicsExtractor gfx, CommandCatalog.CommandRow row, int x0, int y0, int x1, int y1) {
        String label = row.info().risky() ? "!" : (row.info().vanilla() ? "V" : "M");
        int bg = row.info().risky() ? ARGB.color(156, 126, 60, 34)
            : (row.info().vanilla() ? ARGB.color(132, 34, 86, 82) : ARGB.color(132, 72, 58, 118));
        int fg = row.info().risky() ? ARGB.color(255, 255, 194, 130)
            : (row.info().vanilla() ? ARGB.color(255, 166, 244, 226) : ARGB.color(255, 220, 206, 255));
        gfx.fill(x0, y0, x1, y1, bg);
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

        CommandCatalog.CommandRow row = visible.get(selectedIndex >= 0 ? selectedIndex : 0);
        CommandCatalog.CommandInfo info = row.info();
        int x = panelLeft + 8;
        int y = panelTop + 8;
        int maxW = panelRight - x - 8;
        int footerTop = Math.max(y + 72, panelBottom - 72);

        gfx.fill(panelLeft, panelTop, panelLeft + 3, panelBottom, categoryColor(info.category()));
        gfx.text(minecraft.font, ellipsize(info.title(), maxW), x, y, CFG.parseColor(CFG.colorFavoriteStar), true);
        y += minecraft.font.lineHeight + 8;
        gfx.text(minecraft.font, ellipsize(row.command(), maxW), x, y, CFG.parseColor(CFG.colorRiskyText), true);
        y += minecraft.font.lineHeight + 7;
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
            gfx.text(minecraft.font, "Templates", x, y, ARGB.color(255, 255, 226, 120));
            y += minecraft.font.lineHeight + 5;
            List<PresetHit> hits = new ArrayList<>();
            for (String preset : info.presets()) {
                if (y + 18 > panelBottom - 6) {
                    break;
                }
                boolean hovered = mouseX >= x && mouseX < panelRight - 8 && mouseY >= y && mouseY < y + 17;
                gfx.fill(x, y, panelRight - 8, y + 17,
                    hovered ? ARGB.color(142, 86, 96, 120) : ARGB.color(86, 42, 46, 64));
                gfx.text(minecraft.font, ellipsize(preset, maxW - 8), x + 4, y + 4, ARGB.color(255, 210, 245, 255));
                hits.add(new PresetHit(preset, x, y, panelRight - 8, y + 17));
                y += 20;
            }
            presetHits = hits;
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
        gfx.fill(trackLeft, listTop, listRight, listBottom, ARGB.color(92, 8, 10, 18));
        int thumbColor = draggingScrollbar ? ARGB.color(220, 160, 180, 240) : ARGB.color(190, 130, 150, 210);
        gfx.fill(trackLeft, thumbTop, listRight, thumbBottom, thumbColor);
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
        scrollBy(direction * viewportHeight() * PAGE_SCROLL_FACTOR);
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
        ClickAction action = row.info().risky() && clickAction == ClickAction.RUN ? ClickAction.FILL_CHAT : clickAction;
        markRecent(row.command());
        runCommand(row.command(), action);
    }

    private void markRecent(String command) {
        // Remove any existing occurrence so we don't end up with duplicates during the add.
        recentCommands.remove(command);
        recentCommands.add(0, command);
        while (recentCommands.size() > 12) {
            recentCommands.remove(recentCommands.size() - 1);
        }
        saveState();
    }

    private void runCommand(String command, ClickAction action) {
        if (minecraft.player == null) {
            return;
        }
        switch (action) {
            case RUN -> {
                Screen.clickCommandAction(minecraft.player, command, this);
                minecraft.setScreen(null);
            }
            case COPY -> {
                minecraft.keyboardHandler.setClipboard(command);
                markRecent(command);
            }
            case FILL_CHAT -> {
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
            runCommandRow(row);
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
        if (row >= 0 && row < visible.size()) {
            selectedIndex = row;
        }
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
        switch (event.key()) {
            case KEY_DOWN -> {
                moveSelection(1);
                return true;
            }
            case KEY_UP -> {
                moveSelection(-1);
                return true;
            }
            case KEY_PAGE_UP -> {
                pageScroll(-1.0);
                return true;
            }
            case KEY_PAGE_DOWN -> {
                pageScroll(1.0);
                return true;
            }
            case KEY_HOME -> {
                scrollTo(0.0);
                selectedIndex = visible.isEmpty() ? -1 : 0;
                return true;
            }
            case KEY_END -> {
                scrollTo(maxScroll());
                selectedIndex = visible.isEmpty() ? -1 : visible.size() - 1;
                return true;
            }
            case KEY_ENTER, KEY_KP_ENTER -> {
                runCommandRow(selectedIndex >= 0 ? selectedIndex : 0);
                return true;
            }
            case KEY_C -> {
                clickAction = clickAction.next();
                rebuildWidgetsKeepingSearch();
                saveState();
                return true;
            }
            case KEY_D -> {
                compactRows = !compactRows;
                rebuildWidgetsKeepingSearch();
                saveState();
                return true;
            }
            case KEY_F -> {
                toggleFavorite();
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void toggleFavorite() {
        if (selectedIndex < 0 || selectedIndex >= visible.size()) {
            return;
        }
        String command = visible.get(selectedIndex).command();
        if (!favoriteCommands.add(command)) {
            favoriteCommands.remove(command);
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
        String cmd = row.command().toLowerCase(java.util.Locale.ROOT);
        String root = row.root().toLowerCase(java.util.Locale.ROOT);
        String title = row.info().title().toLowerCase(java.util.Locale.ROOT);
        String category = row.info().category().toLowerCase(java.util.Locale.ROOT);
        String desc = row.info().description().toLowerCase(java.util.Locale.ROOT);

        if (cmd.startsWith("/" + query)) score += 500;
        if (root.equals(query)) score += 450;
        if (title.startsWith(query)) score += 350;
        if (category.startsWith(query)) score += 220;
        if (cmd.contains(query)) score += 150;
        if (title.contains(query)) score += 120;
        if (desc.contains(query)) score += 40;
        if (row.info().aliases().stream().anyMatch(a -> a.toLowerCase(java.util.Locale.ROOT).contains(query))) score += 100;
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
