package com.aether.aethervisuals.module.impl.render;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.settings.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public class ClientSoundModule extends Module {

    public final BooleanSetting toggleSounds = new BooleanSetting("Звуки клика", true);
    public final BooleanSetting welcomeVoice = new BooleanSetting("Голос при входе", true);
    private static boolean welcomed = false;

    public ClientSoundModule() {
        super("ClientSound", "Звуки переключения функций и голосовое приветствие при запуске", Category.RENDER);
        addSettings(toggleSounds, welcomeVoice);
        setEnabled(true);
    }

    public void playToggleSound(boolean enabled) {
        if (!isEnabled() || !toggleSounds.getValue()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getSoundManager() != null) {
            float pitch = enabled ? 1.45f : 0.95f;
            mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, pitch, 0.35f));
        }
    }

    public void playWelcomeVoice() {
        if (welcomed || !isEnabled() || !welcomeVoice.getValue()) return;
        welcomed = true;

        new Thread(() -> {
            try {
                String os = System.getProperty("os.name", "").toLowerCase();
                if (os.contains("win")) {
                    String psCommand = "Add-Type -AssemblyName System.Speech; " +
                            "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                            "$s.SelectVoiceByHints([System.Speech.Synthesis.VoiceGender]::Female); " +
                            "$s.Rate = 1; " +
                            "$s.Speak('Добро пожаловать в Эфир Вижуалс');";
                    new ProcessBuilder("powershell", "-NoProfile", "-Command", psCommand).start();
                }
            } catch (Exception ignored) {
            }
        }, "AetherVisuals-WelcomeVoice").start();
    }
}
