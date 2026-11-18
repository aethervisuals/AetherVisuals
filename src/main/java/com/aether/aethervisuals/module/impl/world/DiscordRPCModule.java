package com.aether.aethervisuals.module.impl.world;

import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import net.minecraft.client.MinecraftClient;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class DiscordRPCModule extends Module {

    private static final String CLIENT_ID = "1208469952520261642";
    private RandomAccessFile pipe;
    private Thread rpcThread;
    private volatile boolean running = false;
    private long startTime;

    public DiscordRPCModule() {
        super("DiscordRPC", "Показывает статус AetherVisuals 2.0 в вашем профиле Discord", Category.WORLD);
        setEnabled(true);
    }

    @Override
    public void onEnable() {
        startTime = System.currentTimeMillis() / 1000L;
        running = true;
        rpcThread = new Thread(this::runRpcLoop, "AetherVisuals-DiscordRPC");
        rpcThread.setDaemon(true);
        rpcThread.start();
    }

    @Override
    public void onDisable() {
        running = false;
        closePipe();
    }

    private void runRpcLoop() {
        while (running) {
            try {
                if (pipe == null) {
                    connectPipe();
                    if (pipe != null) {
                        sendPacket(0, "{\"v\":1,\"client_id\":\"" + CLIENT_ID + "\"}");
                        Thread.sleep(500);
                    }
                }
                if (pipe != null) {
                    updatePresence();
                }
                Thread.sleep(5000);
            } catch (Exception e) {
                closePipe();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {
                    break;
                }
            }
        }
    }

    private void connectPipe() {
        for (int i = 0; i < 10; i++) {
            try {
                pipe = new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + i, "rw");
                return;
            } catch (Exception ignored) {
            }
        }
    }

    private void updatePresence() throws Exception {
        MinecraftClient mc = MinecraftClient.getInstance();
        String nick = mc.getSession() != null ? mc.getSession().getUsername() : "Player";
        String state = "В главном меню";
        if (mc.world != null) {
            if (mc.isInSingleplayer()) state = "Одиночная игра";
            else if (mc.getCurrentServerEntry() != null) state = "Сервер: " + mc.getCurrentServerEntry().address;
            else state = "На сервере";
        }

        long pid = ProcessHandle.current().pid();
        String nonce = UUID.randomUUID().toString();
        String json = "{\"cmd\":\"SET_ACTIVITY\",\"args\":{\"pid\":" + pid + ",\"activity\":{" +
                "\"details\":\"AetherVisuals 2.0 | " + escape(nick) + "\"," +
                "\"state\":\"" + escape(state) + "\"," +
                "\"timestamps\":{\"start\":" + startTime + "}" +
                "}},\"nonce\":\"" + nonce + "\"}";

        sendPacket(1, json);
    }

    private void sendPacket(int opcode, String json) throws Exception {
        if (pipe == null) return;
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        header.putInt(opcode);
        header.putInt(data.length);
        pipe.write(header.array());
        pipe.write(data);
    }

    private void closePipe() {
        if (pipe != null) {
            try {
                pipe.close();
            } catch (Exception ignored) {
            }
            pipe = null;
        }
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
