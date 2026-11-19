package com.aether.aethervisuals.module.impl.visuals;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;

public class ItemPhysicsModule extends Module {

    public ItemPhysicsModule() {
        super("ItemPhysics", "Красивая физика лежащих на земле предметов", Category.VISUALS);
        setEnabled(true);
    }
}
