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
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class WorldParticlesModule extends Module {

    private final List<Firefly> particles = new ArrayList<>();
    private final Random random = new Random();

    public WorldParticlesModule() {
        super("WorldParticles", "Светящиеся частицы вокруг игрока в мире", Category.VISUALS);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (particles.size() < 45) {
            double x = mc.player.getX() + (random.nextDouble() - 0.5) * 16.0;
            double y = mc.player.getY() + random.nextDouble() * 5.0;
            double z = mc.player.getZ() + (random.nextDouble() - 0.5) * 16.0;
            double vx = (random.nextDouble() - 0.5) * 0.025;
            double vy = (random.nextDouble() - 0.3) * 0.02;
            double vz = (random.nextDouble() - 0.5) * 0.025;
            boolean cyan = random.nextBoolean();
            particles.add(new Firefly(x, y, z, vx, vy, vz, System.currentTimeMillis(), 2500 + random.nextInt(1500), cyan));
        }

        long now = System.currentTimeMillis();
        Iterator<Firefly> it = particles.iterator();
        while (it.hasNext()) {
            Firefly p = it.next();
            p.x += p.vx;
            p.y += p.vy;
            p.z += p.vz;
            if (now - p.spawnTime > p.lifeTime || mc.player.squaredDistanceTo(p.x, p.y, p.z) > 256) {
                it.remove();
            }
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (particles.isEmpty()) return;
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

        for (Firefly p : particles) {
            float progress = (float) (now - p.spawnTime) / p.lifeTime;
            if (progress >= 1.0f) continue;
            float alpha = (float) Math.sin(progress * Math.PI);

            matrices.push();
            matrices.translate(p.x - camPos.x, p.y - camPos.y, p.z - camPos.z);
            matrices.multiply(camera.getRotation());

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            float size = 0.07f;
            float r = p.cyan ? 0.0f : 0.65f;
            float g = p.cyan ? 0.92f : 0.25f;
            float b = 1.0f;

            buffer.vertex(matrix, -size, -size, 0).color(r, g, b, alpha * 0.9f);
            buffer.vertex(matrix, -size, size, 0).color(r, g, b, alpha * 0.9f);
            buffer.vertex(matrix, size, size, 0).color(r, g, b, alpha * 0.9f);
            buffer.vertex(matrix, size, -size, 0).color(r, g, b, alpha * 0.9f);

            matrices.pop();
        }

        BuiltBuffer built = buffer.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static class Firefly {
        double x, y, z, vx, vy, vz;
        long spawnTime, lifeTime;
        boolean cyan;

        Firefly(double x, double y, double z, double vx, double vy, double vz, long spawnTime, long lifeTime, boolean cyan) {
            this.x = x; this.y = y; this.z = z;
            this.vx = vx; this.vy = vy; this.vz = vz;
            this.spawnTime = spawnTime; this.lifeTime = lifeTime;
            this.cyan = cyan;
        }
    }
}
