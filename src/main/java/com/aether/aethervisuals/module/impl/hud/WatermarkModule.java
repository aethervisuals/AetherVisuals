package com.aether.aethervisuals.module.impl.hud;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.util.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class WatermarkModule extends Module {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public WatermarkModule() {
        super("Watermark", "Отображает логотип клиента, FPS, пинг и время", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.textRenderer == null) return;

        TextRenderer tr = client.textRenderer;

        int fps = client.getCurrentFps();
        int ping = getPing(client);
        String time = LocalTime.now().format(TIME_FORMATTER);

        String title = "AETHER";
        String info = " | " + fps + " FPS | " + ping + " ms | " + time;

        float x = 10.0f;
        float y = 10.0f;
        float paddingX = 8.0f;
        float height = 20.0f;
        float width = tr.getWidth(title + info) + paddingX * 2;

        Render2D.drawGlassPanel(context, x, y, width, height, 6.0f);

        int textY = (int) (y + (height - tr.fontHeight) / 2.0f + 1);
        int titleX = (int) (x + paddingX);

        context.drawTextWithShadow(tr, title, titleX, textY, Render2D.ACCENT_CYAN);
        context.drawTextWithShadow(tr, info, titleX + tr.getWidth(title), textY, 0xFFEAEAEA);
    }

    private int getPing(MinecraftClient client) {
        if (client.getNetworkHandler() == null || client.player == null) return 0;
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        return entry != null ? entry.getLatency() : 0;
    }
}
