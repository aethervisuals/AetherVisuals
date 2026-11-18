package com.aether.aethervisuals.module.impl.hud;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.util.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class ArmorHudModule extends Module {

    public float x = 220.0f;
    public float y = 10.0f;
    public float width = 92.0f;
    public float height = 26.0f;

    public ArmorHudModule() {
        super("ArmorHUD", "Секретный индикатор 2.5", Category.HUD);
        setEnabled(true);
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.textRenderer == null) return;

        boolean hasAny = false;
        for (ItemStack st : mc.player.getArmorItems()) {
            if (!st.isEmpty()) { hasAny = true; break; }
        }
        if (!hasAny) return;

        this.width = 92.0f;
        this.height = 28.0f;
        Render2D.drawMattePanel(context, x, y, width, height, 6.0f);

        float itemX = x + 6.0f;
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = mc.player.getInventory().getArmorStack(i);
            if (!stack.isEmpty()) {
                context.drawItem(stack, (int) itemX, (int) (y + 3));
                if (stack.isDamageable()) {
                    int pct = Math.max(0, Math.round((1.0f - (float) stack.getDamage() / stack.getMaxDamage()) * 100.0f));
                    int col = pct > 50 ? Render2D.ACCENT_CYAN : (pct > 25 ? 0xFFFFCC00 : 0xFFFF4D6D);
                    Render2D.drawStrictText(context, mc.textRenderer, pct + "%", itemX + 1, y + 19, col);
                }
            }
            itemX += 21.0f;
        }
    }
}
