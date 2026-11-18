package com.aether.aethervisuals.network;

import com.aether.aethervisuals.AetherVisuals;
import com.aether.aethervisuals.module.impl.visuals.CosmeticsModule;
import net.minecraft.client.MinecraftClient;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AetherNetwork {

    private static final Map<String, UserCosmeticData> USERS = new ConcurrentHashMap<>();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private static long lastSyncTime = 0L;

    public static void tickSync() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String myName = mc.player.getName().getString();
        CosmeticsModule cos = AetherVisuals.getInstance() != null
                ? AetherVisuals.getInstance().getModuleManager().getModule(CosmeticsModule.class)
                : null;

        UserCosmeticData myData = new UserCosmeticData(
                cos != null && cos.isEnabled() ? cos.headItem.getValue() : "Нет",
                cos != null && cos.isEnabled() ? cos.backItem.getValue() : "Нет",
                cos != null && cos.isEnabled() ? cos.auraItem.getValue() : "Нет",
                System.currentTimeMillis()
        );
        USERS.put(myName.toLowerCase(), myData);

        long now = System.currentTimeMillis();
        if (now - lastSyncTime > 8000L && mc.world != null) {
            lastSyncTime = now;
            syncWithCloud(myName, myData);
        }
    }

    private static void syncWithCloud(String myName, UserCosmeticData myData) {
        new Thread(() -> {
            try {
                String payload = URLEncoder.encode(myData.head + "|" + myData.back + "|" + myData.aura, StandardCharsets.UTF_8);
                String nickEnc = URLEncoder.encode(myName.toLowerCase(), StandardCharsets.UTF_8);
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("https://immanuel.co" + nickEnc + "/" + payload))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(3))
                        .build();
                HTTP.sendAsync(req, HttpResponse.BodyHandlers.discarding());

                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.world != null) {
                    mc.world.getPlayers().forEach(p -> {
                        String pName = p.getName().getString().toLowerCase();
                        if (!pName.equals(myName.toLowerCase())) {
                            fetchPlayer(pName);
                        }
                    });
                }
            } catch (Exception ignored) {
            }
        }, "AetherVisuals-Sync").start();
    }

    private static void fetchPlayer(String nickLower) {
        try {
            String nickEnc = URLEncoder.encode(nickLower, StandardCharsets.UTF_8);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://immanuel.co" + nickEnc))
                    .GET()
                    .timeout(Duration.ofSeconds(3))
                    .build();
            HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenAccept(res -> {
                if (res.statusCode() == 200 && res.body() != null) {
                    String body = res.body().replace("\"", "").trim();
                    if (!body.isEmpty() && !body.equals("null") && body.contains("|")) {
                        List<String> parts = Arrays.asList(body.split("\\|", 3));
                        if (parts.size() == 3) {
                            USERS.put(nickLower, new UserCosmeticData(parts.get(0), parts.get(1), parts.get(2), System.currentTimeMillis()));
                        }
                    }
                }
            });
        } catch (Exception ignored) {
        }
    }

    public static boolean isAetherUser(String playerName) {
        if (playerName == null) return false;
        UserCosmeticData data = USERS.get(playerName.toLowerCase());
        return data != null && (System.currentTimeMillis() - data.updatedAt < 120_000L);
    }

    public static UserCosmeticData getCosmetics(String playerName) {
        if (playerName == null) return null;
        return USERS.get(playerName.toLowerCase());
    }

    public static class UserCosmeticData {
        public final String head;
        public final String back;
        public final String aura;
        public final long updatedAt;

        public UserCosmeticData(String head, String back, String aura, long updatedAt) {
            this.head = head;
            this.back = back;
            this.aura = aura;
            this.updatedAt = updatedAt;
        }
    }
}
