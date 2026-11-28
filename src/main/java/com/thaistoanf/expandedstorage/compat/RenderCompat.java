package com.thaistoanf.expandedstorage.compat;

import com.thaistoanf.expandedstorage.core.ChestTier;
import com.thaistoanf.expandedstorage.core.LayoutCalculator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.model.ChestBlockModel;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side rendering adapter isolating {@link DrawContext} texture calls, 9-slice T-shape stitching,
 * and chest sprite atlas resolution so 1.21.2+ GUI/RenderLayer changes stay in one file.
 */
@Environment(EnvType.CLIENT)
public final class RenderCompat {
    public static final int TEX_SIZE = 128;

    // Sprite sheet layout (128x128) shared identically by gui_light.png and gui_dark.png:
    // [0,  0,  24, 24] -> Main 9-slice container panel (4px border, 16x16 center)
    // [24, 0,  18, 18] -> Standard recessed 18x18 item slot
    // [42, 0,  24, 20] -> Inactive top tab (9-slice, 4px border)
    // [66, 0,  24, 20] -> Active top tab (open bottom, 4px left/top/right border)
    // [0,  24, 12, 12] -> Button normal
    // [12, 24, 12, 12] -> Button hovered
    // [24, 24,  4,  4] -> T-seam left inner-corner joint
    // [28, 24,  4,  4] -> T-seam right inner-corner joint
    // [32, 24,  2, 16] -> Split-wing vertical divider groove
    // [0,  36, 12, 12] -> Icon: Adaptive mode
    // [12, 36, 12, 12] -> Icon: Split-Wing mode
    // [24, 36, 12, 12] -> Icon: Smart Tabbed mode
    // [36, 36, 12, 12] -> Icon: Sun (switch to Light)
    // [48, 36, 12, 12] -> Icon: Moon (switch to Dark)

    public static final Identifier GUI_LIGHT = IdCompat.id("textures/gui/gui_light.png");
    public static final Identifier GUI_DARK = IdCompat.id("textures/gui/gui_dark.png");

    private static final Map<ChestTier, TierSprites> CHEST_SPRITES = new EnumMap<>(ChestTier.class);

    static {
        for (ChestTier tier : ChestTier.values()) {
            String base = "entity/chest/" + tier.id() + "/";
            CHEST_SPRITES.put(tier, new TierSprites(
                    new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, IdCompat.id(base + "normal")),
                    new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, IdCompat.id(base + "left")),
                    new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE, IdCompat.id(base + "right"))
            ));
        }
    }

    private RenderCompat() {
    }

    private record TierSprites(SpriteIdentifier single, SpriteIdentifier left, SpriteIdentifier right) {
    }

    public static Identifier guiTexture(boolean dark) {
        return dark ? GUI_DARK : GUI_LIGHT;
    }

    public static int titleColor(boolean dark) {
        return dark ? 0xFFE6E9EF : 0xFF404040;
    }

    public static int tabTextColor(boolean dark, boolean active) {
        if (dark) {
            return active ? 0xFFFFF3C4 : 0xFF9CA3AF;
        }
        return active ? 0xFF2B2B2B : 0xFF585858;
    }

    public static void drawRegion(DrawContext ctx, Identifier tex, int x, int y, int u, int v, int width, int height) {
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, tex, x, y, (float) u, (float) v, width, height, TEX_SIZE, TEX_SIZE);
    }

    private static void drawTiledRegion(
            DrawContext ctx, Identifier tex,
            int x, int y, int width, int height,
            int u, int v, int tileW, int tileH
    ) {
        if (width <= 0 || height <= 0) {
            return;
        }
        for (int dy = 0; dy < height; dy += tileH) {
            int h = Math.min(tileH, height - dy);
            for (int dx = 0; dx < width; dx += tileW) {
                int w = Math.min(tileW, width - dx);
                drawRegion(ctx, tex, x + dx, y + dy, u, v, w, h);
            }
        }
    }

    /** Draws a pixel-accurate 9-slice box from a source region of size {@code (srcW, srcH)} with border {@code b}. */
    public static void drawNineSlice(
            DrawContext ctx, Identifier tex,
            int x, int y, int width, int height,
            int srcU, int srcV, int srcW, int srcH, int b
    ) {
        int innerW = width - b * 2;
        int innerH = height - b * 2;
        int centerSrcW = srcW - b * 2;
        int centerSrcH = srcH - b * 2;

        // 4 corners
        drawRegion(ctx, tex, x, y, srcU, srcV, b, b);
        drawRegion(ctx, tex, x + width - b, y, srcU + srcW - b, srcV, b, b);
        drawRegion(ctx, tex, x, y + height - b, srcU, srcV + srcH - b, b, b);
        drawRegion(ctx, tex, x + width - b, y + height - b, srcU + srcW - b, srcV + srcH - b, b, b);

        // Top and bottom edges
        drawTiledRegion(ctx, tex, x + b, y, innerW, b, srcU + b, srcV, centerSrcW, b);
        drawTiledRegion(ctx, tex, x + b, y + height - b, innerW, b, srcU + b, srcV + srcH - b, centerSrcW, b);

        // Left and right edges
        drawTiledRegion(ctx, tex, x, y + b, b, innerH, srcU, srcV + b, b, centerSrcH);
        drawTiledRegion(ctx, tex, x + width - b, y + b, b, innerH, srcU + srcW - b, srcV + b, b, centerSrcH);

        // Center fill
        drawTiledRegion(ctx, tex, x + b, y + b, innerW, innerH, srcU + b, srcV + b, centerSrcW, centerSrcH);
    }

    /**
     * Renders the seamless T-shape union of the Top Chest Panel and Bottom Player Inventory Panel
     * with no double border along the shared seam.
     */
    public static void drawTShapePanels(
            DrawContext ctx, Identifier tex,
            int originX, int originY,
            LayoutCalculator.Rect chest,
            LayoutCalculator.Rect player
    ) {
        int cx = originX + chest.x();
        int cy = originY + chest.y();
        int px = originX + player.x();
        int py = originY + player.y();

        // 1. Top chest panel and bottom player panel
        drawNineSlice(ctx, tex, cx, cy, chest.width(), chest.height(), 0, 0, 24, 24, 4);
        drawNineSlice(ctx, tex, px, py, player.width(), player.height(), 0, 0, 24, 24, 4);

        // 2. Erase the shared horizontal border across the interior of the player panel
        drawTiledRegion(ctx, tex, px + 4, py - 1, player.width() - 8, 6, 4, 4, 16, 16);

        // 3. Stitch left & right T-junctions cleanly
        if (chest.width() > player.width()) {
            // Wide T-shape: stamp 4x4 inner-corner bevel joints where chest bottom meets player walls
            drawRegion(ctx, tex, px, py, 24, 24, 4, 4);
            drawRegion(ctx, tex, px + player.width() - 4, py, 28, 24, 4, 4);
        } else {
            // Equal width (176px I-shape): continue straight left/right walls seamlessly across seam
            drawRegion(ctx, tex, px, py, 0, 4, 4, 4);
            drawRegion(ctx, tex, px + player.width() - 4, py, 20, 4, 4, 4);
        }
    }

    public static void drawTab(
            DrawContext ctx, Identifier tex,
            int x, int y, int width, int height,
            boolean active
    ) {
        if (active) {
            drawNineSlice(ctx, tex, x, y, width, height, 66, 0, 24, 20, 4);
            // Bridge the active tab seamlessly into the chest panel's top border
            drawTiledRegion(ctx, tex, x + 3, y + height - 4, width - 6, 5, 4, 4, 16, 16);
        } else {
            drawNineSlice(ctx, tex, x, y + 2, width, height - 2, 42, 0, 24, 20, 4);
        }
    }

    public static void drawSlotFrames(DrawContext ctx, Identifier tex, int originX, int originY, List<LayoutCalculator.SlotPos> slots) {
        for (LayoutCalculator.SlotPos pos : slots) {
            if (pos.visible()) {
                drawRegion(ctx, tex, originX + pos.x() - 1, originY + pos.y() - 1, 24, 0, 18, 18);
            }
        }
    }

    public static void drawSplitDivider(DrawContext ctx, Identifier tex, int x, int topY, int height) {
        drawTiledRegion(ctx, tex, x - 1, topY, 2, height, 32, 24, 2, 16);
    }

    public static void drawIconButton(
            DrawContext ctx, Identifier tex,
            int x, int y, boolean hovered, int iconU, int iconV
    ) {
        drawRegion(ctx, tex, x, y, hovered ? 12 : 0, 24, 12, 12);
        drawRegion(ctx, tex, x, y, iconU, iconV, 12, 12);
    }

    public static void drawLabel(DrawContext ctx, TextRenderer font, Text text, int x, int y, int color, boolean shadow) {
        ctx.drawText(font, text, x, y, color, shadow);
    }

    public static SpriteIdentifier getChestSprite(ChestTier tier, ChestType chestType) {
        TierSprites sprites = CHEST_SPRITES.getOrDefault(tier, CHEST_SPRITES.get(ChestTier.COPPER));
        return switch (chestType) {
            case LEFT -> sprites.left();
            case RIGHT -> sprites.right();
            default -> sprites.single();
        };
    }

    public static RenderLayer getChestRenderLayer(SpriteIdentifier sprite) {
        return sprite.getRenderLayer(RenderLayers::entityCutout);
    }

    public static void submitChestModel(
            OrderedRenderCommandQueue queue,
            ChestBlockModel model,
            float openFactor,
            MatrixStack matrices,
            SpriteHolder materials,
            SpriteIdentifier spriteId,
            int light,
            ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        RenderLayer layer = getChestRenderLayer(spriteId);
        Sprite sprite = materials.getSprite(spriteId);
        queue.submitModel(
                model,
                openFactor,
                matrices,
                layer,
                light,
                OverlayTexture.DEFAULT_UV,
                -1,
                sprite,
                0,
                crumblingOverlay
        );
    }
}
