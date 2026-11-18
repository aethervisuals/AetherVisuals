package com.aether.aethervisuals.module.impl.world;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.ui.ClickGuiScreen;
import com.aether.aethervisuals.util.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;

import java.time.LocalTime;

public class EventsModule extends Module {

    public float x = 10.0f;
    public float y = 118.0f;
    public float width = 148.0f;
    public float height = 58.0f;

    public EventsModule() {
        super("Events", "Показывает обратный отсчёт до ивентов FunTime", Category.WORLD);
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.textRenderer == null) return;

        LocalTime now = LocalTime.now();
        int totalSec = now.getMinute() * 60 + now.getSecond();

        // Циклы ивентов FunTime (раз в 30 и 60 минут)
        int secToMystic = 1800 - (totalSec % 1800);
        int secToVolcano = 3600 - (totalSec % 3600);
        int secToBeacon = 2700 - (totalSec % 2700);

        String[] lines = {
                "§dМистический: §f" + formatTime(secToMystic),
                "§6Вулкан: §f" + formatTime(secToVolcano),
                "§bМаяк убийцы: §f" + formatTime(secToBeacon)
        };

        TextRenderer tr = mc.textRenderer;
        this.width = 148.0f;
        this.height = 56.0f;

        Render2D.drawMattePanel(context, x, y, width, height, 7.0f);
        if (mc.currentScreen instanceof ClickGuiScreen || mc.currentScreen instanceof ChatScreen) {
            Render2D.drawRoundedOutline(context, x, y, width, height, 7.0f, 1.0f,
                    Render2D.ACCENT_PURPLE, Render2D.ACCENT_CYAN);
        }

        context.drawTextWithShadow(tr, "FunTime Events", (int) (x + 8), (int) (y + 6), Render2D.ACCENT_CYAN);

        float lineY = y + 19.0f;
        for (String line : lines) {
            context.drawTextWithShadow(tr, line, (int) (x + 8), (int) lineY, 0xFFFFFFFF);
            lineY += 12.0f;
        }
    }

    private String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}
