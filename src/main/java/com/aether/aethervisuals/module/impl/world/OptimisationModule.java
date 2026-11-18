package com.aether.aethervisuals.module.impl.world;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.particle.ParticlesMode;

public class OptimisationModule extends Module {

    private int tickCounter = 0;

    public OptimisationModule() {
        super("Optimisation", "Повышает FPS за счёт оптимизации частиц и облаков", Category.WORLD);
        setEnabled(true);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) {
            mc.options.getParticles().setValue(ParticlesMode.MINIMAL);
            mc.options.getCloudRenderMode().setValue(CloudRenderMode.FAST);
            mc.options.getEntityShadows().setValue(false);
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options != null) {
            mc.options.getParticles().setValue(ParticlesMode.ALL);
            mc.options.getCloudRenderMode().setValue(CloudRenderMode.FANCY);
            mc.options.getEntityShadows().setValue(true);
        }
    }

    @Override
    public void onTick() {
        tickCounter++;
        if (tickCounter >= 600) {
            tickCounter = 0;
            System.gc();
        }
    }
}
