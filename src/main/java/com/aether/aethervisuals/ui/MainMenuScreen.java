package com.aether.aethervisuals.ui;

import com.aether.aethervisuals.AetherVisuals;
import com.aether.aethervisuals.mixin.MinecraftClientAccessor;
import com.aether.aethervisuals.module.impl.render.ClientSoundModule;
import com.aether.aethervisuals.util.animation.Animation;
import com.aether.aethervisuals.util.animation.Easing;
import com.aether.aethervisuals.util.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.UUID;

public class MainMenuScreen extends Screen {

    private final Animation openAnim = new Animation(Easing.EASE_OUT_CUBIC, 320);
    private boolean accountModalOpen = false;
    private String nicknameInput = "";

    public MainMenuScreen() {
        super(Text.literal("AETHER // VISUALS"));
    }

    @Override
    protected void init() {
        super.init();
        openAnim.setValue(0.0);
        openAnim.animateTo(1.0);
        if (this.client != null && this.client.getSession() != null) {
            this.nicknameInput = this.client.getSession().getUsername();
        }
        if (AetherVisuals.getInstance() != null && AetherVisuals.getInstance().getModuleManager() != null) {
            ClientSoundModule sound = AetherVisuals.getInstance().getModuleManager().getModule(ClientSoundModule.class);
            if (sound != null) {
                sound.playWelcomeVoice();
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xFF050609, 0xFF0D0A18);

        if (accountModalOpen) {
            float mW = 224.0f;
            float mH = 112.0f;
            float mX = (this.width - mW) / 2.0f;
            float mY = (this.height - mH) / 2.0f;

            Render2D.drawMattePanel(context, mX, mY, mW, mH, 10.0f);
            Render2D.drawStrictText(context, this.textRenderer, "SESSION NICKNAME", mX + 16, mY + 14, 0xFFFFFFFF);

            Render2D.drawRoundedRect(context, mX + 16, mY + 32, mW - 32, 22, 5.0f, Render2D.MATTE_CARD);
            Render2D.drawRoundedOutline(context, mX + 16, mY + 32, mW - 32, 22, 5.0f, 1.0f,
                    Render2D.ACCENT_PURPLE, Render2D.ACCENT_CYAN);
            Render2D.drawStrictText(context, this.textRenderer, nicknameInput + "_", mX + 22, mY + 39, Render2D.ACCENT_CYAN);

            Render2D.drawRoundedRect(context, mX + 16, mY + 68, 90, 22, 5.0f, 0xFF4C1B8A);
            Render2D.drawStrictCenteredText(context, this.textRenderer, "APPLY", mX + 61, mY + 75, 0xFFFFFFFF);

            Render2D.drawRoundedRect(context, mX + mW - 106, mY + 68, 90, 22, 5.0f, 0xFF1A1D29);
            Render2D.drawStrictCenteredText(context, this.textRenderer, "CANCEL", mX + mW - 61, mY + 75, 0xFF9AA0B8);
            return;
        }

        float anim = openAnim.getFloat();
        float cardW = 242.0f;
        float cardH = 246.0f;
        float cardX = (this.width - cardW) / 2.0f;
        float cardY = (this.height - cardH) / 2.0f;

        float scale = 0.92f + 0.08f * anim;
        context.getMatrices().push();
        context.getMatrices().translate(cardX + cardW / 2.0f, cardY + cardH / 2.0f, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-(cardX + cardW / 2.0f), -(cardY + cardH / 2.0f), 0);

        Render2D.drawMattePanel(context, cardX, cardY, cardW, cardH, 12.0f);

        Render2D.drawStrictCenteredText(context, this.textRenderer, "AETHER // VISUALS", cardX + cardW / 2.0f, cardY + 16, Render2D.ACCENT_CYAN);

        String currentNick = "USER: " + (this.client != null ? this.client.getSession().getUsername().toUpperCase() : "PLAYER") + "  |  BUILD 2.5";
        Render2D.drawStrictCenteredText(context, this.textRenderer, currentNick, cardX + cardW / 2.0f, cardY + 30, 0xFF7D839C);

        String[] buttons = {"SINGLEPLAYER", "MULTIPLAYER", "ACCOUNT", "OPTIONS", "EXIT"};
        float btnW = 192.0f;
        float btnH = 25.0f;
        float btnX = cardX + (cardW - btnW) / 2.0f;
        float btnY = cardY + 52.0f;

        for (String label : buttons) {
            boolean hovered = isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH);
            int bg = hovered ? 0xFF181B2B : Render2D.MATTE_CARD;
            Render2D.drawRoundedRect(context, btnX, btnY, btnW, btnH, 6.0f, bg);
            if (hovered) {
                Render2D.drawRoundedOutline(context, btnX, btnY, btnW, btnH, 6.0f, 1.1f,
                        Render2D.ACCENT_PURPLE, Render2D.ACCENT_CYAN);
            } else {
                Render2D.drawRoundedOutline(context, btnX, btnY, btnW, btnH, 6.0f, 0.9f,
                        0x558A2BE2, 0x5500F0FF);
            }

            int textColor = label.equals("EXIT") && hovered ? 0xFFFF4D6D : (hovered ? Render2D.ACCENT_CYAN : 0xFFE6E9F5);
            Render2D.drawStrictCenteredText(context, this.textRenderer, label, btnX + btnW / 2.0f, btnY + 9, textColor);

            btnY += 32.0f;
        }

        context.getMatrices().pop();
    }
        @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.client != null) {
            if (accountModalOpen) {
                float mW = 224.0f;
                float mH = 112.0f;
                float mX = (this.width - mW) / 2.0f;
                float mY = (this.height - mH) / 2.0f;

                if (isHovered(mouseX, mouseY, mX + 16, mY + 68, 90, 22)) {
                    applyOfflineNickname(nicknameInput);
                    accountModalOpen = false;
                    return true;
                }
                if (isHovered(mouseX, mouseY, mX + mW - 106, mY + 68, 90, 22)) {
                    accountModalOpen = false;
                    return true;
                }
                return true;
            }

            float cardW = 242.0f;
            float cardH = 246.0f;
            float cardX = (this.width - cardW) / 2.0f;
            float cardY = (this.height - cardH) / 2.0f;
            float btnW = 192.0f;
            float btnH = 25.0f;
            float btnX = cardX + (cardW - btnW) / 2.0f;
            float btnY = cardY + 52.0f;

            if (isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
                this.client.setScreen(new SelectWorldScreen(this));
                return true;
            }
            btnY += 32.0f;
            if (isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
                this.client.setScreen(new MultiplayerScreen(this));
                return true;
            }
            btnY += 32.0f;
            if (isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
                accountModalOpen = true;
                return true;
            }
            btnY += 32.0f;
            if (isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
                this.client.setScreen(new OptionsScreen(this, this.client.options));
                return true;
            }
            btnY += 32.0f;
            if (isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH)) {
                this.client.scheduleStop();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (accountModalOpen) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                accountModalOpen = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) {
                applyOfflineNickname(nicknameInput);
                accountModalOpen = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !nicknameInput.isEmpty()) {
                nicknameInput = nicknameInput.substring(0, nicknameInput.length() - 1);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (accountModalOpen && nicknameInput.length() < 16) {
            if (Character.isLetterOrDigit(chr) || chr == '_') {
                nicknameInput += chr;
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }

    private void applyOfflineNickname(String nick) {
        if (this.client == null) return;
        String clean = nick.trim();
        if (clean.length() < 3) return;
        UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + clean).getBytes());
        Session newSession = new Session(clean, offlineUuid, "", Optional.empty(), Optional.empty(), Session.AccountType.LEGACY);
        ((MinecraftClientAccessor) this.client).setSession(newSession);
    }

    private boolean isHovered(double mouseX, double mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
