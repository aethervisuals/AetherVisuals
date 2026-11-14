package com.aether.aethervisuals.module.settings;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = clamp(defaultValue);
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getStep() { return step; }

    public void setValue(double value) {
        double clamped = clamp(value);
        double rounded = Math.round(clamped / step) * step;
        this.value = Math.min(max, Math.max(min, rounded));
    }

    private double clamp(double v) {
        return Math.min(max, Math.max(min, v));
    }

    @Override
    public void reset() {
        this.value = getDefaultValue();
    }
}
