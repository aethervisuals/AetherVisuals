package com.aether.aethervisuals.module.impl.visuals;

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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TrailsModule extends Module {

    public final ModeSetting style = new ModeSetting("Стиль", "Неон", "Неон", "Радуга", "Пламя");
    private final List<TrailPoint> points = new ArrayList<>();

    public TrailsModule() {
        super("Trails", "Секретный эффект движения 2.5 (ПКМ для выбора)", Category.VISUALS);
        addSettings(style);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        Iterator<TrailPoint> it = points.iterator();
        while (it.hasNext()) {
            if (now - it.next().time > 750) {
                it.remove();
            }
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.gameRenderer == null || mc.gameRenderer.getCamera() == null) return;
        if (mc.options.getPerspective().isFirstPerson()) return;

        double px = MathHelper.lerp(tickDelta, mc.player.lastRenderX, mc.player.getX());
        double py = MathHelper.lerp(tickDelta, mc.player.lastRenderY, mc.player.getY());
        double pz = MathHelper.lerp(tickDelta, mc.player.lastRenderZ, mc.player.getZ());

        long now = System.currentTimeMillis();
        if (points.isEmpty() || points.get(points.size() - 1).pos.squaredDistanceTo(px, py, pz) > 0.002) {
            points.add(new TrailPoint(new Vec3d(px, py, pz), now));
        }

        if (points.size() < 2) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        float height = mc.player.getHeight() * 0.85f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Matrix4f m = matrices.peek().getPositionMatrix();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        int size = points.size();
        for (int i = 0; i < size; i++) {
            TrailPoint tp = points.get(i);
            float age = (now - tp.time) / 750.0f;
            float alpha = Math.max(0.0f, 1.0f - age) * 0.55f;
            float ratio = (float) i / size;

            float r = 0.0f, g = 0.94f, b = 1.0f;
            if (style.getValue().equals("Неон")) {
                r = 0.54f * (1.0f - ratio);
                g = 0.17f * (1.0f - ratio) + 0.94f * ratio;
                b = 0.89f * (1.0f - ratio) + 1.0f * ratio;
            } else if (style.getValue().equals("Радуга")) {
                int rgb = Color.HSBtoRGB(((now % 3000L) / 3000.0f + ratio) % 1.0f, 0.85f, 1.0f);
                r = ((rgb >> 16) & 0xFF) / 255.0f;
                g = ((rgb >> 8) & 0xFF) / 255.0f;
                b = (rgb & 0xFF) / 255.0f;
            } else {
                r = 1.0f;
                g = 0.25f + 0.55f * ratio;
                b = 0.05f;
            }

            float vx = (float) (tp.pos.x - cam.x);
            float vy = (float) (tp.pos.y - cam.y);
            float vz = (float) (tp.pos.z - cam.z);

            buf.vertex(m, vx, vy + height, vz).color(r, g, b, alpha * 0.3f);
            buf.vertex(m, vx, vy + 0.05f, vz).color(r, g, b, alpha);
        }

        BuiltBuffer built = buf.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static class TrailPoint {
        final Vec3d pos;
        final long time;

        TrailPoint(Vec3d pos, long time) {
            this.pos = pos;
            this.time = time;
        }
    }
}
