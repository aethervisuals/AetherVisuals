package com.aether.aethervisuals.module.settings;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;
    private int index;

    public ModeSetting(String name, String defaultValue, String... modes) {
        super(name, defaultValue);
        this.modes = Arrays.asList(modes);
        this.index = this.modes.indexOf(defaultValue);
        if (this.index == -1) {
            this.index = 0;
            this.value = this.modes.get(0);
        }
    }

    public List<String> getModes() { return modes; }
    public int getIndex() { return index; }

    public void setMode(String mode) {
        int idx = modes.indexOf(mode);
        if (idx != -1) {
            this.index = idx;
            this.value = mode;
        }
    }

    public void cycle() {
        this.index = (this.index + 1) % modes.size();
        this.value = modes.get(this.index);
    }

    @Override
    public void reset() {
        this.index = modes.indexOf(getDefaultValue());
        if (this.index == -1) this.index = 0;
        this.value = modes.get(this.index);
    }
}
