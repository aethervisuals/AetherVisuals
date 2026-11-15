package com.aether.aethervisuals.module.impl.world;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class ClickPearlModule extends Module {

    private boolean wasMiddlePressed = false;

    public ClickPearlModule() {
        super("ClickPearl", "Бросок эндер-жемчуга по нажатию колёсика мыши", Category.WORLD);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null || mc.currentScreen != null) return;

        long window = mc.getWindow().getHandle();
        boolean isMiddlePressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

        if (isMiddlePressed && !wasMiddlePressed) {
            throwPearl(mc);
        }
        wasMiddlePressed = isMiddlePressed;
    }

    private void throwPearl(MinecraftClient mc) {
        int pearlSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.ENDER_PEARL) {
                pearlSlot = i;
                break;
            }
        }

        if (pearlSlot == -1) return;

        int oldSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = pearlSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = oldSlot;
    }
}
