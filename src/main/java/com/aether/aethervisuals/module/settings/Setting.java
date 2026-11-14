package com.aether.aethervisuals.module.settings;

public abstract class Setting<T> {
    private final String name;
    protected T value;
    private final T defaultValue;
    private boolean visible = true;

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getName() { return name; }
    public T getValue() { return value; }
    public void setValue(T value) { this.value = value; }
    public T getDefaultValue() { return defaultValue; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public abstract void reset();
}
