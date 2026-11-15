package com.aether.aethervisuals.ui;

import com.aether.aethervisuals.AetherVisuals;
import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.impl.hud.KeystrokesModule;
import com.aether.aethervisuals.module.impl.hud.TargetHudModule;
import com.aether.aethervisuals.module.impl.hud.WatermarkModule;
import com.aether.aethervisuals.util.animation.Animation;
import com.aether.aethervisuals.util.animation.Easing;
import com.aether.aethervisuals.util.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

public class ClickGuiScreen extends Screen {

    private Category selectedCategory = Category.HUD;
    private final Animation openAnim = new Animation(Easing.EASE_OUT_CUBIC, 250);

    private Module draggingModule = null;
    private float dragOffsetX = 0.0f;
    private float dragOffsetY = 0.0f;

    public ClickGuiScreen() {
        super(Text.literal("AetherVisuals ClickGUI"));
    }

    @Override
    protected void init() {
        super.init();
        openAnim.setValue(0.0);
        openAnim.animateTo(1.0);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x66000000);

        float anim = openAnim.getFloat();
        float panelW = 360.0f;
        float panelH = 230.0f;
        float panelX = (this.width - panelW) / 2.0f;
        float panelY = (this.height - panelH) / 2.0f;

        float scale = 0.85f + 0.15f * anim;
        context.getMatrices().push();
        context.getMatrices().translate(panelX + panelW / 2.0f, panelY + panelH / 2.0f, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-(panelX + panelW / 2.0f), -(panelY + panelH / 2.0f), 0);

        Render2D.drawGlassPanel(context, panelX, panelY, panelW, panelH, 12.0f);

        float sidebarW = 95.0f;
        Render2D.drawRoundedRect(context, panelX + 6, panelY + 6, sidebarW, panelH - 12, 8.0f, 0x55080810);

        context.drawTextWithShadow(this.textRenderer, "AETHER", (int) (panelX + 24), (int) (panelY + 18), Render2D.ACCENT_CYAN);
        context.drawTextWithShadow(this.textRenderer, "VISUALS", (int) (panelX + 22), (int) (panelY + 29), Render2D.ACCENT_PURPLE);

        float catY = panelY + 48;
        for (Category category : Category.values()) {
            boolean isSelected = category == selectedCategory;
            if (isSelected) {
                Render2D.drawRoundedRect(context, panelX + 12, catY, sidebarW - 12, 20, 6.0f, 0x778A2BE2);
                Render2D.drawRoundedOutline(context, panelX + 12, catY, sidebarW - 12, 20, 6.0f, 1.0f,
                        Render2D.ACCENT_PURPLE, Render2D.ACCENT_CYAN);
            }
            int textColor = isSelected ? 0xFFFFFFFF : 0xFFA0A0B0;
            context.drawTextWithShadow(this.textRenderer, category.getDisplayName(),
                    (int) (panelX + 22), (int) (catY + 6), textColor);
            catY += 25;
        }

        float contentX = panelX + sidebarW + 14;
        float contentW = panelW - sidebarW - 22;
        float modY = panelY + 14;

        List<Module> modules = AetherVisuals.getInstance().getModuleManager().getModulesByCategory(selectedCategory);
        if (modules.isEmpty()) {
            context.drawTextWithShadow(this.textRenderer, "В этой категории пока пусто",
                    (int) (contentX + 35), (int) (panelY + panelH / 2 - 4), 0xFF808090);
        } else {
            for (Module module : modules) {
                Render2D.drawRoundedRect(context, contentX, modY, contentW, 36, 8.0f, 0x66141422);
                if (module.isEnabled()) {
                    Render2D.drawRoundedOutline(context, contentX, modY, contentW, 36, 8.0f, 1.0f,
                            Render2D.ACCENT_PURPLE, Render2D.ACCENT_CYAN);
                }

                context.drawTextWithShadow(this.textRenderer, module.getName(),
                        (int) (contentX + 10), (int) (modY + 7), 0xFFFFFFFF);
                context.drawTextWithShadow(this.textRenderer, module.getDescription(),
                        (int) (contentX + 10), (int) (modY + 20), 0xFF9090A5);

                float switchW = 28.0f;
                float switchH = 14.0f;
                float switchX = contentX + contentW - switchW - 10;
                float switchY = modY + (36 - switchH) / 2.0f;

                int switchBg = module.isEnabled() ? Render2D.ACCENT_PURPLE : 0x88303040;
                Render2D.drawRoundedRect(context, switchX, switchY, switchW, switchH, 7.0f, switchBg);

                float knobX = module.isEnabled() ? switchX + switchW - 12 : switchX + 2;
                Render2D.drawRoundedRect(context, knobX, switchY + 2, 10, 10, 5.0f, 0xFFFFFFFF);

                modY += 42;
            }
        }

        context.drawTextWithShadow(this.textRenderer, "Перетаскивайте HUD элементы мышкой",
                (int) (panelX + 105), (int) (panelY + panelH - 14), 0xFF707085);

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            WatermarkModule wm = AetherVisuals.getInstance().getModuleManager().getModule(WatermarkModule.class);
            if (wm != null && wm.isEnabled() && isHovered(mouseX, mouseY, wm.x, wm.y, wm.width, wm.height)) {
                draggingModule = wm;
                dragOffsetX = (float) mouseX - wm.x;
                dragOffsetY = (float) mouseY - wm.y;
                return true;
            }

            TargetHudModule th = AetherVisuals.getInstance().getModuleManager().getModule(TargetHudModule.class);
            if (th != null && th.isEnabled() && isHovered(mouseX, mouseY, th.x, th.y, th.width, th.height)) {
                draggingModule = th;
                dragOffsetX = (float) mouseX - th.x;
                dragOffsetY = (float) mouseY - th.y;
                return true;
            }

            KeystrokesModule ks = AetherVisuals.getInstance().getModuleManager().getModule(KeystrokesModule.class);
            if (ks != null && ks.isEnabled() && isHovered(mouseX, mouseY, ks.x, ks.y, ks.width, ks.height)) {
                draggingModule = ks;
                dragOffsetX = (float) mouseX - ks.x;
                dragOffsetY = (float) mouseY - ks.y;
                return true;
            }

            float panelW = 360.0f;
            float panelH = 230.0f;
            float panelX = (this.width - panelW) / 2.0f;
            float panelY = (this.height - panelH) / 2.0f;
            float sidebarW = 95.0f;

            float catY = panelY + 48;
            for (Category category : Category.values()) {
                if (isHovered(mouseX, mouseY, panelX + 12, catY, sidebarW - 12, 20)) {
                    selectedCategory = category;
                    return true;
                }
                catY += 25;
            }

            float contentX = panelX + sidebarW + 14;
            float contentW = panelW - sidebarW - 22;
            float modY = panelY + 14;

            List<Module> modules = AetherVisuals.getInstance().getModuleManager().getModulesByCategory(selectedCategory);
            for (Module module : modules) {
                if (isHovered(mouseX, mouseY, contentX, modY, contentW, 36)) {
                    module.toggle();
                    return true;
                }
                modY += 42;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && draggingModule != null) {
            if (draggingModule instanceof WatermarkModule wm) {
                wm.x = (float) mouseX - dragOffsetX;
                wm.y = (float) mouseY - dragOffsetY;
                return true;
            } else if (draggingModule instanceof TargetHudModule th) {
                th.x = (float) mouseX - dragOffsetX;
                th.y = (float) mouseY - dragOffsetY;
                return true;
            } else if (draggingModule instanceof KeystrokesModule ks) {
                ks.x = (float) mouseX - dragOffsetX;
                ks.y = (float) mouseY - dragOffsetY;
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingModule != null) {
            draggingModule = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isHovered(double mouseX, double mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
