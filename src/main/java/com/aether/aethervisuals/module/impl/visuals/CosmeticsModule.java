package com.aether.aethervisuals.module.impl.visuals;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.settings.ModeSetting;
import com.aether.aethervisuals.network.AetherNetwork;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class CosmeticsModule extends Module {

    public final ModeSetting headItem = new ModeSetting("Голова", "Нимб", "Нет", "Нимб", "Рога", "Ушки");
    public final ModeSetting backItem = new ModeSetting("Спина", "Крылья", "Нет", "Крылья", "Плащ");
    public final ModeSetting auraItem = new ModeSetting("Аура", "Кольца", "Нет", "Кольца", "Сферы");

    public CosmeticsModule() {
        super("Cosmetics", "Сетевая 3D-одежда и аксессуары (видят другие игроки AV)", Category.VISUALS);
        addSettings(headItem, backItem, auraItem);
        setEnabled(true);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.gameRenderer == null || mc.gameRenderer.getCamera() == null) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        float time = (System.currentTimeMillis() % 4000L) / 4000.0f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!player.isAlive()) continue;
            boolean isSelf = player == mc.player;
            if (isSelf && mc.options.getPerspective().isFirstPerson()) continue;

            AetherNetwork.UserCosmeticData data = isSelf
                    ? new AetherNetwork.UserCosmeticData(headItem.getValue(), backItem.getValue(), auraItem.getValue(), System.currentTimeMillis())
                    : AetherNetwork.getCosmetics(player.getName().getString());

            if (data == null) continue;

            double px = MathHelper.lerp(tickDelta, player.lastRenderX, player.getX()) - cam.x;
            double py = MathHelper.lerp(tickDelta, player.lastRenderY, player.getY()) - cam.y;
            double pz = MathHelper.lerp(tickDelta, player.lastRenderZ, player.getZ()) - cam.z;
            float yaw = MathHelper.lerp(tickDelta, player.prevBodyYaw, player.bodyYaw);
            float h = player.isSneaking() ? player.getHeight() - 0.18f : player.getHeight();

            matrices.push();
            matrices.translate(px, py, pz);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
            Matrix4f m = matrices.peek().getPositionMatrix();

            renderHeadAccessory(m, data.head, h, time);
            renderBackAccessory(m, data.back, h, time);
            renderAuraAccessory(m, data.aura, h, time);

            matrices.pop();
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
      private void renderHeadAccessory(Matrix4f m, String type, float h, float time) {
        if (type == null || type.equals("Нет")) return;
        Tessellator tess = Tessellator.getInstance();

        if (type.equals("Нимб")) {
            float y = h + 0.22f + (float) Math.sin(time * Math.PI * 2) * 0.03f;
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i <= 36; i++) {
                double a = (Math.PI * 2.0 * i) / 36;
                float cos = (float) Math.cos(a);
                float sin = (float) Math.sin(a);
                buf.vertex(m, cos * 0.34f, y, sin * 0.34f).color(0.0f, 0.94f, 1.0f, 0.9f);
                buf.vertex(m, cos * 0.25f, y, sin * 0.25f).color(0.54f, 0.17f, 0.89f, 0.5f);
            }
            BuiltBuffer built = buf.endNullable();
            if (built != null) BufferRenderer.drawWithGlobalProgram(built);
        } else if (type.equals("Рога")) {
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            for (int side = -1; side <= 1; side += 2) {
                float bx = side * 0.16f;
                buf.vertex(m, bx, h - 0.05f, 0.05f).color(0.85f, 0.1f, 0.25f, 0.9f);
                buf.vertex(m, bx + side * 0.08f, h - 0.05f, -0.05f).color(0.85f, 0.1f, 0.25f, 0.9f);
                buf.vertex(m, bx + side * 0.18f, h + 0.32f, 0.10f).color(1.0f, 0.35f, 0.45f, 0.95f);
            }
            BuiltBuffer built = buf.endNullable();
            if (built != null) BufferRenderer.drawWithGlobalProgram(built);
        } else if (type.equals("Ушки")) {
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            for (int side = -1; side <= 1; side += 2) {
                float bx = side * 0.14f;
                buf.vertex(m, bx - 0.05f, h, 0.0f).color(0.54f, 0.17f, 0.89f, 0.9f);
                buf.vertex(m, bx + 0.05f, h, 0.0f).color(0.54f, 0.17f, 0.89f, 0.9f);
                buf.vertex(m, bx + side * 0.04f, h + 0.24f, 0.02f).color(0.0f, 0.94f, 1.0f, 0.95f);
            }
            BuiltBuffer built = buf.endNullable();
            if (built != null) BufferRenderer.drawWithGlobalProgram(built);
        }
    }

    private void renderBackAccessory(Matrix4f m, String type, float h, float time) {
        if (type == null || type.equals("Нет")) return;
        Tessellator tess = Tessellator.getInstance();

        if (type.equals("Крылья")) {
            float flap = (float) Math.sin(time * Math.PI * 4) * 0.18f;
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            for (int side = -1; side <= 1; side += 2) {
                float zOff = -0.16f - Math.abs(flap) * 0.25f;
                buf.vertex(m, side * 0.06f, h * 0.78f, -0.14f).color(0.54f, 0.17f, 0.89f, 0.85f);
                buf.vertex(m, side * 0.95f, h * 0.95f + flap, zOff - 0.20f).color(0.0f, 0.94f, 1.0f, 0.75f);
                buf.vertex(m, side * 0.75f, h * 0.35f, zOff - 0.12f).color(0.54f, 0.17f, 0.89f, 0.45f);
            }
            BuiltBuffer built = buf.endNullable();
            if (built != null) BufferRenderer.drawWithGlobalProgram(built);
        } else if (type.equals("Плащ")) {
            float sway = (float) Math.sin(time * Math.PI * 2) * 0.06f;
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            buf.vertex(m, -0.24f, h * 0.80f, -0.15f).color(0.08f, 0.08f, 0.14f, 0.92f);
            buf.vertex(m, 0.24f, h * 0.80f, -0.15f).color(0.08f, 0.08f, 0.14f, 0.92f);
            buf.vertex(m, 0.28f, 0.12f, -0.28f + sway).color(0.54f, 0.17f, 0.89f, 0.85f);
            buf.vertex(m, -0.28f, 0.12f, -0.28f + sway).color(0.0f, 0.94f, 1.0f, 0.85f);
            BuiltBuffer built = buf.endNullable();
            if (built != null) BufferRenderer.drawWithGlobalProgram(built);
        }
    }

    private void renderAuraAccessory(Matrix4f m, String type, float h, float time) {
        if (type == null || type.equals("Нет")) return;
        Tessellator tess = Tessellator.getInstance();

        if (type.equals("Кольца")) {
            float angle = (float) (time * Math.PI * 2);
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
            for (int i = 0; i <= 32; i++) {
                double a = angle + (Math.PI * 2.0 * i) / 32;
                float y = h * 0.5f + (float) Math.sin(a * 2) * 0.22f;
                buf.vertex(m, (float) Math.cos(a) * 0.58f, y, (float) Math.sin(a) * 0.58f).color(0.0f, 0.94f, 1.0f, 0.85f);
            }
            BuiltBuffer built = buf.endNullable();
            if (built != null) BufferRenderer.drawWithGlobalProgram(built);
        } else if (type.equals("Сферы")) {
            BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            for (int i = 0; i < 3; i++) {
                double a = time * Math.PI * 2 + (Math.PI * 2 * i) / 3.0;
                float sx = (float) Math.cos(a) * 0.65f;
                float sz = (float) Math.sin(a) * 0.65f;
                float sy = h * 0.55f + (float) Math.sin(a * 2) * 0.15f;
                float s = 0.08f;
                buf.vertex(m, sx, sy + s, sz).color(0.0f, 0.94f, 1.0f, 0.95f);
                buf.vertex(m, sx - s, sy - s, sz).color(0.54f, 0.17f, 0.89f, 0.85f);
                buf.vertex(m, sx + s, sy - s, sz).color(0.54f, 0.17f, 0.89f, 0.85f);
            }
            BuiltBuffer built = buf.endNullable();
            if (built != null) BufferRenderer.drawWithGlobalProgram(built);
        }
    }
}
