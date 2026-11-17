package com.aether.aethervisuals.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

public class Render2D {

    public static final int ACCENT_PURPLE = 0xFF8A2BE2;
    public static final int ACCENT_CYAN   = 0xFF00F0FF;
    public static final int GLASS_BG      = 0xFF090A0F;
    public static final int MATTE_BG      = 0xFF090A0F;
    public static final int MATTE_CARD    = 0xFF12141E;

    public static void drawGlassPanel(DrawContext context, float x, float y, float width, float height, float radius) {
        drawMattePanel(context, x, y, width, height, radius);
    }

    public static void drawMattePanel(DrawContext context, float x, float y, float width, float height, float radius) {
        drawRoundedRect(context, x + 2, y + 3, width, height, radius, 0xCC000000);
        drawRoundedRect(context, x, y, width, height, radius, MATTE_BG);
        drawRoundedOutline(context, x, y, width, height, radius, 1.2f, ACCENT_PURPLE, ACCENT_CYAN);
    }

    public static void drawRoundedRect(DrawContext context, float x, float y, float width, float height, float radius, int color) {
        if (width <= 0 || height <= 0) return;
        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = Math.round(width);
        int ih = Math.round(height);
        int r = Math.min(Math.round(radius), Math.min(iw, ih) / 2);

        if (r <= 1) {
            context.fill(ix, iy, ix + iw, iy + ih, color);
            return;
        }

        context.fill(ix + r, iy, ix + iw - r, iy + ih, color);
        context.fill(ix, iy + r, ix + r, iy + ih - r, color);
        context.fill(ix + iw - r, iy + r, ix + iw, iy + ih - r, color);

        for (int dy = 0; dy < r; dy++) {
            double distY = r - dy - 0.5;
            int dx = (int) Math.round(r - Math.sqrt(Math.max(0, r * r - distY * distY)));
            context.fill(ix + dx, iy + dy, ix + r, iy + dy + 1, color);
            context.fill(ix + iw - r, iy + dy, ix + iw - dx, iy + dy + 1, color);
            context.fill(ix + dx, iy + ih - dy - 1, ix + r, iy + ih - dy, color);
            context.fill(ix + iw - r, iy + ih - dy - 1, ix + iw - dx, iy + ih - dy, color);
        }
    }

    public static void drawRoundedOutline(DrawContext context, float x, float y, float width, float height, float radius, float lineWidth, int color1, int color2) {
        if (width <= 0 || height <= 0) return;
        radius = Math.min(radius, Math.min(width, height) / 2.0f);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        float a1 = ((color1 >> 24) & 0xFF) / 255.0f, r1 = ((color1 >> 16) & 0xFF) / 255.0f, g1 = ((color1 >> 8) & 0xFF) / 255.0f, b1 = (color1 & 0xFF) / 255.0f;
        float a2 = ((color2 >> 24) & 0xFF) / 255.0f, r2 = ((color2 >> 16) & 0xFF) / 255.0f, g2 = ((color2 >> 8) & 0xFF) / 255.0f, b2 = (color2 & 0xFF) / 255.0f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.lineWidth(lineWidth);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

        int segments = 12;
        for (int c = 0; c < 4; c++) {
            float cx = (c == 0 || c == 1) ? (x + width - radius) : (x + radius);
            float cy = (c == 1 || c == 2) ? (y + height - radius) : (y + radius);
            float startAngle = (float) (c * Math.PI / 2.0);
            for (int i = 0; i <= segments; i++) {
                float angle = startAngle + (float) (i * Math.PI / (2.0 * segments));
                float vx = cx + (float) Math.sin(angle) * radius;
                float vy = cy - (float) Math.cos(angle) * radius;

                float t = ((vx - x) / width + (vy - y) / height) / 2.0f;
                t = Math.max(0.0f, Math.min(1.0f, t));

                float r = r1 + (r2 - r1) * t;
                float g = g1 + (g2 - g1) * t;
                float b = b1 + (b2 - b1) * t;
                float a = a1 + (a2 - a1) * t;

                buffer.vertex(matrix, vx, vy, 0).color(r, g, b, a);
            }
        }
        buffer.vertex(matrix, x + width - radius, y, 0).color(r1, g1, b1, a1);

        BuiltBuffer built = buffer.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();
    }
}
