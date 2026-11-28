package com.thaistoanf.expandedstorage.screen;

import com.thaistoanf.expandedstorage.compat.RenderCompat;
import com.thaistoanf.expandedstorage.core.ClientConfig;
import com.thaistoanf.expandedstorage.core.GuiMode;
import com.thaistoanf.expandedstorage.core.LayoutCalculator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic 9-slice T-shape chest screen supporting Adaptive Grid, Split-Wing, and Smart Tabbed modes,
 * automatic small-screen fallback, click-through flanks, and instant Light/Dark theme switching.
 */
@Environment(EnvType.CLIENT)
public class ExpandedChestScreen extends AbstractContainerScreen<ExpandedChestScreenHandler> {
    private LayoutCalculator.Layout layout;
    private int activeTab = 0;
    private boolean suppressNextRelease = false;

    public ExpandedChestScreen(ExpandedChestScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.layout = LayoutCalculator.compute(
                handler.getTier(), handler.isDoubleChest(), ClientConfig.get().guiMode(), 0, 0, 0
        );
        this.imageWidth = this.layout.totalWidth();
        this.imageHeight = this.layout.totalHeight();
    }

    @Override
    protected void init() {
        recomputeLayout();
    }

    private void recomputeLayout() {
        this.layout = LayoutCalculator.compute(
                this.menu.getTier(),
                this.menu.isDoubleChest(),
                ClientConfig.get().guiMode(),
                this.activeTab,
                this.width,
                this.height
        );
        this.activeTab = this.layout.activeTab();
        this.imageWidth = this.layout.totalWidth();
        this.imageHeight = this.layout.totalHeight();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.titleLabelX = this.layout.titleX();
        this.titleLabelY = this.layout.titleY();
        this.inventoryLabelX = this.layout.playerTitleX();
        this.inventoryLabelY = this.layout.playerTitleY();
        this.menu.applyLayout(this.layout);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        this.drawControlTooltips(context, mouseX, mouseY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractBackground(context, mouseX, mouseY, delta);
        boolean dark = ClientConfig.get().darkGui();
        Identifier tex = RenderCompat.guiTexture(dark);
        double relX = mouseX - this.leftPos;
        double relY = mouseY - this.topPos;

        // 1. Inactive Smart Tabs behind top chest edge
        if (this.layout.effectiveMode() == GuiMode.SMART_TABBED) {
            for (LayoutCalculator.TabSlice tab : this.layout.tabs()) {
                if (tab.index() != this.layout.activeTab()) {
                    drawSingleTab(context, tex, dark, tab, false);
                }
            }
        }

        // 2. Seamless T-shape chest + player inventory panels
        RenderCompat.drawTShapePanels(context, tex, this.leftPos, this.topPos, this.layout.chestPanel(), this.layout.playerPanel());

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
            int divTop = this.topPos + this.layout.chestPanel().y() + LayoutCalculator.HEADER_HEIGHT;
            int divHeight = this.layout.rows() * LayoutCalculator.SLOT_SIZE;
            RenderCompat.drawSplitDivider(context, tex, this.leftPos + this.layout.dividerX(), divTop, divHeight);
        }

        // 5. Crisp recessed 18x18 slot frames
        RenderCompat.drawSlotFrames(context, tex, this.leftPos, this.topPos, this.layout.chestSlots());
        RenderCompat.drawSlotFrames(context, tex, this.leftPos, this.topPos, this.layout.playerSlots());

        // 6. Header mode & dark/light icon buttons
        LayoutCalculator.Rect modeRect = this.layout.modeButton();
        int modeIconU = switch (ClientConfig.get().guiMode()) {
            case ADAPTIVE -> 0;
            case SPLIT_WING -> 12;
            case SMART_TABBED -> 24;
        };
        RenderCompat.drawIconButton(
                context, tex,
                this.leftPos + modeRect.x(), this.topPos + modeRect.y(),
                modeRect.contains(relX, relY),
                modeIconU, 36
        );

        LayoutCalculator.Rect themeRect = this.layout.themeButton();
        int themeIconU = dark ? 36 : 48;
        RenderCompat.drawIconButton(
                context, tex,
                this.leftPos + themeRect.x(), this.topPos + themeRect.y(),
                themeRect.contains(relX, relY),
                themeIconU, 36
        );
    }

    private void drawSingleTab(
            GuiGraphicsExtractor context,
            Identifier tex,
            boolean dark,
            LayoutCalculator.TabSlice tab,
            boolean active
    ) {
        LayoutCalculator.Rect b = tab.bounds();
        int tx = this.leftPos + b.x();
        int ty = this.topPos + b.y();
        RenderCompat.drawTab(context, tex, tx, ty, b.width(), b.height(), active);

        String label = Integer.toString(tab.index() + 1);
        int textW = this.font.width(label);
        int labelX = tx + (b.width() - textW) / 2;
        int labelY = ty + (active ? 5 : 7);
        context.text(this.font, label, labelX, labelY, RenderCompat.tabTextColor(dark, active), dark && active);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        boolean dark = ClientConfig.get().darkGui();
        int color = RenderCompat.titleColor(dark);

        int maxTitleWidth = Math.max(40, this.layout.modeButton().x() - this.titleLabelX - 4);
        Component displayTitle = this.title;
        if (this.font.width(displayTitle) > maxTitleWidth) {
            String trimmed = this.font.plainSubstrByWidth(displayTitle.getString(), maxTitleWidth - 6) + "...";
            displayTitle = Component.literal(trimmed);
        }

        RenderCompat.drawLabel(context, this.font, displayTitle, this.titleLabelX, this.titleLabelY, color, dark);
        RenderCompat.drawLabel(
                context, this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, color, dark
        );
    }

    private void drawControlTooltips(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        if (!this.menu.getCarried().isEmpty()) {
            return;
        }
        double relX = mouseX - this.leftPos;
        double relY = mouseY - this.topPos;

        if (this.layout.modeButton().contains(relX, relY)) {
            List<Component> lines = new ArrayList<>(3);
            GuiMode configured = ClientConfig.get().guiMode();
            lines.add(Component.translatable("gui.expandedstorage.button.mode", Component.translatable(configured.translationKey())));
            lines.add(Component.translatable(configured.translationKey() + ".desc").withStyle(ChatFormatting.GRAY));
            if (this.layout.autoFallback()) {
                lines.add(Component.translatable("gui.expandedstorage.mode.fallback_notice").withStyle(ChatFormatting.GOLD));
            } else if (configured == GuiMode.SPLIT_WING && !this.menu.isDoubleChest()) {
                lines.add(Component.translatable("gui.expandedstorage.mode.single_notice").withStyle(ChatFormatting.GRAY));
            }
            context.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
            return;
        }

        if (this.layout.themeButton().contains(relX, relY)) {
            boolean dark = ClientConfig.get().darkGui();
            Component state = Component.translatable(dark ? "gui.expandedstorage.theme.dark" : "gui.expandedstorage.theme.light");
            List<Component> lines = List.of(
                    Component.translatable("gui.expandedstorage.button.theme", state),
                    Component.translatable("gui.expandedstorage.button.theme.desc").withStyle(ChatFormatting.GRAY)
            );
            context.setComponentTooltipForNextFrame(this.font, lines, mouseX, mouseY);
            return;
        }

        if (this.layout.effectiveMode() == GuiMode.SMART_TABBED) {
            for (LayoutCalculator.TabSlice tab : this.layout.tabs()) {
                if (tab.bounds().contains(relX, relY)) {
                    Component tip = Component.translatable(
                            "gui.expandedstorage.tab.tooltip",
                            tab.index() + 1,
                            this.layout.tabCount(),
                            tab.startSlot() + 1,
                            tab.endSlotExclusive()
                    );
                    context.setTooltipForNextFrame(this.font, tip, mouseX, mouseY);
                    return;
                }
            }
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top) {
        return !this.layout.containsPoint(mouseX - left, mouseY - top);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() == 0) {
            double relX = click.x() - this.leftPos;
            double relY = click.y() - this.topPos;

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
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (this.suppressNextRelease) {
            this.suppressNextRelease = false;
            return true;
        }
        return super.mouseReleased(click);
    }

    private static void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f)
        );
    }
}
