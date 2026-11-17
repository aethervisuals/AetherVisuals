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
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class TargetESPModule extends Module {

    public final ModeSetting mode = new ModeSetting("Режим", "Кольцо", "Кольцо", "Ромбы", "Цепи", "Квадрат");
    private LivingEntity lastTarget;
    private long lastTargetTime;

    public TargetESPModule() {
        super("TargetESP", "Красивый 3D-эффект вокруг цели (ПКМ для смены режима)", Category.VISUALS);
        addSettings(mode);
        setEnabled(true);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.gameRenderer == null || mc.gameRenderer.getCamera() == null) return;

        LivingEntity target = null;
        if (mc.targetedEntity instanceof LivingEntity living && living.isAlive()) {
            target = living;
            lastTarget = living;
            lastTargetTime = System.currentTimeMillis();
        } else if (lastTarget != null && lastTarget.isAlive() && System.currentTimeMillis() - lastTargetTime < 2500) {
            target = lastTarget;
        }
        if (target == null) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        double tx = MathHelper.lerp(tickDelta, target.lastRenderX, target.getX()) - cam.x;
        double ty = MathHelper.lerp(tickDelta, target.lastRenderY, target.getY()) - cam.y;
        double tz = MathHelper.lerp(tickDelta, target.lastRenderZ, target.getZ()) - cam.z;
        float h = target.getHeight();
        float r = Math.max(0.55f, target.getWidth() * 0.85f);

        float time = (System.currentTimeMillis() % 3000L) / 3000.0f;
        float angle = (float) (time * Math.PI * 2.0);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        matrices.push();
        matrices.translate(tx, ty, tz);
        Matrix4f m = matrices.peek().getPositionMatrix();

        Tessellator tess = Tessellator.getInstance();
        String current = mode.getValue();

        if (current.equals("Кольцо")) {
            float yOffset = (float) ((Math.sin(angle) * 0.5 + 0.5) * h);
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i <= 48; i++) {
                double a = (Math.PI * 2.0 * i) / 48;
                float cos = (float) Math.cos(a);
                float sin = (float) Math.sin(a);
                buf.vertex(m, cos * r, yOffset + 0.08f, sin * r).color(0.0f, 0.94f, 1.0f, 0.85f);
                buf.vertex(m, cos * (r - 0.12f), yOffset - 0.08f, sin * (r - 0.12f)).color(0.54f, 0.17f, 0.89f, 0.15f);
            }
            BuiltBuffer built = buf.endNullable();
            if (built != null) BufferRenderer.drawWithGlobalProgram(built);
        } else if (current.equals("Ромбы")) {
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            for (int i = 0; i < 4; i++) {
                float a = angle + (float) (i * Math.PI / 2.0);
                float cx = (float) Math.cos(a) * (r + 0.15f);
                float cz = (float) Math.sin(a) * (r + 0.15f);
                float cy = h * 0.5f + (float) Math.sin(angle * 2 + i) * 0.25f;
                drawDiamond(buf, m, cx, cy, cz, 0.14f);
            }
            BuiltBuffer built = buf.endNullable();
            if (built != null) BufferRenderer.drawWithGlobalProgram(built);
        } else if (current.equals("Цепи")) {
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            for (int i = 0; i < 24; i++) {
                double a1 = angle + (Math.PI * 2.0 * i) / 24;
                double a2 = angle + (Math.PI * 2.0 * (i + 0.65)) / 24;
                float y1 = h * 0.35f + (float) Math.sin(a1 * 3) * 0.18f;
                float y2 = h * 0.35f + (float) Math.sin(a2 * 3) * 0.18f;
                buf.vertex(m, (float) Math.cos(a1) * r, y1, (float) Math.sin(a1) * r).color(0.0f, 0.94f, 1.0f, 0.9f);
                buf.vertex(m, (float) Math.cos(a2) * r, y2, (float) Math.sin(a2) * r).color(0.54f, 0.17f, 0.89f, 0.9f);
            }
            BuiltBuffer built = buf.endNullable();
            if (built != null) BufferRenderer.drawWithGlobalProgram(built);
        } else {
            // Квадрат
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i <= 4; i++) {
                double a = angle + (Math.PI * 2.0 * i) / 4;
                float px = (float) Math.cos(a) * (r + 0.15f);
                float pz = (float) Math.sin(a) * (r + 0.15f);
                buf.vertex(m, px, h * 0.5f, pz).color(0.0f, 0.94f, 1.0f, 0.95f);
            }
            BuiltBuffer built = buf.endNullable();
            if (built != null) BufferRenderer.drawWithGlobalProgram(built);
        }

        matrices.pop();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void drawDiamond(BufferBuilder buf, Matrix4f m, float x, float y, float z, float s) {
        buf.vertex(m, x, y + s, z).color(0.0f, 0.94f, 1.0f, 0.9f);
        buf.vertex(m, x - s, y, z).color(0.54f, 0.17f, 0.89f, 0.8f);
        buf.vertex(m, x + s, y, z).color(0.54f, 0.17f, 0.89f, 0.8f);

        buf.vertex(m, x, y - s, z).color(0.0f, 0.94f, 1.0f, 0.9f);
        buf.vertex(m, x + s, y, z).color(0.54f, 0.17f, 0.89f, 0.8f);
        buf.vertex(m, x - s, y, z).color(0.54f, 0.17f, 0.89f, 0.8f);
    }
}
