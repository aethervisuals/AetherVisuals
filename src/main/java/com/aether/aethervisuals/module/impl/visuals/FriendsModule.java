package com.aether.aethervisuals.module.impl.visuals;

import com.aether.aethervisuals.AetherVisuals;
import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FriendsModule extends Module {

    public FriendsModule() {
        super("Friends", "Подсвечивает друзей зелёной меткой в мире (.friend add <ник>)", Category.VISUALS);
        setEnabled(true);
    }

    @Override
    public void onRender3D(MatrixStack matrices, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.gameRenderer == null || mc.gameRenderer.getCamera() == null) return;
        if (AetherVisuals.getInstance().getFriendManager() == null) return;

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        TextRenderer tr = mc.textRenderer;
        VertexConsumerProvider.Immediate consumers = mc.getBufferBuilders().getEntityVertexConsumers();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || !player.isAlive()) continue;
            String name = player.getName().getString();
            if (!AetherVisuals.getInstance().getFriendManager().isFriend(name)) continue;

            double px = MathHelper.lerp(tickDelta, player.lastRenderX, player.getX()) - cam.x;
            double py = MathHelper.lerp(tickDelta, player.lastRenderY, player.getY()) - cam.y + player.getHeight() + 0.55;
            double pz = MathHelper.lerp(tickDelta, player.lastRenderZ, player.getZ()) - cam.z;

            double dist = Math.sqrt(px * px + py * py + pz * pz);
            float scale = (float) Math.max(0.025, dist * 0.0032);

            matrices.push();
            matrices.translate(px, py, pz);
            matrices.multiply(mc.gameRenderer.getCamera().getRotation());
            matrices.scale(scale, -scale, scale);

            String tag = "§a[FRIEND] §f" + name + " §7[" + (int) dist + "m]";
            float textX = -tr.getWidth(tag) / 2.0f;

            tr.draw(tag, textX, 0, 0xFFFFFFFF, true, matrices.peek().getPositionMatrix(),
                    consumers, TextRenderer.TextLayerType.SEE_THROUGH, 0x9908140C, 15728880);

            matrices.pop();
        }
        consumers.draw();
    }
}
