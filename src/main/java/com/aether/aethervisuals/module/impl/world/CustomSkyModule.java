package com.aether.aethervisuals.module.impl.world;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.settings.ModeSetting;

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

    public int getSkyColorInt() {
        return switch (preset.getValue()) {
            case "Розовый закат" -> 0xFFBF388C;
            case "Космос" -> 0xFF070818;
            case "Северное сияние" -> 0xFF0F8C7A;
            default -> 0xFF290D59; // Киберпанк-ночь
        };
    }
}
