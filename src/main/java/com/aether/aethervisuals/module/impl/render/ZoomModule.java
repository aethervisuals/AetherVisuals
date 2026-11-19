package com.aether.aethervisuals.module.impl.render;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ZoomModule extends Module {

    public final NumberSetting zoomFactor = new NumberSetting("Кратность", 3.5, 2.0, 6.0, 0.5);
    private float currentFactor = 1.0f;

    public ZoomModule() {
        super("Zoom", "Плавное приближение камеры на клавишу C (ПКМ для настройки)", Category.RENDER);
        addSettings(zoomFactor);
        setEnabled(true);
    }

    public float getSmoothFovMultiplier() {
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean keyDown = false;
        if (mc.getWindow() != null && mc.currentScreen == null) {
            keyDown = InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_C);
        }
        float target = (isEnabled() && keyDown) ? (1.0f / zoomFactor.getValue().floatValue()) : 1.0f;
        currentFactor += (target - currentFactor) * 0.18f;
        return currentFactor;
    }
}
