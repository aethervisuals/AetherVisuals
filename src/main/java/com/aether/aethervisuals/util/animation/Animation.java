package com.aether.aethervisuals.util.animation;

public class Animation {
    private final Easing easing;
    private final long duration;
    private double startValue;
    private double targetValue;
    private double currentValue;
    private long startTime;

    public Animation(Easing easing, long duration) {
        this.easing = easing;
        this.duration = duration;
    }

    public void animateTo(double target) {
        if (this.targetValue == target) return;
        this.startValue = getValue();
        this.targetValue = target;
        this.startTime = System.currentTimeMillis();
    }

    public double getValue() {
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= duration || duration <= 0) {
            currentValue = targetValue;
            return currentValue;
        }
        double progress = (double) elapsed / duration;
        double eased = easing.ease(Math.max(0.0, Math.min(1.0, progress)));
        currentValue = startValue + (targetValue - startValue) * eased;
        return currentValue;
    }

    public float getFloat() {
        return (float) getValue();
    }

    public void setValue(double value) {
        this.startValue = value;
        this.targetValue = value;
        this.currentValue = value;
        this.startTime = 0;
    }
}
