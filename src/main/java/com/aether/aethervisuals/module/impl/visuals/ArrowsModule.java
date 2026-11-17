package com.aether.aethervisuals.module.impl.visuals;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.settings.NumberSetting;
import com.aether.aethervisuals.util.render.Render2D;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class ArrowsModule extends Module {

    public final NumberSetting radius = new NumberSetting("Радиус", 45.0, 25.0, 90.0, 5.0);

    public ArrowsModule() {
        super("Arrows", "Стрелочки вокруг прицела в сторону игроков", Category.VISUALS);
        addSettings(radius);
        setEnabled(true);
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        float cx = mc.getWindow().getScaledWidth() / 2.0f;
        float cy = mc.getWindow().getScaledHeight() / 2.0f;
        float distRadius = radius.getValue().floatValue();

        for (PlayerEntity target : mc.world.getPlayers()) {
            if (target == mc.player || !target.isAlive()) continue;

            double dx = MathHelper.lerp(tickDelta, target.lastRenderX, target.getX()) - mc.player.getX();
            double dz = MathHelper.lerp(tickDelta, target.lastRenderZ, target.getZ()) - mc.player.getZ();

            float angleToTarget = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
            float yawDiff = MathHelper.wrapDegrees(angleToTarget - mc.player.getYaw());
            float rad = (float) Math.toRadians(yawDiff);

            float ax = cx + (float) Math.sin(rad) * distRadius;
            float ay = cy - (float) Math.cos(rad) * distRadius;

            context.getMatrices().push();
            context.getMatrices().translate(ax, ay, 0);
            context.getMatrices().rotate(new org.joml.Quaternionf().rotateZ(rad));

            drawTriangleArrow(context, 0, 0, 5.5f);

            context.getMatrices().pop();
        }
    }

    private void drawTriangleArrow(DrawContext context, float x, float y, float size) {
        Matrix4f m = context.getMatrices().peek().getPositionMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

        buf.vertex(m, x, y - size, 0).color(0.0f, 0.94f, 1.0f, 0.95f);
        buf.vertex(m, x - size * 0.75f, y + size * 0.7f, 0).color(0.54f, 0.17f, 0.89f, 0.90f);
        buf.vertex(m, x + size * 0.75f, y + size * 0.7f, 0).color(0.54f, 0.17f, 0.89f, 0.90f);

        BuiltBuffer built = buf.endNullable();
        if (built != null) {
            BufferRenderer.drawWithGlobalProgram(built);
        }
        RenderSystem.disableBlend();
    }
}
