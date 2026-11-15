package com.aether.aethervisuals.module.impl.visuals;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JumpCircleModule extends Module {

    private final List<Circle> circles = new ArrayList<>();
    private boolean wasOnGround = true;

    public JumpCircleModule() {
        super("JumpCircle", "Неоновые круги под ногами при прыжке", Category.VISUALS);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();
        if (wasOnGround && !onGround && mc.player.getVelocity().y > 0) {
            circles.add(new Circle(mc.player.getPos(), System.currentTimeMillis()));
        }
        wasOnGround = onGround;

        long now = System.currentTimeMillis();
        Iterator<Circle> it = circles.iterator();
        while (it.hasNext()) {
            if (now - it.next().startTime > 1200) {
                it.remove();
            }
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (circles.isEmpty()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.gameRenderer == null || mc.gameRenderer.getCamera() == null) return;

        Vec3d camPos = mc.gameRenderer.getCamera().getPos();
        long now = System.currentTimeMillis();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        for (Circle circle : circles) {
            float progress = (now - circle.startTime) / 1200.0f;
            if (progress > 1.0f) continue;

            float ease = 1.0f - (float) Math.pow(1.0f - progress, 3);
            float outerRadius = 0.25f + ease * 1.45f;
            float innerRadius = Math.max(0.0f, outerRadius - 0.14f);
            float alpha = 1.0f - progress;

            matrices.push();
            matrices.translate(circle.pos.x - camPos.x, circle.pos.y - camPos.y + 0.02, circle.pos.z - camPos.z);
            Matrix4f matrix = matrices.peek().getPositionMatrix();

            int segments = 64;
            for (int i = 0; i <= segments; i++) {
                double angle = (Math.PI * 2.0 * i) / segments;
                float cos = (float) Math.cos(angle);
                float sin = (float) Math.sin(angle);

                float wave = (float) (Math.sin(angle * 2.0 + progress * 5.0) * 0.5 + 0.5);
                float r = 0.0f + wave * 0.54f;
                float g = 0.90f - wave * 0.73f;
                float b = 1.0f - wave * 0.11f;

                buffer.vertex(matrix, cos * outerRadius, 0.0f, sin * outerRadius).color(r, g, b, alpha * 0.85f);
                buffer.vertex(matrix, cos * innerRadius, 0.0f, sin * innerRadius).color(r, g, b, 0.0f);
            }
            matrices.pop();
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static class Circle {
        final Vec3d pos;
        final long startTime;

        Circle(Vec3d pos, long startTime) {
            this.pos = pos;
            this.startTime = startTime;
        }
    }
}
