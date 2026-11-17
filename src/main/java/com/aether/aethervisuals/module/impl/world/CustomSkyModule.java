package com.aether.aethervisuals.module.impl.world;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.settings.ModeSetting;
import net.minecraft.util.math.Vec3d;

public class CustomSkyModule extends Module {

    public final ModeSetting preset = new ModeSetting(
            "Пресет неба",
            "Киберпанк-ночь",
            "Киберпанк-ночь",
            "Розовый закат",
            "Космос",
            "Северное сияние"
    );

    public CustomSkyModule() {
        super("CustomSky", "Изменяет цвет неба и атмосферу мира (ПКМ для выбора)", Category.WORLD);
        addSettings(preset);
    }

    public Vec3d getSkyColor() {
        return switch (preset.getValue()) {
            case "Розовый закат" -> new Vec3d(0.75, 0.22, 0.55);
            case "Космос" -> new Vec3d(0.03, 0.03, 0.09);
            case "Северное сияние" -> new Vec3d(0.06, 0.55, 0.48);
            default -> new Vec3d(0.16, 0.05, 0.35); // Киберпанк-ночь
        };
    }
}
