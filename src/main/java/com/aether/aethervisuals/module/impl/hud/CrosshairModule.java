package com.aether.aethervisuals.module.impl.hud;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.settings.ModeSetting;
import com.aether.aethervisuals.util.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class CrosshairModule extends Module {

    public final ModeSetting style = new ModeSetting("Стиль", "Динамический", "Динамический", "Точка", "Крест");

    public CrosshairModule() {
        super("Crosshair", "Секретный элемент интерфейса 2.5 (ПКМ для выбора)", Category.HUD);
        addSettings(style);
        setEnabled(true);
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !mc.options.getPerspective().isFirstPerson()) return;

        int cx = mc.getWindow().getScaledWidth() / 2;
        int cy = mc.getWindow().getScaledHeight() / 2;

        float cd = 1.0f - mc.player.getAttackCooldownProgress(tickDelta);
        int gap = style.getValue().equals("Динамический") ? (int) (3 + cd * 8) : 3;
        int len = 5;

        context.fill(cx - 1, cy - 1, cx + 1, cy + 1, Render2D.ACCENT_CYAN);

        if (!style.getValue().equals("Точка")) {
            context.fill(cx - gap - len, cy, cx - gap, cy + 1, 0xFFFFFFFF);
            context.fill(cx + gap + 1, cy, cx + gap + len + 1, cy + 1, 0xFFFFFFFF);
            context.fill(cx, cy - gap - len, cx + 1, cy - gap, 0xFFFFFFFF);
            context.fill(cx, cy + gap + 1, cx + 1, cy + gap + len + 1, 0xFFFFFFFF);
        }
    }
}
