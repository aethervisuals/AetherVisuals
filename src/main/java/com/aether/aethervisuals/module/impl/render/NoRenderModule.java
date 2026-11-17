package com.aether.aethervisuals.module.impl.render;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;

public class NoRenderModule extends Module {

    public final BooleanSetting fire = new BooleanSetting("Огонь (Fire)", true);
    public final BooleanSetting hurtCam = new BooleanSetting("Тряска (HurtCam)", true);
    public final BooleanSetting badEffects = new BooleanSetting("Плохие эффекты", true);
    public final BooleanSetting totem = new BooleanSetting("Анимация тотема", true);
    public final BooleanSetting fog = new BooleanSetting("Густой туман", true);

    public NoRenderModule() {
        super("NoRender", "Убирает огонь, тряску и эффекты (ПКМ для выбора)", Category.RENDER);
        addSettings(fire, hurtCam, badEffects, totem, fog);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (badEffects.getValue()) {
            mc.player.removeStatusEffect(StatusEffects.NAUSEA);
            mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
            mc.player.removeStatusEffect(StatusEffects.DARKNESS);
        }
    }
}
