package com.aether.aethervisuals.module;

public enum Category {
    HUD("HUD"),
    RENDER("Render"),
    WORLD("World"),
    VISUALS("Visuals"),
    SETTINGS("Settings");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
