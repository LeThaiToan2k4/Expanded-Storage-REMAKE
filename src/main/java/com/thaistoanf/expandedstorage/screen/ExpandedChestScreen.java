package com.thaistoanf.expandedstorage.screen;

import com.thaistoanf.expandedstorage.compat.RenderCompat;
import com.thaistoanf.expandedstorage.core.ClientConfig;
import com.thaistoanf.expandedstorage.core.GuiMode;
import com.thaistoanf.expandedstorage.core.LayoutCalculator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic 9-slice T-shape chest screen supporting Adaptive Grid, Split-Wing, and Smart Tabbed modes,
 * automatic small-screen fallback, click-through flanks, and instant Light/Dark theme switching.
 */
@Environment(EnvType.CLIENT)
public class ExpandedChestScreen extends HandledScreen<ExpandedChestScreenHandler> {
    private LayoutCalculator.Layout layout;
    private int activeTab = 0;
    private boolean suppressNextRelease = false;

    public ExpandedChestScreen(ExpandedChestScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.layout = LayoutCalculator.compute(
                handler.getTier(), handler.isDoubleChest(), ClientConfig.get().guiMode(), 0, 0, 0
        );
        this.backgroundWidth = this.layout.totalWidth();
        this.backgroundHeight = this.layout.totalHeight();
    }

    @Override
    protected void init() {
        recomputeLayout();
    }

    private void recomputeLayout() {
        this.layout = LayoutCalculator.compute(
                this.handler.getTier(),
                this.handler.isDoubleChest(),
                ClientConfig.get().guiMode(),
                this.activeTab,
                this.width,
                this.height
        );
        this.activeTab = this.layout.activeTab();
        this.backgroundWidth = this.layout.totalWidth();
        this.backgroundHeight = this.layout.totalHeight();
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;
        this.titleX = this.layout.titleX();
        this.titleY = this.layout.titleY();
        this.playerInventoryTitleX = this.layout.playerTitleX();
        this.playerInventoryTitleY = this.layout.playerTitleY();
        this.handler.applyLayout(this.layout);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
        this.drawControlTooltips(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        boolean dark = ClientConfig.get().darkGui();
        Identifier tex = RenderCompat.guiTexture(dark);
        double relX = mouseX - this.x;
        double relY = mouseY - this.y;

        // 1. Inactive Smart Tabs behind top chest edge
        if (this.layout.effectiveMode() == GuiMode.SMART_TABBED) {
            for (LayoutCalculator.TabSlice tab : this.layout.tabs()) {
                if (tab.index() != this.layout.activeTab()) {
                    drawSingleTab(context, tex, dark, tab, false);
                }
            }
        }

        // 2. Seamless T-shape chest + player inventory panels
        RenderCompat.drawTShapePanels(context, tex, this.x, this.y, this.layout.chestPanel(), this.layout.playerPanel());

        // 3. Active Smart Tab merged seamlessly onto the top chest edge
        if (this.layout.effectiveMode() == GuiMode.SMART_TABBED) {
            for (LayoutCalculator.TabSlice tab : this.layout.tabs()) {
                if (tab.index() == this.layout.activeTab()) {
                    drawSingleTab(context, tex, dark, tab, true);
                }
            }
        }

        // 4. Split-Wing center vertical divider groove
        if (this.layout.splitWings() && this.layout.dividerX() >= 0) {
            int divTop = this.y + this.layout.chestPanel().y() + LayoutCalculator.HEADER_HEIGHT;
            int divHeight = this.layout.rows() * LayoutCalculator.SLOT_SIZE;
            RenderCompat.drawSplitDivider(context, tex, this.x + this.layout.dividerX(), divTop, divHeight);
        }

        // 5. Crisp recessed 18x18 slot frames
        RenderCompat.drawSlotFrames(context, tex, this.x, this.y, this.layout.chestSlots());
        RenderCompat.drawSlotFrames(context, tex, this.x, this.y, this.layout.playerSlots());

        // 6. Header mode & dark/light icon buttons
        LayoutCalculator.Rect modeRect = this.layout.modeButton();
        int modeIconU = switch (ClientConfig.get().guiMode()) {
            case ADAPTIVE -> 0;
            case SPLIT_WING -> 12;
            case SMART_TABBED -> 24;
        };
        RenderCompat.drawIconButton(
                context, tex,
                this.x + modeRect.x(), this.y + modeRect.y(),
                modeRect.contains(relX, relY),
                modeIconU, 36
        );

        LayoutCalculator.Rect themeRect = this.layout.themeButton();
        int themeIconU = dark ? 36 : 48;
        RenderCompat.drawIconButton(
                context, tex,
                this.x + themeRect.x(), this.y + themeRect.y(),
                themeRect.contains(relX, relY),
                themeIconU, 36
        );
    }

    private void drawSingleTab(
            DrawContext context,
            Identifier tex,
            boolean dark,
            LayoutCalculator.TabSlice tab,
            boolean active
    ) {
        LayoutCalculator.Rect b = tab.bounds();
        int tx = this.x + b.x();
        int ty = this.y + b.y();
        RenderCompat.drawTab(context, tex, tx, ty, b.width(), b.height(), active);

        String label = Integer.toString(tab.index() + 1);
        int textW = this.textRenderer.getWidth(label);
        int labelX = tx + (b.width() - textW) / 2;
        int labelY = ty + (active ? 5 : 7);
        context.drawText(this.textRenderer, label, labelX, labelY, RenderCompat.tabTextColor(dark, active), dark && active);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        boolean dark = ClientConfig.get().darkGui();
        int color = RenderCompat.titleColor(dark);

        int maxTitleWidth = Math.max(40, this.layout.modeButton().x() - this.titleX - 4);
        Text displayTitle = this.title;
        if (this.textRenderer.getWidth(displayTitle) > maxTitleWidth) {
            String trimmed = this.textRenderer.trimToWidth(displayTitle.getString(), maxTitleWidth - 6) + "...";
            displayTitle = Text.literal(trimmed);
        }

        RenderCompat.drawLabel(context, this.textRenderer, displayTitle, this.titleX, this.titleY, color, dark);
        RenderCompat.drawLabel(
                context, this.textRenderer, this.playerInventoryTitle,
                this.playerInventoryTitleX, this.playerInventoryTitleY, color, dark
        );
    }

    private void drawControlTooltips(DrawContext context, int mouseX, int mouseY) {
        if (!this.handler.getCursorStack().isEmpty()) {
            return;
        }
        double relX = mouseX - this.x;
        double relY = mouseY - this.y;

        if (this.layout.modeButton().contains(relX, relY)) {
            List<Text> lines = new ArrayList<>(3);
            GuiMode configured = ClientConfig.get().guiMode();
            lines.add(Text.translatable("gui.expandedstorage.button.mode", Text.translatable(configured.translationKey())));
            lines.add(Text.translatable(configured.translationKey() + ".desc").formatted(Formatting.GRAY));
            if (this.layout.autoFallback()) {
                lines.add(Text.translatable("gui.expandedstorage.mode.fallback_notice").formatted(Formatting.GOLD));
            } else if (configured == GuiMode.SPLIT_WING && !this.handler.isDoubleChest()) {
                lines.add(Text.translatable("gui.expandedstorage.mode.single_notice").formatted(Formatting.GRAY));
            }
            context.drawTooltip(this.textRenderer, lines, mouseX, mouseY);
            return;
        }

        if (this.layout.themeButton().contains(relX, relY)) {
            boolean dark = ClientConfig.get().darkGui();
            Text state = Text.translatable(dark ? "gui.expandedstorage.theme.dark" : "gui.expandedstorage.theme.light");
            List<Text> lines = List.of(
                    Text.translatable("gui.expandedstorage.button.theme", state),
                    Text.translatable("gui.expandedstorage.button.theme.desc").formatted(Formatting.GRAY)
            );
            context.drawTooltip(this.textRenderer, lines, mouseX, mouseY);
            return;
        }

        if (this.layout.effectiveMode() == GuiMode.SMART_TABBED) {
            for (LayoutCalculator.TabSlice tab : this.layout.tabs()) {
                if (tab.bounds().contains(relX, relY)) {
                    Text tip = Text.translatable(
                            "gui.expandedstorage.tab.tooltip",
                            tab.index() + 1,
                            this.layout.tabCount(),
                            tab.startSlot() + 1,
                            tab.endSlotExclusive()
                    );
                    context.drawTooltip(this.textRenderer, tip, mouseX, mouseY);
                    return;
                }
            }
        }
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        return !this.layout.containsPoint(mouseX - left, mouseY - top);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double relX = mouseX - this.x;
            double relY = mouseY - this.y;

            if (this.layout.modeButton().contains(relX, relY)) {
                ClientConfig.get().setGuiMode(ClientConfig.get().guiMode().next());
                this.activeTab = 0;
                recomputeLayout();
                playClickSound();
                this.suppressNextRelease = true;
                return true;
            }

            if (this.layout.themeButton().contains(relX, relY)) {
                ClientConfig.get().setDarkGui(!ClientConfig.get().darkGui());
                playClickSound();
                this.suppressNextRelease = true;
                return true;
            }

            if (this.layout.effectiveMode() == GuiMode.SMART_TABBED) {
                for (LayoutCalculator.TabSlice tab : this.layout.tabs()) {
                    if (tab.bounds().contains(relX, relY)) {
                        if (tab.index() != this.activeTab) {
                            this.activeTab = tab.index();
                            recomputeLayout();
                            playClickSound();
                        }
                        this.suppressNextRelease = true;
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.suppressNextRelease) {
            this.suppressNextRelease = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private static void playClickSound() {
        MinecraftClient.getInstance().getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f)
        );
    }
}
