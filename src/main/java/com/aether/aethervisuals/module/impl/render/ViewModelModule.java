package com.aether.aethervisuals.module.impl.render;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.settings.ModeSetting;
import com.aether.aethervisuals.module.settings.NumberSetting;

public class ViewModelModule extends Module {

    public final NumberSetting posX = new NumberSetting("Сдвиг X", 0.0, -0.5, 0.5, 0.1);
    public final NumberSetting posY = new NumberSetting("Сдвиг Y", -0.1, -0.5, 0.5, 0.1);
    public final NumberSetting posZ = new NumberSetting("Сдвиг Z", -0.2, -0.5, 0.5, 0.1);
    public final NumberSetting scale = new NumberSetting("Размер руки", 0.8, 0.4, 1.3, 0.1);
    public final ModeSetting swingStyle = new ModeSetting("Стиль взмаха", "Плавный", "Обычный", "Плавный", "Сильный", "Вращение");

    public ViewModelModule() {
        super("ViewModel", "Секретная настройка 2.5 (ПКМ для открытия параметров)", Category.RENDER);
        addSettings(posX, posY, posZ, scale, swingStyle);
        setEnabled(true);
    }
}
