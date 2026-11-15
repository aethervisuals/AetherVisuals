package com.aether.aethervisuals.module.impl.hud;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.ui.ClickGuiScreen;
import com.aether.aethervisuals.util.animation.Animation;
import com.aether.aethervisuals.util.animation.Easing;
import com.aether.aethervisuals.util.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class TargetHudModule extends Module {

    private final Animation openAnimation = new Animation(Easing.EASE_OUT_CUBIC, 250);
    private final Animation healthAnimation = new Animation(Easing.EASE_OUT_QUAD, 200);

    private LivingEntity lastTarget;
    private long lastTargetTime;

    public float x = -1.0f;
    public float y = -1.0f;
    public final float width = 135.0f;
    public final float height = 44.0f;

    public TargetHudModule() {
        super("TargetHUD", "Показывает карточку с информацией о цели", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.textRenderer == null) return;

        if (x < 0 || y < 0) {
            x = (client.getWindow().getScaledWidth() - width) / 2.0f;
            y = client.getWindow().getScaledHeight() / 2.0f + 35.0f;
        }

        boolean isEditing = client.currentScreen instanceof ChatScreen || client.currentScreen instanceof ClickGuiScreen;

        LivingEntity target = null;
        if (isEditing) {
            target = client.player;
        } else if (client.targetedEntity instanceof LivingEntity living && living.isAlive()) {
            target = living;
            lastTarget = living;
            lastTargetTime = System.currentTimeMillis();
        } else if (lastTarget != null && lastTarget.isAlive() && System.currentTimeMillis() - lastTargetTime < 2500) {
            target = lastTarget;
        }

        openAnimation.animateTo(target != null ? 1.0 : 0.0);
        float anim = openAnimation.getFloat();

        if (anim <= 0.02f) return;

        LivingEntity displayTarget = target != null ? target : lastTarget;
        if (displayTarget == null) return;

        float maxHp = Math.max(1.0f, displayTarget.getMaxHealth());
        float currentHp = MathHelper.clamp(displayTarget.getHealth(), 0.0f, maxHp);
        healthAnimation.animateTo(currentHp / maxHp);
        float animatedHpRatio = MathHelper.clamp(healthAnimation.getFloat(), 0.0f, 1.0f);

        float scale = 0.85f + 0.15f * anim;
        context.getMatrices().push();
        context.getMatrices().translate(x + width / 2.0f, y + height / 2.0f, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-(x + width / 2.0f), -(y + height / 2.0f), 0);

        Render2D.drawGlassPanel(context, x, y, width, height, 8.0f);

        if (isEditing) {
            Render2D.drawRoundedOutline(context, x, y, width, height, 8.0f, 1.0f,
                    Render2D.ACCENT_PURPLE, Render2D.ACCENT_CYAN);
        }

        int headSize = 28;
        int headX = (int) (x + 8);
        int headY = (int) (y + (height - headSize) / 2.0f);

        if (displayTarget instanceof AbstractClientPlayerEntity playerTarget) {
            PlayerSkinDrawer.draw(context, playerTarget.getSkinTextures(), headX, headY, headSize);
        } else {
            Render2D.drawRoundedRect(context, headX, headY, headSize, headSize, 5.0f, 0x668A2BE2);
            String initial = displayTarget.getName().getString().substring(0, 1).toUpperCase();
            TextRenderer tr = client.textRenderer;
            context.drawTextWithShadow(tr, initial,
                    headX + (headSize - tr.getWidth(initial)) / 2,
                    headY + (headSize - tr.fontHeight) / 2 + 1,
                    Render2D.ACCENT_CYAN);
        }

        TextRenderer tr = client.textRenderer;
        float infoX = headX + headSize + 8;
        String name = displayTarget.getName().getString();
        if (tr.getWidth(name) > 85) {
            name = tr.trimToWidth(name, 80) + "...";
        }
        context.drawTextWithShadow(tr, name, (int) infoX, (int) (y + 8), 0xFFFFFFFF);

        String hpText = String.format("%.1f HP", currentHp);
        context.drawTextWithShadow(tr, hpText, (int) infoX, (int) (y + 19), 0xFFB0B0C0);

        float barX = infoX;
        float barY = y + height - 11;
        float barWidth = width - (infoX - x) - 8;
        float barHeight = 5.0f;
        Render2D.drawRoundedRect(context, barX, barY, barWidth, barHeight, 2.5f, 0x55000000);

        float filledWidth = Math.max(barHeight, barWidth * animatedHpRatio);
        if (animatedHpRatio > 0.01f) {
            Render2D.drawRoundedRect(context, barX, barY, filledWidth, barHeight, 2.5f, Render2D.ACCENT_CYAN);
        }

        context.getMatrices().pop();
    }
}
