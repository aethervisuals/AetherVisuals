package com.aether.aethervisuals.ui;

import com.aether.aethervisuals.AetherVisuals;
import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.impl.hud.CoordsModule;
import com.aether.aethervisuals.module.impl.hud.KeystrokesModule;
import com.aether.aethervisuals.module.impl.hud.TargetHudModule;
import com.aether.aethervisuals.module.impl.hud.WatermarkModule;
import com.aether.aethervisuals.module.impl.render.AnimationModule;
import com.aether.aethervisuals.module.settings.BooleanSetting;
import com.aether.aethervisuals.module.settings.ModeSetting;
import com.aether.aethervisuals.module.settings.NumberSetting;
import com.aether.aethervisuals.module.settings.Setting;
import com.aether.aethervisuals.util.animation.Animation;
import com.aether.aethervisuals.util.animation.Easing;
import com.aether.aethervisuals.util.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClickGuiScreen extends Screen {

    private Category selectedCategory = Category.HUD;
    private final Animation openAnim = new Animation(Easing.EASE_OUT_CUBIC, 240);

    private String searchQuery = "";
    private boolean searchFocused = false;
    private final Set<Module> expandedModules = new HashSet<>();

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
        context.fill(0, 0, this.width, this.height, 0x77000000);

        AnimationModule animMod = AetherVisuals.getInstance().getModuleManager().getModule(AnimationModule.class);
        boolean useAnim = animMod != null && animMod.isEnabled();
        float anim = useAnim ? openAnim.getFloat() : 1.0f;

        float panelW = 390.0f;
        float panelH = 250.0f;
        float panelX = (this.width - panelW) / 2.0f;
        float panelY = (this.height - panelH) / 2.0f;

        float scale = 0.88f + 0.12f * anim;
        context.getMatrices().push();
        context.getMatrices().translate(panelX + panelW / 2.0f, panelY + panelH / 2.0f, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-(panelX + panelW / 2.0f), -(panelY + panelH / 2.0f), 0);

        Render2D.drawMattePanel(context, panelX, panelY, panelW, panelH, 12.0f);

        float sidebarW = 100.0f;
        Render2D.drawRoundedRect(context, panelX + 6, panelY + 6, sidebarW, panelH - 12, 8.0f, 0xFF06070B);

        context.drawTextWithShadow(this.textRenderer, "AETHER", (int) (panelX + 26), (int) (panelY + 14), Render2D.ACCENT_CYAN);
        context.drawTextWithShadow(this.textRenderer, "VISUALS 2.0", (int) (panelX + 18), (int) (panelY + 25), Render2D.ACCENT_PURPLE);

        float catY = panelY + 42;
        for (Category category : Category.values()) {
            boolean isSelected = category == selectedCategory && searchQuery.isEmpty();
            if (isSelected) {
                Render2D.drawRoundedRect(context, panelX + 12, catY, sidebarW - 12, 19, 6.0f, 0xFF3B1363);
                Render2D.drawRoundedOutline(context, panelX + 12, catY, sidebarW - 12, 19, 6.0f, 1.0f,
                        Render2D.ACCENT_PURPLE, Render2D.ACCENT_CYAN);
            }
            int textColor = isSelected ? 0xFFFFFFFF : 0xFFA0A0B5;
            context.drawTextWithShadow(this.textRenderer, category.getDisplayName(),
                    (int) (panelX + 20), (int) (catY + 5), textColor);
            catY += 22;
        }

        if (this.client != null && this.client.player != null) {
            int boxX = (int) (panelX + 12);
            int boxY = (int) (panelY + panelH - 68);
            Render2D.drawRoundedRect(context, boxX, boxY, sidebarW - 12, 56, 7.0f, Render2D.MATTE_CARD);
            InventoryScreen.drawEntity(context, boxX + 2, boxY + 2, boxX + 36, boxY + 46, 19,
                    0.0625f, mouseX, mouseY, this.client.player);
            String nick = this.client.player.getName().getString();
            if (this.textRenderer.getWidth(nick) > 46) {
                nick = this.textRenderer.trimToWidth(nick, 42) + "..";
            }
            context.drawTextWithShadow(this.textRenderer, nick, boxX + 38, boxY + 22, Render2D.ACCENT_CYAN);
        }

        float contentX = panelX + sidebarW + 14;
        float contentW = panelW - sidebarW - 22;
        float searchY = panelY + 10;
        Render2D.drawRoundedRect(context, contentX, searchY, contentW, 20, 6.0f, Render2D.MATTE_CARD);
        Render2D.drawRoundedOutline(context, contentX, searchY, contentW, 20, 6.0f, 1.0f,
                searchFocused ? Render2D.ACCENT_CYAN : 0x668A2BE2, Render2D.ACCENT_CYAN);

        String searchText = searchQuery.isEmpty() && !searchFocused
                ? "Поиск функции..."
                : searchQuery + (searchFocused ? "_" : "");
        int searchColor = searchQuery.isEmpty() && !searchFocused ? 0xFF75758A : 0xFFFFFFFF;
        context.drawTextWithShadow(this.textRenderer, searchText, (int) (contentX + 8), (int) (searchY + 6), searchColor);

        float modY = searchY + 26;
        List<Module> modules = getFilteredModules();

        for (Module module : modules) {
            if (modY + 34 > panelY + panelH - 12) break;

            Render2D.drawRoundedRect(context, contentX, modY, contentW, 34, 8.0f, Render2D.MATTE_CARD);
            if (module.isEnabled()) {
                Render2D.drawRoundedOutline(context, contentX, modY, contentW, 34, 8.0f, 1.0f,
                        Render2D.ACCENT_PURPLE, Render2D.ACCENT_CYAN);
            }

            context.drawTextWithShadow(this.textRenderer, module.getName(),
                    (int) (contentX + 10), (int) (modY + 6), 0xFFFFFFFF);
            String desc = this.textRenderer.trimToWidth(module.getDescription(), (int) (contentW - 55));
            context.drawTextWithShadow(this.textRenderer, desc,
                    (int) (contentX + 10), (int) (modY + 19), 0xFF9090A5);

            float switchW = 26.0f;
            float switchH = 13.0f;
            float switchX = contentX + contentW - switchW - 10;
            float switchY = modY + (34 - switchH) / 2.0f;

            int switchBg = module.isEnabled() ? Render2D.ACCENT_PURPLE : 0xFF282A3A;
            Render2D.drawRoundedRect(context, switchX, switchY, switchW, switchH, 6.5f, switchBg);
            float knobX = module.isEnabled() ? switchX + switchW - 11 : switchX + 2;
            Render2D.drawRoundedRect(context, knobX, switchY + 2, 9, 9, 4.5f, 0xFFFFFFFF);

            modY += 38;

            if (expandedModules.contains(module) && !module.getSettings().isEmpty()) {
                for (Setting<?> setting : module.getSettings()) {
                    if (!setting.isVisible() || modY + 20 > panelY + panelH - 10) continue;
                    Render2D.drawRoundedRect(context, contentX + 6, modY, contentW - 12, 20, 5.0f, 0xFF0B0C14);
                    context.drawTextWithShadow(this.textRenderer, setting.getName(),
                            (int) (contentX + 12), (int) (modY + 6), 0xFFD0D0E0);

                    if (setting instanceof BooleanSetting boolSet) {
                        String state = boolSet.getValue() ? "ВКЛ" : "ВЫКЛ";
                        int col = boolSet.getValue() ? Render2D.ACCENT_CYAN : 0xFF707085;
                        context.drawTextWithShadow(this.textRenderer, state,
                                (int) (contentX + contentW - 38), (int) (modY + 6), col);
                    } else if (setting instanceof ModeSetting modeSet) {
                        String val = modeSet.getValue();
                        context.drawTextWithShadow(this.textRenderer, val,
                                (int) (contentX + contentW - this.textRenderer.getWidth(val) - 14),
                                (int) (modY + 6), Render2D.ACCENT_CYAN);
                    } else if (setting instanceof NumberSetting numSet) {
                        String val = String.format("%.1f", numSet.getValue());
                        context.drawTextWithShadow(this.textRenderer, val,
                                (int) (contentX + contentW - this.textRenderer.getWidth(val) - 14),
                                (int) (modY + 6), Render2D.ACCENT_CYAN);
                    }
                    modY += 23;
                }
            }
        }

        context.getMatrices().pop();
    }
        private List<Module> getFilteredModules() {
        if (!searchQuery.isEmpty()) {
            String q = searchQuery.toLowerCase();
            return AetherVisuals.getInstance().getModuleManager().getModules().stream()
                    .filter(m -> m.getName().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }
        return AetherVisuals.getInstance().getModuleManager().getModulesByCategory(selectedCategory);
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
            CoordsModule cm = AetherVisuals.getInstance().getModuleManager().getModule(CoordsModule.class);
            if (cm != null && cm.isEnabled() && isHovered(mouseX, mouseY, cm.x, cm.y, cm.width, cm.height)) {
                draggingModule = cm;
                dragOffsetX = (float) mouseX - cm.x;
                dragOffsetY = (float) mouseY - cm.y;
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
        }

        float panelW = 390.0f;
        float panelH = 250.0f;
        float panelX = (this.width - panelW) / 2.0f;
        float panelY = (this.height - panelH) / 2.0f;
        float sidebarW = 100.0f;
        float contentX = panelX + sidebarW + 14;
        float contentW = panelW - sidebarW - 22;

        if (button == 0) {
            searchFocused = isHovered(mouseX, mouseY, contentX, panelY + 10, contentW, 20);

            float catY = panelY + 42;
            for (Category category : Category.values()) {
                if (isHovered(mouseX, mouseY, panelX + 12, catY, sidebarW - 12, 19)) {
                    selectedCategory = category;
                    searchQuery = "";
                    return true;
                }
                catY += 22;
            }
        }

        float modY = panelY + 36;
        for (Module module : getFilteredModules()) {
            if (modY + 34 > panelY + panelH - 12) break;

            if (isHovered(mouseX, mouseY, contentX, modY, contentW, 34)) {
                if (button == 0) {
                    module.toggle();
                    return true;
                } else if (button == 1 && !module.getSettings().isEmpty()) {
                    if (expandedModules.contains(module)) expandedModules.remove(module);
                    else expandedModules.add(module);
                    return true;
                }
            }
            modY += 38;

            if (expandedModules.contains(module) && !module.getSettings().isEmpty()) {
                for (Setting<?> setting : module.getSettings()) {
                    if (!setting.isVisible() || modY + 20 > panelY + panelH - 10) continue;
                    if (button == 0 && isHovered(mouseX, mouseY, contentX + 6, modY, contentW - 12, 20)) {
                        if (setting instanceof BooleanSetting boolSet) boolSet.toggle();
                        else if (setting instanceof ModeSetting modeSet) modeSet.cycle();
                        else if (setting instanceof NumberSetting numSet) {
                            double next = numSet.getValue() + numSet.getStep();
                            if (next > numSet.getMax()) next = numSet.getMin();
                            numSet.setValue(next);
                        }
                        return true;
                    }
                    modY += 23;
                }
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
            } else if (draggingModule instanceof CoordsModule cm) {
                cm.x = (float) mouseX - dragOffsetX;
                cm.y = (float) mouseY - dragOffsetY;
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused && searchQuery.length() < 20) {
            searchQuery += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    private boolean isHovered(double mouseX, double mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
