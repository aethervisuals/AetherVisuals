package com.aether.aethervisuals.module.impl.hud;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.util.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class WatermarkModule extends Module {

    public float x = 10.0f;
    public float y = 10.0f;
    public float width = 180.0f;
    public float height = 20.0f;

    public WatermarkModule() {
        super("Watermark", "Строгий системный оверлей с FPS и пингом", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.textRenderer == null) return;

        TextRenderer tr = mc.textRenderer;
        String brand = "AETHER // 2.5";
        String user = mc.player.getName().getString().toUpperCase();
        String fps = mc.getCurrentFps() + " FPS";

        int ping = 0;
        if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()) != null) {
            ping = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()).getLatency();
        }
        String pingStr = ping + " MS";
        String sep = "  |  ";

        float totalW = tr.getWidth(brand + sep + user + sep + fps + sep + pingStr) + 16.0f;
        this.width = totalW;
        this.height = 20.0f;

        Render2D.drawMattePanel(context, x, y, width, height, 6.0f);

        float curX = x + 8.0f;
        float textY = y + 6.0f;

        Render2D.drawStrictText(context, tr, brand, curX, textY, Render2D.ACCENT_CYAN);
        curX += tr.getWidth(brand);

        Render2D.drawStrictText(context, tr, sep, curX, textY, 0xFF4D5266);
        curX += tr.getWidth(sep);

        Render2D.drawStrictText(context, tr, user, curX, textY, 0xFFFFFFFF);
        curX += tr.getWidth(user);

        Render2D.drawStrictText(context, tr, sep, curX, textY, 0xFF4D5266);
        curX += tr.getWidth(sep);

        Render2D.drawStrictText(context, tr, fps, curX, textY, 0xFFD0D5E8);
        curX += tr.getWidth(fps);

        Render2D.drawStrictText(context, tr, sep, curX, textY, 0xFF4D5266);
        curX += tr.getWidth(sep);

        Render2D.drawStrictText(context, tr, pingStr, curX, textY, Render2D.ACCENT_CYAN);
    }
}
