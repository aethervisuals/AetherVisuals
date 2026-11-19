package com.aether.aethervisuals.module.impl.world;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import net.minecraft.client.MinecraftClient;

public class AutoSprintModule extends Module {

    public AutoSprintModule() {
        super("AutoSprint", "Автоматически включает спринт при движении вперёд", Category.WORLD);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (mc.player.forwardSpeed > 0.0f
                && !mc.player.isSneaking()
                && !mc.player.horizontalCollision
                && !mc.player.isUsingItem()
                && mc.player.getHungerManager().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }
}
