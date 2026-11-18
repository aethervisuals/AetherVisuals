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

    private final Animation openAnim = new Animation(Easing.EASE_OUT_CUBIC, 350);
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
        context.fillGradient(0, 0, this.width, this.height, 0xFF06070C, 0xFF100A1E);

        if (accountModalOpen) {
            float mW = 220.0f;
            float mH = 110.0f;
            float mX = (this.width - mW) / 2.0f;
            float mY = (this.height - mH) / 2.0f;

            Render2D.drawMattePanel(context, mX, mY, mW, mH, 10.0f);
            Render2D.drawCleanText(context, this.textRenderer, "ACCOUNT // НИКНЕЙМ", mX + 16, mY + 14, 0xFFE6E6F2);

            Render2D.drawRoundedRect(context, mX + 16, mY + 32, mW - 32, 22, 5.0f, Render2D.MATTE_CARD);
            Render2D.drawRoundedOutline(context, mX + 16, mY + 32, mW - 32, 22, 5.0f, 1.0f,
                    Render2D.ACCENT_PURPLE, Render2D.ACCENT_CYAN);
            Render2D.drawCleanText(context, this.textRenderer, nicknameInput + "_", mX + 22, mY + 39, Render2D.ACCENT_CYAN);

            Render2D.drawRoundedRect(context, mX + 16, mY + 68, 88, 22, 5.0f, 0xFF5D21A8);
            Render2D.drawCleanText(context, this.textRenderer, "ПРИМЕНИТЬ", mX + 32, mY + 75, 0xFFFFFFFF);

            Render2D.drawRoundedRect(context, mX + mW - 104, mY + 68, 88, 22, 5.0f, 0xFF202332);
            Render2D.drawCleanText(context, this.textRenderer, "ОТМЕНА", mX + mW - 78, mY + 75, 0xFFB0B0C5);
            return;
        }

        float anim = openAnim.getFloat();
        float cardW = 240.0f;
        float cardH = 244.0f;
        float cardX = (this.width - cardW) / 2.0f;
        float cardY = (this.height - cardH) / 2.0f;

        float scale = 0.90f + 0.10f * anim;
        context.getMatrices().push();
        context.getMatrices().translate(cardX + cardW / 2.0f, cardY + cardH / 2.0f, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-(cardX + cardW / 2.0f), -(cardY + cardH / 2.0f), 0);

        Render2D.drawMattePanel(context, cardX, cardY, cardW, cardH, 12.0f);

        String title = "AETHER // VISUALS 2.5";
        float titleX = cardX + (cardW - this.textRenderer.getWidth(title)) / 2.0f;
        Render2D.drawCleanText(context, this.textRenderer, title, titleX, cardY + 16, Render2D.ACCENT_CYAN);

        String currentNick = "SESSION: " + (this.client != null ? this.client.getSession().getUsername() : "Player");
        float nickX = cardX + (cardW - this.textRenderer.getWidth(currentNick)) / 2.0f;
        Render2D.drawCleanText(context, this.textRenderer, currentNick, nickX, cardY + 30, 0xFF8E92A8);

        String[] buttons = {"SINGLEPLAYER", "MULTIPLAYER", "ACCOUNT", "OPTIONS", "QUIT"};
        float btnW = 190.0f;
        float btnH = 25.0f;
        float btnX = cardX + (cardW - btnW) / 2.0f;
        float btnY = cardY + 50.0f;

        for (String label : buttons) {
            boolean hovered = isHovered(mouseX, mouseY, btnX, btnY, btnW, btnH);
            int bg = hovered ? 0xFF1A1D2E : Render2D.MATTE_CARD;
            Render2D.drawRoundedRect(context, btnX, btnY, btnW, btnH, 6.0f, bg);
            if (hovered) {
                Render2D.drawRoundedOutline(context, btnX, btnY, btnW, btnH, 6.0f, 1.2f,
                        Render2D.ACCENT_PURPLE, Render2D.ACCENT_CYAN);
            } else {
                Render2D.drawRoundedOutline(context, btnX, btnY, btnW, btnH, 6.0f, 1.0f,
                        0x558A2BE2, 0x5500F0FF);
            }

            int textColor = label.equals("QUIT") && hovered ? 0xFFFF5577 : (hovered ? Render2D.ACCENT_CYAN : 0xFFE6E6F2);
            float textX = btnX + (btnW - this.textRenderer.getWidth(label)) / 2.0f;
            float textY = btnY + (btnH - this.textRenderer.fontHeight) / 2.0f + 1;
            Render2D.drawCleanText(context, this.textRenderer, label, textX, textY, textColor);

            btnY += 32.0f;
        }

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.client != null) {
            if (accountModalOpen) {
                float mW = 220.0f;
                float mH = 110.0f;
                float mX = (this.width - mW) / 2.0f;
                float mY = (this.height - mH) / 2.0f;

                if (isHovered(mouseX, mouseY, mX + 16, mY + 68, 88, 22)) {
                    applyOfflineNickname(nicknameInput);
                    accountModalOpen = false;
                    return true;
                }
                if (isHovered(mouseX, mouseY, mX + mW - 104, mY + 68, 88, 22)) {
                    accountModalOpen = false;
                    return true;
                }
                return true;
            }

            float cardW = 240.0f;
            float cardH = 244.0f;
            float cardX = (this.width - cardW) / 2.0f;
            float cardY = (this.height - cardH) / 2.0f;
            float btnW = 190.0f;
            float btnH = 25.0f;
            float btnX = cardX + (cardW - btnW) / 2.0f;
            float btnY = cardY + 50.0f;

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
