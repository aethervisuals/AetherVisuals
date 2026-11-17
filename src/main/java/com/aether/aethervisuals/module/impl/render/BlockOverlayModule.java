package com.aether.aethervisuals.module.impl.render;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.settings.ModeSetting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;

public class BlockOverlayModule extends Module {

    public final ModeSetting mode = new ModeSetting("Стиль", "Неон", "Неон", "Градиент", "Пульсация", "Радуга");

    public BlockOverlayModule() {
        super("BlockOverlay", "Подсветка блока под прицелом с переливаниями", Category.RENDER);
        addSettings(mode);
        setEnabled(true);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || !(mc.crosshairTarget instanceof BlockHitResult hit)) return;
        if (hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = hit.getBlockPos();
        if (mc.world.getBlockState(pos).isAir()) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        Box box = new Box(pos).expand(0.003).offset(-cam.x, -cam.y, -cam.z);

        float time = (System.currentTimeMillis() % 4000L) / 4000.0f;
        float r = 0.0f, g = 0.94f, b = 1.0f;
        float alpha = 0.25f;

        String currentMode = mode.getValue();
        if (currentMode.equals("Градиент")) {
            float wave = (float) (Math.sin(time * Math.PI * 2.0) * 0.5 + 0.5);
            r = 0.54f * wave;
            g = 0.17f * wave + 0.94f * (1.0f - wave);
            b = 0.89f * wave + 1.0f * (1.0f - wave);
        } else if (currentMode.equals("Пульсация")) {
            float pulse = (float) (Math.sin(time * Math.PI * 4.0) * 0.5 + 0.5);
            r = 0.54f;
            g = 0.17f;
            b = 0.89f;
            alpha = 0.12f + 0.28f * pulse;
        } else if (currentMode.equals("Радуга")) {
            int rgb = Color.HSBtoRGB(time, 0.85f, 1.0f);
            r = ((rgb >> 16) & 0xFF) / 255.0f;
            g = ((rgb >> 8) & 0xFF) / 255.0f;
            b = (rgb & 0xFF) / 255.0f;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Matrix4f m = matrices.peek().getPositionMatrix();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        float x1 = (float) box.minX, y1 = (float) box.minY, z1 = (float) box.minZ;
        float x2 = (float) box.maxX, y2 = (float) box.maxY, z2 = (float) box.maxZ;

        // Верх и низ
        buf.vertex(m, x1, y2, z1).color(r, g, b, alpha);
        buf.vertex(m, x1, y2, z2).color(r, g, b, alpha);
        buf.vertex(m, x2, y2, z2).color(r, g, b, alpha);
        buf.vertex(m, x2, y2, z1).color(r, g, b, alpha);

        buf.vertex(m, x1, y1, z1).color(r, g, b, alpha);
        buf.vertex(m, x2, y1, z1).color(r, g, b, alpha);
        buf.vertex(m, x2, y1, z2).color(r, g, b, alpha);
        buf.vertex(m, x1, y1, z2).color(r, g, b, alpha);

        // 4 боковые грани
        buf.vertex(m, x1, y1, z1).color(r, g, b, alpha);
        buf.vertex(m, x1, y2, z1).color(r, g, b, alpha);
        buf.vertex(m, x2, y2, z1).color(r, g, b, alpha);
        buf.vertex(m, x2, y1, z1).color(r, g, b, alpha);

        buf.vertex(m, x1, y1, z2).color(r, g, b, alpha);
        buf.vertex(m, x2, y1, z2).color(r, g, b, alpha);
        buf.vertex(m, x2, y2, z2).color(r, g, b, alpha);
        buf.vertex(m, x1, y2, z2).color(r, g, b, alpha);

        buf.vertex(m, x1, y1, z1).color(r, g, b, alpha);
        buf.vertex(m, x1, y1, z2).color(r, g, b, alpha);
        buf.vertex(m, x1, y2, z2).color(r, g, b, alpha);
        buf.vertex(m, x1, y2, z1).color(r, g, b, alpha);

        buf.vertex(m, x2, y1, z1).color(r, g, b, alpha);
        buf.vertex(m, x2, y2, z1).color(r, g, b, alpha);
        buf.vertex(m, x2, y2, z2).color(r, g, b, alpha);
        buf.vertex(m, x2, y1, z2).color(r, g, b, alpha);

        BuiltBuffer built = buf.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
