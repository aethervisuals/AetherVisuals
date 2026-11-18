package com.aether.aethervisuals.config;

import com.aether.aethervisuals.AetherVisuals;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FriendManager {

    private final File friendsFile;
    private final Set<String> friends = new HashSet<>();

    public FriendManager() {
        File runDir = MinecraftClient.getInstance().runDirectory;
        File dir = new File(runDir, "aethervisuals");
        if (!dir.exists()) dir.mkdirs();
        this.friendsFile = new File(dir, "friends.txt");
        load();
    }

    public boolean addFriend(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        boolean added = friends.add(name.trim().toLowerCase());
        if (added) save();
        return added;
    }

    public boolean removeFriend(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        boolean removed = friends.remove(name.trim().toLowerCase());
        if (removed) save();
        return removed;
    }

    public boolean isFriend(String name) {
        return name != null && friends.contains(name.trim().toLowerCase());
    }

    public List<String> getFriends() {
        return new ArrayList<>(friends);
    }

    private void save() {
        try {
            Files.write(friendsFile.toPath(), friends, StandardCharsets.UTF_8);
        } catch (Exception e) {
            AetherVisuals.LOGGER.error("Ошибка сохранения друзей: ", e);
        }
    }

    private void load() {
        try {
            if (friendsFile.exists()) {
                for (String line : Files.readAllLines(friendsFile.toPath(), StandardCharsets.UTF_8)) {
                    if (!line.trim().isEmpty()) {
                        friends.add(line.trim().toLowerCase());
                    }
                }
            }
        } catch (Exception e) {
            AetherVisuals.LOGGER.error("Ошибка загрузки друзей: ", e);
        }
    }
}
