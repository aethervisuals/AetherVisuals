package com.aether.aethervisuals;

import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.ModuleManager;
import com.aether.aethervisuals.module.impl.hud.KeystrokesModule;
import com.aether.aethervisuals.module.impl.hud.TargetHudModule;
import com.aether.aethervisuals.module.impl.hud.WatermarkModule;
import com.aether.aethervisuals.module.impl.render.AnimationModule;
import com.aether.aethervisuals.module.impl.render.FullBrightModule;
import com.aether.aethervisuals.module.impl.visuals.HitParticlesModule;
import com.aether.aethervisuals.module.impl.visuals.JumpCircleModule;
import com.aether.aethervisuals.module.impl.visuals.WorldParticlesModule;
import com.aether.aethervisuals.module.impl.world.ClickPearlModule;
import com.aether.aethervisuals.ui.ClickGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AetherVisuals implements ClientModInitializer {

    public static final String MOD_ID = "aethervisuals";
    public static final String MOD_NAME = "AetherVisuals";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static AetherVisuals instance;
    private ModuleManager moduleManager;
    private KeyBinding clickGuiKey;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("[AetherVisuals] Запуск клиента...");

        this.moduleManager = new ModuleManager();
        this.moduleManager.register(new WatermarkModule());
        this.moduleManager.register(new TargetHudModule());
        this.moduleManager.register(new KeystrokesModule());

        this.moduleManager.register(new FullBrightModule());
        this.moduleManager.register(new AnimationModule());
        this.moduleManager.register(new ClickPearlModule());

        this.moduleManager.register(new JumpCircleModule());
        this.moduleManager.register(new WorldParticlesModule());
        this.moduleManager.register(new HitParticlesModule());

        this.clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.aethervisuals.clickgui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.aethervisuals"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (clickGuiKey.wasPressed()) {
                client.setScreen(new ClickGuiScreen());
            }

            for (Module module : moduleManager.getEnabledModules()) {
                module.onTick();
            }
        });

        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            float tickDelta = renderTickCounter.getTickDelta(true);
            for (Module module : moduleManager.getEnabledModules()) {
                module.onRender2D(drawContext, tickDelta);
            }
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            if (context.matrixStack() == null) return;
            float tickDelta = context.tickCounter().getTickDelta(true);
            for (Module module : moduleManager.getEnabledModules()) {
                module.onRender3D(context.matrixStack(), tickDelta);
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient && player.isMainPlayer()) {
                HitParticlesModule hitParticles = moduleManager.getModule(HitParticlesModule.class);
                if (hitParticles != null && hitParticles.isEnabled()) {
                    hitParticles.spawnHitParticles(entity);
                }
            }
            return ActionResult.PASS;
        });

        LOGGER.info("[AetherVisuals] Успешно загружено {} модулей!", moduleManager.getModules().size());
    }

    public static AetherVisuals getInstance() {
        return instance;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }
}
