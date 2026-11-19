package com.aether.aethervisuals.module.impl.visuals;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class DamageParticlesModule extends Module {

    private final List<DamageText> texts = new ArrayList<>();
    private final Random random = new Random();

    public DamageParticlesModule() {
        super("DamageParticles", "Всплывающие цифры урона при ударе по цели", Category.VISUALS);
        setEnabled(true);
    }

    public void onAttack(Entity target) {
        if (!isEnabled() || !(target instanceof LivingEntity living)) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        float dmg = Math.max(1.0f, (float) mc.player.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.ATTACK_DAMAGE));
        boolean crit = mc.player.fallDistance > 0.0f && !mc.player.isOnGround();
        if (crit) dmg *= 1.5f;

        double ox = (random.nextDouble() - 0.5) * 0.6;
        double oz = (random.nextDouble() - 0.5) * 0.6;
        Vec3d pos = new Vec3d(living.getX() + ox, living.getY() + living.getHeight() + 0.25, living.getZ() + oz);

        String label = (crit ? "§d-" : "§b-") + String.format("%.1f", dmg);
        texts.add(new DamageText(pos, label, System.currentTimeMillis()));
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        if (texts.isEmpty()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.gameRenderer == null || mc.gameRenderer.getCamera() == null) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        TextRenderer tr = mc.textRenderer;
        VertexConsumerProvider.Immediate consumers = mc.getBufferBuilders().getEntityVertexConsumers();
        long now = System.currentTimeMillis();

        Iterator<DamageText> it = texts.iterator();
        while (it.hasNext()) {
            DamageText dt = it.next();
            long age = now - dt.time;
            if (age > 950L) {
                it.remove();
                continue;
            }

            float progress = age / 950.0f;
            double px = dt.pos.x - cam.x;
            double py = dt.pos.y + progress * 0.65 - cam.y;
            double pz = dt.pos.z - cam.z;

            double dist = Math.sqrt(px * px + py * py + pz * pz);
            float scale = (float) Math.max(0.022, dist * 0.0028);

            matrices.push();
            matrices.translate(px, py, pz);
            matrices.multiply(mc.gameRenderer.getCamera().getRotation());
            matrices.scale(scale, -scale, scale);

            float textX = -tr.getWidth(dt.text) / 2.0f;
            tr.draw(dt.text, textX, 0, 0xFFFFFFFF, true, matrices.peek().getPositionMatrix(),
                    consumers, TextRenderer.TextLayerType.SEE_THROUGH, 0, 15728880);

            matrices.pop();
        }
        consumers.draw();
    }

    private static class DamageText {
        final Vec3d pos;
        final String text;
        final long time;

        DamageText(Vec3d pos, String text, long time) {
            this.pos = pos;
            this.text = text;
            this.time = time;
        }
    }
}
