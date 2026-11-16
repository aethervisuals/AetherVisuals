package com.aether.aethervisuals.module.impl.render;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;

public class AnimationModule extends Module {

    public AnimationModule() {
        super("Animation", "Плавная анимация открытия ClickGUI и инвентаря", Category.RENDER);
        setEnabled(true);
    }
}
