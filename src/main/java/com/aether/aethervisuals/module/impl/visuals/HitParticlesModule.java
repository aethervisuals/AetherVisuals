package com.aether.aethervisuals.module.impl.visuals;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
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
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class HitParticlesModule extends Module {

    private final List<Spark> sparks = new ArrayList<>();
    private final Random random = new Random();

    public HitParticlesModule() {
        super("HitParticles", "Яркие частицы при ударе по противнику", Category.VISUALS);
    }

    public void spawnHitParticles(Entity target) {
        double cx = target.getX();
        double cy = target.getY() + target.getHeight() * 0.55;
        double cz = target.getZ();

        for (int i = 0; i < 20; i++) {
            double vx = (random.nextDouble() - 0.5) * 0.28;
            double vy = random.nextDouble() * 0.22 + 0.05;
            double vz = (random.nextDouble() - 0.5) * 0.28;
            boolean cyan = random.nextBoolean();
            sparks.add(new Spark(cx, cy, cz, vx, vy, vz, System.currentTimeMillis(), cyan));
        }
    }

    @Override
    public void onTick() {
        long now = System.currentTimeMillis();
        Iterator<Spark> it = sparks.iterator();
        while (it.hasNext()) {
            Spark s = it.next();
            s.x += s.vx;
            s.y += s.vy;
            s.z += s.vz;
            s.vy -= 0.015;
            s.vx *= 0.94;
            s.vz *= 0.94;

            if (now - s.spawnTime > 750) {
                it.remove();
            }
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (sparks.isEmpty()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.gameRenderer == null || mc.gameRenderer.getCamera() == null) return;

        var camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        long now = System.currentTimeMillis();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Spark s : sparks) {
            float progress = (float) (now - s.spawnTime) / 750.0f;
            if (progress >= 1.0f) continue;
            float alpha = Math.max(0.0f, 1.0f - progress);

            matrices.push();
            matrices.translate(s.x - camPos.x, s.y - camPos.y, s.z - camPos.z);
            matrices.multiply(camera.getRotation());

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float size = 0.065f * (1.0f - progress * 0.4f);
            float r = s.cyan ? 0.0f : 0.72f;
            float g = s.cyan ? 0.95f : 0.22f;
            float b = 1.0f;

            buffer.vertex(matrix, -size, -size, 0).color(r, g, b, alpha);
            buffer.vertex(matrix, -size, size, 0).color(r, g, b, alpha);
            buffer.vertex(matrix, size, size, 0).color(r, g, b, alpha);
            buffer.vertex(matrix, size, -size, 0).color(r, g, b, alpha);

            matrices.pop();
        }

        BuiltBuffer built = buffer.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static class Spark {
        double x, y, z, vx, vy, vz;
        long spawnTime;
        boolean cyan;

        Spark(double x, double y, double z, double vx, double vy, double vz, long spawnTime, boolean cyan) {
            this.x = x; this.y = y; this.z = z;
            this.vx = vx; this.vy = vy; this.vz = vz;
            this.spawnTime = spawnTime;
            this.cyan = cyan;
        }
    }
}

