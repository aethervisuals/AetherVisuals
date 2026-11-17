package com.aether.aethervisuals.module.impl.hud;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.ui.ClickGuiScreen;
import com.aether.aethervisuals.util.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.world.World;

public class CoordsModule extends Module {

    public float x = 10.0f;
    public float y = 34.0f;
    public float width = 130.0f;
    public float height = 20.0f;

    public CoordsModule() {
        super("Coords", "Отображает ваши текущие координаты в углу экрана", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.textRenderer == null) return;

        TextRenderer tr = client.textRenderer;
        int px = (int) Math.floor(client.player.getX());
        int py = (int) Math.floor(client.player.getY());
        int pz = (int) Math.floor(client.player.getZ());

        boolean isNether = client.world.getRegistryKey() == World.NETHER;
        int altX = isNether ? px * 8 : px / 8;
        int altZ = isNether ? pz * 8 : pz / 8;

        String label = "XYZ: ";
        String coords = px + ", " + py + ", " + pz + " (" + altX + ", " + altZ + ")";

        float paddingX = 8.0f;
        this.height = 20.0f;
        this.width = tr.getWidth(label + coords) + paddingX * 2;

        Render2D.drawMattePanel(context, x, y, width, height, 6.0f);

        if (client.currentScreen instanceof ClickGuiScreen || client.currentScreen instanceof ChatScreen) {
            Render2D.drawRoundedOutline(context, x, y, width, height, 6.0f, 1.0f,
                    Render2D.ACCENT_PURPLE, Render2D.ACCENT_CYAN);
        }

        int textY = (int) (y + (height - tr.fontHeight) / 2.0f + 1);
        int textX = (int) (x + paddingX);

        context.drawTextWithShadow(tr, label, textX, textY, Render2D.ACCENT_CYAN);
        context.drawTextWithShadow(tr, coords, textX + tr.getWidth(label), textY, 0xFFEAEAEA);
    }
}
