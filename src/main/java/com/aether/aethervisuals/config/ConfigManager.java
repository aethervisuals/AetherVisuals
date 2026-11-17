package com.aether.aethervisuals.config;

import com.aether.aethervisuals.AetherVisuals;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.impl.hud.CoordsModule;
import com.aether.aethervisuals.module.impl.hud.KeystrokesModule;
import com.aether.aethervisuals.module.impl.hud.TargetHudModule;
import com.aether.aethervisuals.module.impl.hud.WatermarkModule;
import com.aether.aethervisuals.module.settings.BooleanSetting;
import com.aether.aethervisuals.module.settings.ModeSetting;
import com.aether.aethervisuals.module.settings.NumberSetting;
import com.aether.aethervisuals.module.settings.Setting;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final File configDir;

    public ConfigManager() {
        File runDir = MinecraftClient.getInstance().runDirectory;
        this.configDir = new File(runDir, "aethervisuals/configs");
        if (!this.configDir.exists()) {
            this.configDir.mkdirs();
        }
    }

    public boolean saveConfig(String name) {
        String clean = sanitize(name);
        if (clean.isEmpty()) return false;
        try {
            if (!configDir.exists()) configDir.mkdirs();
            List<String> lines = new ArrayList<>();
            for (Module m : AetherVisuals.getInstance().getModuleManager().getModules()) {
                lines.add("MOD:" + m.getName() + ":" + m.isEnabled());
                for (Setting<?> s : m.getSettings()) {
                    lines.add("SET:" + m.getName() + ":" + s.getName() + ":" + s.getValue());
                }
            }
            WatermarkModule wm = AetherVisuals.getInstance().getModuleManager().getModule(WatermarkModule.class);
            if (wm != null) lines.add("HUD:Watermark:" + wm.x + ":" + wm.y);
            TargetHudModule th = AetherVisuals.getInstance().getModuleManager().getModule(TargetHudModule.class);
            if (th != null) lines.add("HUD:TargetHUD:" + th.x + ":" + th.y);
            KeystrokesModule ks = AetherVisuals.getInstance().getModuleManager().getModule(KeystrokesModule.class);
            if (ks != null) lines.add("HUD:Keystrokes:" + ks.x + ":" + ks.y);
            CoordsModule cm = AetherVisuals.getInstance().getModuleManager().getModule(CoordsModule.class);
            if (cm != null) lines.add("HUD:Coords:" + cm.x + ":" + cm.y);

            File file = new File(configDir, clean + ".txt");
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
            return true;
        } catch (Exception e) {
            AetherVisuals.LOGGER.error("Ошибка сохранения конфига: ", e);
            return false;
        }
    }

    public boolean loadConfig(String name) {
        String clean = sanitize(name);
        if (clean.isEmpty()) return false;
        File file = new File(configDir, clean + ".txt");
        if (!file.exists()) return false;
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] parts = line.split(":", 4);
                if (parts.length < 3) continue;

                if (parts[0].equals("MOD")) {
                    Module m = AetherVisuals.getInstance().getModuleManager().getModuleByName(parts[1]);
                    if (m != null) m.setEnabled(Boolean.parseBoolean(parts[2]));
                } else if (parts[0].equals("SET") && parts.length == 4) {
                    Module m = AetherVisuals.getInstance().getModuleManager().getModuleByName(parts[1]);
                    if (m != null) {
                        for (Setting<?> s : m.getSettings()) {
                            if (s.getName().equalsIgnoreCase(parts[2])) {
                                if (s instanceof BooleanSetting bs) bs.setValue(Boolean.parseBoolean(parts[3]));
                                else if (s instanceof ModeSetting ms) ms.setMode(parts[3]);
                                else if (s instanceof NumberSetting ns) ns.setValue(Double.parseDouble(parts[3]));
                            }
                        }
                    }
                } else if (parts[0].equals("HUD") && parts.length == 4) {
                    float hx = Float.parseFloat(parts[2]);
                    float hy = Float.parseFloat(parts[3]);
                    if (parts[1].equals("Watermark")) {
                        WatermarkModule wm = AetherVisuals.getInstance().getModuleManager().getModule(WatermarkModule.class);
                        if (wm != null) { wm.x = hx; wm.y = hy; }
                    } else if (parts[1].equals("TargetHUD")) {
                        TargetHudModule th = AetherVisuals.getInstance().getModuleManager().getModule(TargetHudModule.class);
                        if (th != null) { th.x = hx; th.y = hy; }
                    } else if (parts[1].equals("Keystrokes")) {
                        KeystrokesModule ks = AetherVisuals.getInstance().getModuleManager().getModule(KeystrokesModule.class);
                        if (ks != null) { ks.x = hx; ks.y = hy; }
                    } else if (parts[1].equals("Coords")) {
                        CoordsModule cm = AetherVisuals.getInstance().getModuleManager().getModule(CoordsModule.class);
                        if (cm != null) { cm.x = hx; cm.y = hy; }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            AetherVisuals.LOGGER.error("Ошибка загрузки конфига: ", e);
            return false;
        }
    }

    public List<String> getConfigList() {
        List<String> names = new ArrayList<>();
        if (configDir.exists() && configDir.listFiles() != null) {
            for (File f : configDir.listFiles()) {
                if (f.getName().endsWith(".txt")) {
                    names.add(f.getName().replace(".txt", ""));
                }
            }
        }
        return names;
    }

    private String sanitize(String name) {
        return name == null ? "" : name.replaceAll("[^a-zA-Z0-9_-]", "");
    }
}
