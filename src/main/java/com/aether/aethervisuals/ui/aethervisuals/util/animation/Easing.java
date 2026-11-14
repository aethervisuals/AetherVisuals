package com.aether.aethervisuals.util.animation;

public enum Easing {
    LINEAR {
        @Override
        public double ease(double t) { return t; }
    },
    EASE_OUT_QUAD {
        @Override
        public double ease(double t) { return 1 - (1 - t) * (1 - t); }
    },
    EASE_OUT_CUBIC {
        @Override
        public double ease(double t) { return 1 - Math.pow(1 - t, 3); }
    },
    EASE_IN_OUT_CUBIC {
        @Override
        public double ease(double t) {
            return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
        }
    };

    public abstract double ease(double t);
}
