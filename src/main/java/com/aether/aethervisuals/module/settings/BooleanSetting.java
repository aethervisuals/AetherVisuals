package com.aether.aethervisuals.module.settings;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public void toggle() {
        this.value = !this.value;
    }

    @Override
    public void reset() {
        this.value = getDefaultValue();
    }
}
