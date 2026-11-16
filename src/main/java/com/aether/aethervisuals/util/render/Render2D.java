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
    public static final int GLASS_BG      = 0xB80E0E16;
    public static final int MATTE_BG      = 0xEB0D0E15;
    public static final int MATTE_CARD    = 0xF0141622;

    public static void drawGlassPanel(DrawContext context, float x, float y, float width, float height, float radius) {
        drawMattePanel(context, x, y, width, height, radius);
    }

    public static void drawMattePanel(DrawContext context, float x, float y, float width, float height, float radius) {
        drawRoundedRect(context, x + 2, y + 3, width, height, radius, 0x66000000);
        drawRoundedRect(context, x, y, width, height, radius, MATTE_BG);
        drawRoundedOutline(context, x, y, width, height, radius, 1.2f, ACCENT_PURPLE, ACCENT_CYAN);
    }

    public static void drawRoundedRect(DrawContext context, float x, float y, float width, float height, float radius, int color) {
        if (width <= 0 || height <= 0) return;
        radius = Math.min(radius, Math.min(width, height) / 2.0f);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        float cx = x + width / 2.0f;
        float cy = y + height / 2.0f;
        buffer.vertex(matrix, cx, cy, 0).color(r, g, b, a);

        int segments = 12;
        float[][] corners = {
                {x + width - radius, y + radius},
                {x + width - radius, y + height - radius},
                {x + radius,         y + height - radius},
                {x + radius,         y + radius}
        };

        for (int c = 0; c < 4; c++) {
            float startAngle = (float) (c * Math.PI / 2.0);
            for (int i = 0; i <= segments; i++) {
                float angle = startAngle + (float) (i * Math.PI / (2.0 * segments));
                float vx = corners[c][0] + (float) Math.sin(angle) * radius;
                float vy = corners[c][1] - (float) Math.cos(angle) * radius;
                buffer.vertex(matrix, vx, vy, 0).color(r, g, b, a);
            }
        }
        buffer.vertex(matrix, corners[0][0], corners[0][1] - radius, 0).color(r, g, b, a);

        BuiltBuffer built = buffer.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }
        RenderSystem.disableBlend();
    }

    public static void drawRoundedOutline(DrawContext context, float x, float y, float width, float height, float radius, float lineWidth, int color1, int color2) {
        if (width <= 0 || height <= 0) return;
        radius = Math.min(radius, Math.min(width, height) / 2.0f);

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        float a1 = ((color1 >> 24) & 0xFF) / 255.0f, r1 = ((color1 >> 16) & 0xFF) / 255.0f, g1 = ((color1 >> 8) & 0xFF) / 255.0f, b1 = (color1 & 0xFF) / 255.0f;
        float a2 = ((color2 >> 24) & 0xFF) / 255.0f, r2 = ((color2 >> 16) & 0xFF) / 255.0f, g2 = ((color2 >> 8) & 0xFF) / 255.0f, b2 = (color2 & 0xFF) / 255.0f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.lineWidth(lineWidth);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

        int segments = 12;
        float[][] corners = {
                {x + width - radius, y + radius},
                {x + width - radius, y + height - radius},
                {x + radius,         y + height - radius},
                {x + radius,         y + radius}
        };

        for (int c = 0; c < 4; c++) {
            float startAngle = (float) (c * Math.PI / 2.0);
            for (int i = 0; i <= segments; i++) {
                float angle = startAngle + (float) (i * Math.PI / (2.0 * segments));
                float vx = corners[c][0] + (float) Math.sin(angle) * radius;
                float vy = corners[c][1] - (float) Math.cos(angle) * radius;

                float t = ((vx - x) / width + (vy - y) / height) / 2.0f;
                t = Math.max(0.0f, Math.min(1.0f, t));

                float r = r1 + (r2 - r1) * t;
                float g = g1 + (g2 - g1) * t;
                float b = b1 + (b2 - b1) * t;
                float a = a1 + (a2 - a1) * t;

                buffer.vertex(matrix, vx, vy, 0).color(r, g, b, a);
            }
        }
        buffer.vertex(matrix, corners[0][0], corners[0][1] - radius, 0).color(r1, g1, b1, a1);

        BuiltBuffer built = buffer.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();
    }
}
