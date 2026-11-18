package com.aether.aethervisuals.module.impl.world;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.ui.ClickGuiScreen;
import com.aether.aethervisuals.util.render.Render2D;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class FTHelperModule extends Module {

    public float x = 10.0f;
    public float y = 62.0f;
    public float width = 135.0f;
    public float height = 50.0f;

    public FTHelperModule() {
        super("FTHelper", "Отображает таймеры и наличие трапок, пластов и дезориентации FunTime", Category.WORLD);
    }

    @Override
    public void onRender2D(DrawContext context, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.textRenderer == null) return;

        List<String> lines = new ArrayList<>();
        String[] targets = {"Трапка", "Пласт", "Дезориентация", "Явная пыль", "Божья аура", "Огненный смерч"};

        for (String target : targets) {
            int count = 0;
            float cooldownSec = 0.0f;
            for (int i = 0; i < mc.player.getInventory().size(); i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.isEmpty()) continue;
                String name = stack.getName().getString();
                if (name.toLowerCase().contains(target.toLowerCase())) {
                    count += stack.getCount();
                    float cd = mc.player.getItemCooldownManager().getCooldownProgress(stack, tickDelta);
                    if (cd > 0.01f) {
                        cooldownSec = Math.max(cooldownSec, cd * 15.0f);
                    }
                }
            }
            if (count > 0) {
                String status = cooldownSec > 0.1f
                        ? "§c" + String.format("%.1fs", cooldownSec)
                        : "§aГОТОВО";
                lines.add("§f" + target + " §7(x" + count + ") " + status);
            }
        }

        boolean isEditing = mc.currentScreen instanceof ClickGuiScreen || mc.currentScreen instanceof ChatScreen;
        if (lines.isEmpty()) {
            if (!isEditing) return;
            lines.add("§7Трапка (x0) §aГОТОВО");
            lines.add("§7Дезориентация (x0) §aГОТОВО");
        }

        TextRenderer tr = mc.textRenderer;
        float maxW = tr.getWidth("FTHelper [FunTime]");
        for (String line : lines) {
            maxW = Math.max(maxW, tr.getWidth(line));
        }

        this.width = maxW + 16.0f;
        this.height = 20.0f + lines.size() * 12.0f;

        Render2D.drawMattePanel(context, x, y, width, height, 7.0f);
        context.drawTextWithShadow(tr, "FTHelper", (int) (x + 8), (int) (y + 6), Render2D.ACCENT_CYAN);

        float lineY = y + 19.0f;
        for (String line : lines) {
            context.drawTextWithShadow(tr, line, (int) (x + 8), (int) lineY, 0xFFFFFFFF);
            lineY += 12.0f;
        }
    }
}
