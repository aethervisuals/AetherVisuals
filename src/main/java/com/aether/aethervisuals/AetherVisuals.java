package com.aether.aethervisuals;

import com.aether.aethervisuals.config.ConfigManager;
import com.aether.aethervisuals.config.FriendManager;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.ModuleManager;
import com.aether.aethervisuals.module.impl.hud.CoordsModule;
import com.aether.aethervisuals.module.impl.hud.KeystrokesModule;
import com.aether.aethervisuals.module.impl.hud.TargetHudModule;
import com.aether.aethervisuals.module.impl.hud.WatermarkModule;
import com.aether.aethervisuals.module.impl.render.AnimationModule;
import com.aether.aethervisuals.module.impl.render.BlockOverlayModule;
import com.aether.aethervisuals.module.impl.render.ClientSoundModule;
import com.aether.aethervisuals.module.impl.render.FullBrightModule;
import com.aether.aethervisuals.module.impl.render.NoRenderModule;
import com.aether.aethervisuals.module.impl.visuals.ArrowsModule;
import com.aether.aethervisuals.module.impl.visuals.FriendsModule;
import com.aether.aethervisuals.module.impl.visuals.HitParticlesModule;
import com.aether.aethervisuals.module.impl.visuals.JumpCircleModule;
import com.aether.aethervisuals.module.impl.visuals.TargetESPModule;
import com.aether.aethervisuals.module.impl.visuals.WorldParticlesModule;
import com.aether.aethervisuals.module.impl.world.AucHelperModule;
import com.aether.aethervisuals.module.impl.world.ClickPearlModule;
import com.aether.aethervisuals.module.impl.world.CustomSkyModule;
import com.aether.aethervisuals.module.impl.world.DiscordRPCModule;
import com.aether.aethervisuals.module.impl.world.EventsModule;
import com.aether.aethervisuals.module.impl.world.FTHelperModule;
import com.aether.aethervisuals.module.impl.world.OptimisationModule;
import com.aether.aethervisuals.ui.ClickGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class AetherVisuals implements ClientModInitializer {

    public static final String MOD_ID = "aethervisuals";
    public static final String MOD_NAME = "AetherVisuals";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static AetherVisuals instance;
    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private FriendManager friendManager;
    private KeyBinding clickGuiKey;

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("[AetherVisuals] Запуск клиента...");

        this.moduleManager = new ModuleManager();
        // HUD
        this.moduleManager.register(new WatermarkModule());
        this.moduleManager.register(new CoordsModule());
        this.moduleManager.register(new TargetHudModule());
        this.moduleManager.register(new KeystrokesModule());

        // Render
        this.moduleManager.register(new FullBrightModule());
        this.moduleManager.register(new AnimationModule());
        this.moduleManager.register(new NoRenderModule());
        this.moduleManager.register(new BlockOverlayModule());
        this.moduleManager.register(new ClientSoundModule());

        // Visuals
        this.moduleManager.register(new TargetESPModule());
        this.moduleManager.register(new ArrowsModule());
        this.moduleManager.register(new FriendsModule());
        this.moduleManager.register(new JumpCircleModule());
        this.moduleManager.register(new WorldParticlesModule());
        this.moduleManager.register(new HitParticlesModule());

        // World & FunTime
        this.moduleManager.register(new ClickPearlModule());
        this.moduleManager.register(new CustomSkyModule());
        this.moduleManager.register(new FTHelperModule());
        this.moduleManager.register(new AucHelperModule());
        this.moduleManager.register(new EventsModule());
        this.moduleManager.register(new OptimisationModule());
        this.moduleManager.register(new DiscordRPCModule());

        this.configManager = new ConfigManager();
        this.friendManager = new FriendManager();

        this.clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.aethervisuals.clickgui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.aethervisuals"
        ));

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            String trimmed = message.trim();
            if (trimmed.toLowerCase().startsWith(".config")) {
                handleConfigCommand(trimmed);
                return false;
            }
            if (trimmed.toLowerCase().startsWith(".friend")) {
                handleFriendCommand(trimmed);
                return false;
            }
            return true;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (clickGuiKey.wasPressed()) {
                client.setScreen(new ClickGuiScreen());
            }

            for (Module module : moduleManager.getEnabledModules()) {
                module.onTick();
            }
        });

        HudRenderCallback.EVENT.register((drawContext, renderTickCounter) -> {
            float tickDelta = renderTickCounter.getTickDelta(true);
            for (Module module : moduleManager.getEnabledModules()) {
                module.onRender2D(drawContext, tickDelta);
            }
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            if (context.matrixStack() == null) return;
            float tickDelta = context.tickCounter().getTickDelta(true);
            for (Module module : moduleManager.getEnabledModules()) {
                module.onRender3D(context.matrixStack(), tickDelta);
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient && player.isMainPlayer()) {
                HitParticlesModule hitParticles = moduleManager.getModule(HitParticlesModule.class);
                if (hitParticles != null && hitParticles.isEnabled()) {
                    hitParticles.spawnHitParticles(entity);
                }
            }
            return ActionResult.PASS;
        });

        LOGGER.info("[AetherVisuals] Успешно загружено {} модулей!", moduleManager.getModules().size());
    }

    private void handleConfigCommand(String cmd) {
        List<String> args = Arrays.asList(cmd.split("\\s+"));
        if (args.size() < 2) {
            sendChat("§b[AetherVisuals] §fИспользуйте: §7.config save <имя> | load <имя> | list");
            return;
        }
        String sub = args.get(1).toLowerCase();
        if (sub.equals("list")) {
            List<String> list = configManager.getConfigList();
            if (list.isEmpty()) {
                sendChat("§b[AetherVisuals] §7Сохранённых конфигов пока нет.");
            } else {
                sendChat("§b[AetherVisuals] §fКонфиги (§b" + list.size() + "§f): §a" + String.join(", ", list));
            }
        } else if (sub.equals("save") && args.size() >= 3) {
            boolean ok = configManager.saveConfig(args.get(2));
            sendChat(ok ? "§b[AetherVisuals] §aКонфиг '" + args.get(2) + "' сохранён!"
                    : "§b[AetherVisuals] §cОшибка сохранения конфига.");
        } else if (sub.equals("load") && args.size() >= 3) {
            boolean ok = configManager.loadConfig(args.get(2));
            sendChat(ok ? "§b[AetherVisuals] §aКонфиг '" + args.get(2) + "' загружен!"
                    : "§b[AetherVisuals] §cКонфиг '" + args.get(2) + "' не найден.");
        } else {
            sendChat("§b[AetherVisuals] §fИспользуйте: §7.config save <имя> | load <имя> | list");
        }
    }

    private void handleFriendCommand(String cmd) {
        List<String> args = Arrays.asList(cmd.split("\\s+"));
        if (args.size() < 2) {
            sendChat("§b[AetherVisuals] §fИспользуйте: §7.friend add <ник> | remove <ник> | list");
            return;
        }
        String sub = args.get(1).toLowerCase();
        if (sub.equals("list")) {
            List<String> list = friendManager.getFriends();
            if (list.isEmpty()) {
                sendChat("§b[AetherVisuals] §7Список друзей пуст.");
            } else {
                sendChat("§b[AetherVisuals] §fДрузья (§a" + list.size() + "§f): §a" + String.join(", ", list));
            }
        } else if (sub.equals("add") && args.size() >= 3) {
            boolean ok = friendManager.addFriend(args.get(2));
            sendChat(ok ? "§b[AetherVisuals] §aИгрок " + args.get(2) + " добавлен в друзья!"
                    : "§b[AetherVisuals] §eИгрок уже в списке друзей.");
        } else if (sub.equals("remove") && args.size() >= 3) {
            boolean ok = friendManager.removeFriend(args.get(2));
            sendChat(ok ? "§b[AetherVisuals] §cИгрок " + args.get(2) + " удалён из друзей."
                    : "§b[AetherVisuals] §7Игрок не найден в друзьях.");
        } else {
            sendChat("§b[AetherVisuals] §fИспользуйте: §7.friend add <ник> | remove <ник> | list");
        }
    }

    private void sendChat(String text) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal(text), false);
}
}
public static AetherVisuals getInstance() {
return instance;
}
public ModuleManager getModuleManager() {
return moduleManager;
}
public ConfigManager getConfigManager() {
return configManager;
}
public FriendManager getFriendManager() {
return friendManager;
}
}
