package com.aether.aethervisuals.module.impl.hud;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.util.animation.Animation;
import com.aether.aethervisuals.util.animation.Easing;
import com.aether.aethervisuals.util.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KeystrokesModule extends Module {

    private final Animation animW = new Animation(Easing.EASE_OUT_QUAD, 120);
    private final Animation animA = new Animation(Easing.EASE_OUT_QUAD, 120);
    private final Animation animS = new Animation(Easing.EASE_OUT_QUAD, 120);
    private final Animation animD = new Animation(Easing.EASE_OUT_QUAD, 120);
    private final Animation animSpace = new Animation(Easing.EASE_OUT_QUAD, 120);
    private final Animation animLmb = new Animation(Easing.EASE_OUT_QUAD, 120);
    private final Animation animRmb = new Animation(Easing.EASE_OUT_QUAD, 120);

    private final List<Long> leftClicks = new ArrayList<>();
    private final List<Long> rightClicks = new ArrayList<>();
    private boolean wasLmbPressed = false;
    private boolean wasRmbPressed = false;

    public KeystrokesModule() {
        super("Keystrokes", "Отображает нажатия клавиш WASD, пробела и CPS", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.textRenderer == null) return;

        long now = System.currentTimeMillis();
        boolean lmb = client.options.attackKey.isPressed();
        boolean rmb = client.options.useKey.isPressed();

        if (lmb && !wasLmbPressed) leftClicks.add(now);
        if (rmb && !wasRmbPressed) rightClicks.add(now);
        wasLmbPressed = lmb;
        wasRmbPressed = rmb;

        cleanOldClicks(leftClicks, now);
        cleanOldClicks(rightClicks, now);

        float startX = 10.0f;
        float startY = client.getWindow().getScaledHeight() - 105.0f;
        float keySize = 24.0f;
        float gap = 3.0f;

        drawKey(context, client.options.forwardKey, "W", animW,
                startX + keySize + gap, startY, keySize, keySize);

        float row2Y = startY + keySize + gap;
        drawKey(context, client.options.leftKey, "A", animA, startX, row2Y, keySize, keySize);
        drawKey(context, client.options.backKey, "S", animS, startX + keySize + gap, row2Y, keySize, keySize);
        drawKey(context, client.options.rightKey, "D", animD, startX + (keySize + gap) * 2, row2Y, keySize, keySize);

        float row3Y = row2Y + keySize + gap;
        float totalWidth = keySize * 3 + gap * 2;
        float mouseKeyW = (totalWidth - gap) / 2.0f;

        drawMouseKey(context, lmb, "LMB", leftClicks.size() + " CPS", animLmb,
                startX, row3Y, mouseKeyW, keySize);
        drawMouseKey(context, rmb, "RMB", rightClicks.size() + " CPS", animRmb,
                startX + mouseKeyW + gap, row3Y, mouseKeyW, keySize);

        float row4Y = row3Y + keySize + gap;
        drawKey(context, client.options.jumpKey, "SPACE", animSpace, startX, row4Y, totalWidth, 16.0f);
    }

    private void drawKey(DrawContext context, KeyBinding key, String label, Animation anim,
                         float x, float y, float w, float h) {
        anim.animateTo(key.isPressed() ? 1.0 : 0.0);
        float progress = anim.getFloat();

        Render2D.drawGlassPanel(context, x, y, w, h, 5.0f);

        if (progress > 0.01f) {
            int alpha = (int) (progress * 140);
            int highlight = (alpha << 24) | (Render2D.ACCENT_PURPLE & 0x00FFFFFF);
            Render2D.drawRoundedRect(context, x + 1, y + 1, w - 2, h - 2, 4.0f, highlight);
        }

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int textX = (int) (x + (w - tr.getWidth(label)) / 2.0f);
        int textY = (int) (y + (h - tr.fontHeight) / 2.0f + 1);
        context.drawTextWithShadow(tr, label, textX, textY, 0xFFFFFFFF);
    }

    private void drawMouseKey(DrawContext context, boolean pressed, String label, String cpsText,
                              Animation anim, float x, float y, float w, float h) {
        anim.animateTo(pressed ? 1.0 : 0.0);
        float progress = anim.getFloat();

        Render2D.drawGlassPanel(context, x, y, w, h, 5.0f);

        if (progress > 0.01f) {
            int alpha = (int) (progress * 140);
            int highlight = (alpha << 24) | (Render2D.ACCENT_CYAN & 0x00FFFFFF);
            Render2D.drawRoundedRect(context, x + 1, y + 1, w - 2, h - 2, 4.0f, highlight);
        }

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int labelX = (int) (x + (w - tr.getWidth(label)) / 2.0f);
        context.drawTextWithShadow(tr, label, labelX, (int) (y + 3), 0xFFFFFFFF);

        context.getMatrices().push();
        float scale = 0.65f;
        context.getMatrices().translate(x + w / 2.0f, y + h - 7, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.drawTextWithShadow(tr, cpsText, -tr.getWidth(cpsText) / 2, 0, 0xFFB0B0C0);
        context.getMatrices().pop();
    }

    private void cleanOldClicks(List<Long> clicks, long now) {
        Iterator<Long> it = clicks.iterator();
        while (it.hasNext()) {
            if (now - it.next() > 1000) {
                it.remove();
            }
        }
    }
}
