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

public class ChinaHatModule extends Module {

    public final ModeSetting style = new ModeSetting("Стиль", "Неон", "Неон", "Радуга", "Космос");

    public ChinaHatModule() {
        super("ChinaHat", "Секретный головной аксессуар 2.5 (ПКМ для смены стиля)", Category.VISUALS);
        addSettings(style);
        setEnabled(true);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.gameRenderer == null || mc.gameRenderer.getCamera() == null) return;
        if (mc.options.getPerspective().isFirstPerson()) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        double px = MathHelper.lerp(tickDelta, mc.player.lastRenderX, mc.player.getX()) - cam.x;
        double py = MathHelper.lerp(tickDelta, mc.player.lastRenderY, mc.player.getY()) - cam.y + mc.player.getHeight() + 0.06;
        double pz = MathHelper.lerp(tickDelta, mc.player.lastRenderZ, mc.player.getZ()) - cam.z;

        if (mc.player.isSneaking()) {
            py -= 0.18;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        matrices.push();
        matrices.translate(px, py, pz);
        Matrix4f m = matrices.peek().getPositionMatrix();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        float radius = 0.56f;
        float topY = 0.30f;
        int segments = 48;
        float time = (System.currentTimeMillis() % 4000L) / 4000.0f;

        buf.vertex(m, 0.0f, topY, 0.0f).color(0.0f, 0.94f, 1.0f, 0.78f);

        for (int i = 0; i <= segments; i++) {
            double angle = (Math.PI * 2.0 * i) / segments;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float r = 0.54f, g = 0.17f, b = 0.89f;
            if (style.getValue().equals("Радуга")) {
                int rgb = Color.HSBtoRGB((time + (float) i / segments) % 1.0f, 0.85f, 1.0f);
                r = ((rgb >> 16) & 0xFF) / 255.0f;
                g = ((rgb >> 8) & 0xFF) / 255.0f;
                b = (rgb & 0xFF) / 255.0f;
            } else if (style.getValue().equals("Космос")) {
                float wave = (float) (Math.sin(angle * 3.0 + time * Math.PI * 2) * 0.5 + 0.5);
                r = 0.08f + wave * 0.35f;
                g = 0.05f + wave * 0.65f;
                b = 0.45f + wave * 0.55f;
            }

            buf.vertex(m, cos * radius, 0.0f, sin * radius).color(r, g, b, 0.55f);
        }

        BuiltBuffer built = buf.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }

        matrices.pop();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
