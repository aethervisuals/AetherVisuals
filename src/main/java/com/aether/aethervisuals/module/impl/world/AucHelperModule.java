package com.aether.aethervisuals.module.impl.world;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AucHelperModule extends Module {

    private static final Pattern PRICE_PATTERN = Pattern.compile("(?i)(?:цена|price|\\$)[^0-9]*([0-9\\s.,]+)");
    private int bestSlotIndex = -1;

    public AucHelperModule() {
        super("AucHelper", "Подсвечивает самые выгодные лоты на аукционе /ah", Category.WORLD);
    }

    @Override
    public void onTick() {
        bestSlotIndex = -1;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !(mc.currentScreen instanceof GenericContainerScreen container)) return;

        String title = container.getTitle().getString().toLowerCase();
        if (!title.contains("аукцион") && !title.contains("auction") && !title.contains("/ah")) return;

        double bestUnitPrice = Double.MAX_VALUE;
        for (Slot slot : container.getScreenHandler().slots) {
            if (slot.inventory == mc.player.getInventory()) continue;
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            double price = extractPrice(mc, stack);
            if (price > 0) {
                double unitPrice = price / Math.max(1, stack.getCount());
                if (unitPrice < bestUnitPrice) {
                    bestUnitPrice = unitPrice;
                    bestSlotIndex = slot.id;
                }
            }
        }
    }

    private double extractPrice(MinecraftClient mc, ItemStack stack) {
        try {
            List<Text> lore = stack.getTooltip(Item.TooltipContext.DEFAULT, mc.player, TooltipType.BASIC);
            for (Text line : lore) {
                String clean = line.getString().replaceAll("§.", "");
                Matcher m = PRICE_PATTERN.matcher(clean);
                if (m.find()) {
                    String num = m.group(1).replaceAll("[^0-9]", "");
                    if (!num.isEmpty()) return Double.parseDouble(num);
                }
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    public int getBestSlotIndex() {
        return bestSlotIndex;
    }
}
