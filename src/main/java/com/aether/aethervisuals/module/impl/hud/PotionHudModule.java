package com.aether.aethervisuals.module.impl.hud;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.ui.ClickGuiScreen;
import com.aether.aethervisuals.util.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.ArrayList;
import java.util.List;

public class PotionHudModule extends Module {

    public float x = 10.0f;
    public float y = 180.0f;
    public float width = 128.0f;
    public float height = 44.0f;

    public PotionHudModule() {
        super("Potions", "Секретный блок 2.5", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.textRenderer == null) return;

        List<String> lines = new ArrayList<>();
        for (StatusEffectInstance eff : mc.player.getStatusEffects()) {
            String name = eff.getEffectType().value().getName().getString();
            int amp = eff.getAmplifier() + 1;
            int sec = eff.getDuration() / 20;
            String time = String.format("%02d:%02d", sec / 60, sec % 60);
            lines.add(name + " " + amp + "  |  " + time);
        }

        if (lines.isEmpty()) {
            if (!(mc.currentScreen instanceof ClickGuiScreen)) return;
            lines.add("STRENGTH II  |  01:45");
            lines.add("SPEED II  |  02:10");
        }

        TextRenderer tr = mc.textRenderer;
        float maxW = tr.getWidth("ACTIVE EFFECTS");
        for (String s : lines) maxW = Math.max(maxW, tr.getWidth(s));

        this.width = maxW + 16.0f;
        this.height = 20.0f + lines.size() * 12.0f;

        Render2D.drawMattePanel(context, x, y, width, height, 6.0f);
        Render2D.drawStrictText(context, tr, "ACTIVE EFFECTS", x + 8, y + 6, Render2D.ACCENT_CYAN);

        float ly = y + 19.0f;
        for (String s : lines) {
            Render2D.drawStrictText(context, tr, s, x + 8, ly, 0xFFE6E9F5);
            ly += 12.0f;
        }
    }
}
