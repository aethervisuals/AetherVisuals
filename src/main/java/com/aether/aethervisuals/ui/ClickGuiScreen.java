package com.aether.aethervisuals.ui;

import com.aether.aethervisuals.AetherVisuals;
import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
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

        float catY = panelY + 52;
        for (Category category : Category.values()) {
            boolean isSelected = category == selectedCategory;
            if (isSelected) {
                Render2D.drawRoundedRect(context, panelX + 12, catY, sidebarW - 12, 22, 6.0f, 0x778A2BE2);
                Render2D.drawRoundedOutline(context, panelX + 12, catY, sidebarW - 12, 22, 6.0f, 1.0f,
                        Render2D.ACCENT_PURPLE, Render2D.ACCENT_CYAN);
            }
            int textColor = isSelected ? 0xFFFFFFFF : 0xFFA0A0B0;
            context.drawTextWithShadow(this.textRenderer, category.getDisplayName(),
                    (int) (panelX + 22), (int) (catY + 7), textColor);
            catY += 28;
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

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            float panelW = 360.0f;
            float panelH = 230.0f;
            float panelX = (this.width - panelW) / 2.0f;
            float panelY = (this.height - panelH) / 2.0f;
            float sidebarW = 95.0f;

            float catY = panelY + 52;
            for (Category category : Category.values()) {
                if (isHovered(mouseX, mouseY, panelX + 12, catY, sidebarW - 12, 22)) {
                    selectedCategory = category;
                    return true;
                }
                catY += 28;
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

    private boolean isHovered(double mouseX, double mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
