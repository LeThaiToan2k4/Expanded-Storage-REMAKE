package com.thaistoanf.expandedstorage.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pure, version-agnostic coordinate math for the Expanded Storage T-shape GUI.
 *
 * <p>All coordinates inside {@link Layout} are relative to the overall bounding box
 * {@code (0, 0, totalWidth, totalHeight)} anchored at {@code (HandledScreen.x, HandledScreen.y)},
 * except off-screen disabled slots which are placed at {@link #OFFSCREEN_COORD}.</p>
 */
public final class LayoutCalculator {
    public static final int SLOT_SIZE = 18;
    public static final int BORDER_PAD = 7;
    public static final int HEADER_HEIGHT = 17;
    public static final int CHEST_BOTTOM_PAD = 4;
    public static final int SEAM_OVERLAP = 4;

    public static final int PLAYER_WIDTH = 176;
    public static final int PLAYER_HEIGHT = 96;
    public static final int PLAYER_INV_TOP_OFFSET = 14;
    public static final int PLAYER_HOTBAR_TOP_OFFSET = 72;

    public static final int SPLIT_DIVIDER_GAP = 10;
    public static final int TAB_BAR_OFFSET = 16;
    public static final int TAB_HEIGHT = 19;
    public static final int BUTTON_SIZE = 12;
    public static final int BUTTON_GAP = 2;
    public static final int SCREEN_MARGIN = 6;
    public static final int OFFSCREEN_COORD = -10000;

    private LayoutCalculator() {
    }

    public record Rect(int x, int y, int width, int height) {
        public int right() {
            return x + width;
        }

        public int bottom() {
            return y + height;
        }

        public boolean contains(double px, double py) {
            return px >= x && px < x + width && py >= y && py < y + height;
        }
    }

    public record SlotPos(int x, int y, boolean visible) {
    }

    public record TabSlice(int index, int startSlot, int endSlotExclusive, Rect bounds) {
        public int slotCount() {
            return endSlotExclusive - startSlot;
        }
    }

    public record Layout(
            GuiMode requestedMode,
            GuiMode effectiveMode,
            boolean autoFallback,
            int totalWidth,
            int totalHeight,
            int cols,
            int rows,
            boolean splitWings,
            int dividerX,
            int activeTab,
            int tabCount,
            Rect chestPanel,
            Rect playerPanel,
            Rect modeButton,
            Rect themeButton,
            int titleX,
            int titleY,
            int playerTitleX,
            int playerTitleY,
            List<TabSlice> tabs,
            List<SlotPos> chestSlots,
            List<SlotPos> playerSlots
    ) {
        /**
         * Returns true if {@code (relX, relY)} (relative to screen origin {@code (x, y)})
         * lies inside any visible interactive region of the T-shape container.
         */
        public boolean containsPoint(double relX, double relY) {
            if (chestPanel.contains(relX, relY) || playerPanel.contains(relX, relY)) {
                return true;
            }
            if (modeButton.contains(relX, relY) || themeButton.contains(relX, relY)) {
                return true;
            }
            for (TabSlice tab : tabs) {
                if (tab.bounds().contains(relX, relY)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static Layout compute(
            ChestTier tier,
            boolean isDouble,
            GuiMode requestedMode,
            int activeTab,
            int screenWidth,
            int screenHeight
    ) {
        GuiMode mode = requestedMode == null ? GuiMode.ADAPTIVE : requestedMode;

        // Split-Wing is dedicated to double chests; single chests gracefully use Adaptive.
        GuiMode candidate = (mode == GuiMode.SPLIT_WING && !isDouble) ? GuiMode.ADAPTIVE : mode;
        boolean autoFallback = false;

        if (candidate != GuiMode.SMART_TABBED) {
            Rect testChest = measureNonTabbedChest(tier, isDouble, candidate);
            int testTotalH = testChest.height() + PLAYER_HEIGHT - SEAM_OVERLAP;
            if (screenWidth > 0 && screenHeight > 0
                    && (testChest.width() + SCREEN_MARGIN * 2 > screenWidth
                    || testTotalH + SCREEN_MARGIN * 2 > screenHeight)) {
                candidate = GuiMode.SMART_TABBED;
                autoFallback = true;
            }
        }

        if (candidate == GuiMode.SPLIT_WING) {
            return buildSplitWing(tier, mode, false);
        }
        if (candidate == GuiMode.ADAPTIVE) {
            return buildAdaptive(tier, isDouble, mode, false);
        }
        return buildSmartTabbed(tier, isDouble, mode, autoFallback, activeTab, screenWidth, screenHeight);
    }

    private static Rect measureNonTabbedChest(ChestTier tier, boolean isDouble, GuiMode mode) {
        if (mode == GuiMode.SPLIT_WING && isDouble) {
            ChestTier.Grid wing = tier.splitWing();
            int w = BORDER_PAD * 2 + wing.cols() * SLOT_SIZE * 2 + SPLIT_DIVIDER_GAP;
            int h = HEADER_HEIGHT + wing.rows() * SLOT_SIZE + CHEST_BOTTOM_PAD;
            return new Rect(0, 0, Math.max(PLAYER_WIDTH, w), h);
        }
        ChestTier.Grid grid = tier.adaptive(isDouble);
        int w = Math.max(PLAYER_WIDTH, BORDER_PAD * 2 + grid.cols() * SLOT_SIZE);
        int h = HEADER_HEIGHT + grid.rows() * SLOT_SIZE + CHEST_BOTTOM_PAD;
        return new Rect(0, 0, w, h);
    }

    private static Layout buildAdaptive(ChestTier tier, boolean isDouble, GuiMode requestedMode, boolean autoFallback) {
        int totalSlots = tier.slots(isDouble);
        ChestTier.Grid grid = tier.adaptive(isDouble);
        int cols = grid.cols();
        int rows = grid.rows();

        int chestW = Math.max(PLAYER_WIDTH, BORDER_PAD * 2 + cols * SLOT_SIZE);
        int chestH = HEADER_HEIGHT + rows * SLOT_SIZE + CHEST_BOTTOM_PAD;
        Rect chestPanel = new Rect(0, 0, chestW, chestH);

        int playerX = (chestW - PLAYER_WIDTH) / 2;
        int playerY = chestH - SEAM_OVERLAP;
        Rect playerPanel = new Rect(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        int totalW = chestW;
        int totalH = playerY + PLAYER_HEIGHT;

        int gridStartX = (chestW - cols * SLOT_SIZE) / 2 + 1;
        int gridStartY = HEADER_HEIGHT + 1;

        List<SlotPos> chestSlots = new ArrayList<>(totalSlots);
        for (int i = 0; i < totalSlots; i++) {
            int col = i % cols;
            int row = i / cols;
            chestSlots.add(new SlotPos(gridStartX + col * SLOT_SIZE, gridStartY + row * SLOT_SIZE, true));
        }

        List<SlotPos> playerSlots = buildPlayerSlots(playerPanel);
        Rect themeBtn = new Rect(chestPanel.right() - BORDER_PAD - BUTTON_SIZE, chestPanel.y() + 3, BUTTON_SIZE, BUTTON_SIZE);
        Rect modeBtn = new Rect(themeBtn.x() - BUTTON_GAP - BUTTON_SIZE, chestPanel.y() + 3, BUTTON_SIZE, BUTTON_SIZE);

        return new Layout(
                requestedMode,
                GuiMode.ADAPTIVE,
                autoFallback,
                totalW,
                totalH,
                cols,
                rows,
                false,
                -1,
                0,
                1,
                chestPanel,
                playerPanel,
                modeBtn,
                themeBtn,
                chestPanel.x() + 8,
                chestPanel.y() + 6,
                playerPanel.x() + 8,
                playerPanel.y() + 3,
                Collections.emptyList(),
                Collections.unmodifiableList(chestSlots),
                playerSlots
        );
    }

    private static Layout buildSplitWing(ChestTier tier, GuiMode requestedMode, boolean autoFallback) {
        ChestTier.Grid wing = tier.splitWing();
        int wingCols = wing.cols();
        int wingRows = wing.rows();
        int singleCount = tier.singleSlots();
        int totalSlots = tier.doubleSlots();

        int chestW = Math.max(PLAYER_WIDTH, BORDER_PAD * 2 + wingCols * SLOT_SIZE * 2 + SPLIT_DIVIDER_GAP);
        int chestH = HEADER_HEIGHT + wingRows * SLOT_SIZE + CHEST_BOTTOM_PAD;
        Rect chestPanel = new Rect(0, 0, chestW, chestH);

        int playerX = (chestW - PLAYER_WIDTH) / 2;
        int playerY = chestH - SEAM_OVERLAP;
        Rect playerPanel = new Rect(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        int totalW = chestW;
        int totalH = playerY + PLAYER_HEIGHT;

        int leftStartX = BORDER_PAD + 1;
        int rightStartX = BORDER_PAD + wingCols * SLOT_SIZE + SPLIT_DIVIDER_GAP + 1;
        int gridStartY = HEADER_HEIGHT + 1;
        int dividerX = BORDER_PAD + wingCols * SLOT_SIZE + SPLIT_DIVIDER_GAP / 2;

        List<SlotPos> chestSlots = new ArrayList<>(totalSlots);
        for (int i = 0; i < totalSlots; i++) {
            boolean rightWing = i >= singleCount;
            int local = rightWing ? (i - singleCount) : i;
            int col = local % wingCols;
            int row = local / wingCols;
            int sx = (rightWing ? rightStartX : leftStartX) + col * SLOT_SIZE;
            int sy = gridStartY + row * SLOT_SIZE;
            chestSlots.add(new SlotPos(sx, sy, true));
        }

        List<SlotPos> playerSlots = buildPlayerSlots(playerPanel);
        Rect themeBtn = new Rect(chestPanel.right() - BORDER_PAD - BUTTON_SIZE, chestPanel.y() + 3, BUTTON_SIZE, BUTTON_SIZE);
        Rect modeBtn = new Rect(themeBtn.x() - BUTTON_GAP - BUTTON_SIZE, chestPanel.y() + 3, BUTTON_SIZE, BUTTON_SIZE);

        return new Layout(
                requestedMode,
                GuiMode.SPLIT_WING,
                autoFallback,
                totalW,
                totalH,
                wingCols * 2,
                wingRows,
                true,
                dividerX,
                0,
                1,
                chestPanel,
                playerPanel,
                modeBtn,
                themeBtn,
                chestPanel.x() + 8,
                chestPanel.y() + 6,
                playerPanel.x() + 8,
                playerPanel.y() + 3,
                Collections.emptyList(),
                Collections.unmodifiableList(chestSlots),
                playerSlots
        );
    }

    private static Layout buildSmartTabbed(
            ChestTier tier,
            boolean isDouble,
            GuiMode requestedMode,
            boolean autoFallback,
            int requestedTab,
            int screenWidth,
            int screenHeight
    ) {
        int totalSlots = tier.slots(isDouble);
        ChestTier.Grid baseGrid = tier.tabbed(isDouble);
        int cols = baseGrid.cols();
        int rows = baseGrid.rows();

        // Clamp further if the window is extremely small (e.g. high GUI scale in a tiny window).
        if (screenWidth > 0) {
            int maxCols = Math.max(9, (screenWidth - SCREEN_MARGIN * 2 - BORDER_PAD * 2) / SLOT_SIZE);
            cols = Math.min(cols, maxCols);
        }
        if (screenHeight > 0) {
            int overhead = TAB_BAR_OFFSET + HEADER_HEIGHT + CHEST_BOTTOM_PAD + PLAYER_HEIGHT - SEAM_OVERLAP + SCREEN_MARGIN * 2;
            int maxRows = Math.max(2, (screenHeight - overhead) / SLOT_SIZE);
            rows = Math.min(rows, maxRows);
        }

        int pageCapacity = cols * rows;
        int tabCount = Math.max(1, (totalSlots + pageCapacity - 1) / pageCapacity);
        int clampedTab = Math.max(0, Math.min(requestedTab, tabCount - 1));

        int chestY = TAB_BAR_OFFSET;
        int chestW = Math.max(PLAYER_WIDTH, BORDER_PAD * 2 + cols * SLOT_SIZE);
        int chestH = HEADER_HEIGHT + rows * SLOT_SIZE + CHEST_BOTTOM_PAD;
        Rect chestPanel = new Rect(0, chestY, chestW, chestH);

        int playerX = (chestW - PLAYER_WIDTH) / 2;
        int playerY = chestY + chestH - SEAM_OVERLAP;
        Rect playerPanel = new Rect(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        int totalW = chestW;
        int totalH = playerY + PLAYER_HEIGHT;

        // Build top docked tabs cleanly along the upper-left edge of chestPanel.
        int availableTabSpan = chestW - 8;
        int tabWidth = Math.min(36, Math.max(22, (availableTabSpan - (tabCount - 1) * 2) / tabCount));
        List<TabSlice> tabs = new ArrayList<>(tabCount);
        for (int t = 0; t < tabCount; t++) {
            int start = t * pageCapacity;
            int end = Math.min(totalSlots, start + pageCapacity);
            int tx = 4 + t * (tabWidth + 2);
            Rect tabBounds = new Rect(tx, 0, tabWidth, TAB_HEIGHT);
            tabs.add(new TabSlice(t, start, end, tabBounds));
        }

        TabSlice currentSlice = tabs.get(clampedTab);
        int gridStartX = (chestW - cols * SLOT_SIZE) / 2 + 1;
        int gridStartY = chestY + HEADER_HEIGHT + 1;

        List<SlotPos> chestSlots = new ArrayList<>(totalSlots);
        for (int i = 0; i < totalSlots; i++) {
            if (i >= currentSlice.startSlot() && i < currentSlice.endSlotExclusive()) {
                int local = i - currentSlice.startSlot();
                int col = local % cols;
                int row = local / cols;
                chestSlots.add(new SlotPos(gridStartX + col * SLOT_SIZE, gridStartY + row * SLOT_SIZE, true));
            } else {
                chestSlots.add(new SlotPos(OFFSCREEN_COORD, OFFSCREEN_COORD, false));
            }
        }

        List<SlotPos> playerSlots = buildPlayerSlots(playerPanel);
        Rect themeBtn = new Rect(chestPanel.right() - BORDER_PAD - BUTTON_SIZE, chestPanel.y() + 3, BUTTON_SIZE, BUTTON_SIZE);
        Rect modeBtn = new Rect(themeBtn.x() - BUTTON_GAP - BUTTON_SIZE, chestPanel.y() + 3, BUTTON_SIZE, BUTTON_SIZE);

        return new Layout(
                requestedMode,
                GuiMode.SMART_TABBED,
                autoFallback,
                totalW,
                totalH,
                cols,
                rows,
                false,
                -1,
                clampedTab,
                tabCount,
                chestPanel,
                playerPanel,
                modeBtn,
                themeBtn,
                chestPanel.x() + 8,
                chestPanel.y() + 6,
                playerPanel.x() + 8,
                playerPanel.y() + 3,
                Collections.unmodifiableList(tabs),
                Collections.unmodifiableList(chestSlots),
                playerSlots
        );
    }

    private static List<SlotPos> buildPlayerSlots(Rect playerPanel) {
        List<SlotPos> slots = new ArrayList<>(36);
        int startX = playerPanel.x() + BORDER_PAD + 1;
        int invStartY = playerPanel.y() + PLAYER_INV_TOP_OFFSET + 1;
        // 27 main player inventory slots (indices 9..35 in PlayerInventory)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slots.add(new SlotPos(startX + col * SLOT_SIZE, invStartY + row * SLOT_SIZE, true));
            }
        }
        // 9 hotbar slots (indices 0..8 in PlayerInventory)
        int hotbarY = playerPanel.y() + PLAYER_HOTBAR_TOP_OFFSET + 1;
        for (int col = 0; col < 9; col++) {
            slots.add(new SlotPos(startX + col * SLOT_SIZE, hotbarY, true));
        }
        return Collections.unmodifiableList(slots);
    }
}
