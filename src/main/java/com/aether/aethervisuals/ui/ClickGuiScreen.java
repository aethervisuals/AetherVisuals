package com.aether.aethervisuals.ui;

import com.aether.aethervisuals.AetherVisuals;
import com.aether.aethervisuals.module.Category;
import com.aether.aethervisuals.module.Module;
import com.aether.aethervisuals.module.impl.hud.ArmorHudModule;
import com.aether.aethervisuals.module.impl.hud.CoordsModule;
import com.aether.aethervisuals.module.impl.hud.KeystrokesModule;
import com.aether.aethervisuals.module.impl.hud.PotionHudModule;
import com.aether.aethervisuals.module.impl.hud.TargetHudModule;
import com.aether.aethervisuals.module.impl.hud.WatermarkModule;
import com.aether.aethervisuals.module.impl.render.AnimationModule;
import com.aether.aethervisuals.module.impl.world.EventsModule;
import com.aether.aethervisuals.module.impl.world.FTHelperModule;
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
    private final Animation openAnim = new Animation(Easing.EASE_OUT_CUBIC, 220);

    private String searchQuery = "";
    private boolean searchFocused = false;
    private final Set<Module> expandedModules = new HashSet<>();

    private Module draggingModule = null;
    private float dragOffsetX = 0.0f;
    private float dragOffsetY = 0.0f;

    public ClickGuiScreen() {
        super(Text.literal("AetherVisuals Interface"));
    }

    @Override
    protected void init() {
        super.init();
        openAnim.setValue(0.0);
        openAnim.animateTo(1.0);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x85000000);
        highlightHudBounds(context, mouseX, mouseY);

        AnimationModule animMod = AetherVisuals.getInstance().getModuleManager().getModule(AnimationModule.class);
        boolean useAnim = animMod != null && animMod.isEnabled();
        float anim = useAnim ? openAnim.getFloat() : 1.0f;

        float panelW = 396.0f;
        float panelH = 254.0f;
        float panelX = (this.width - panelW) / 2.0f;
        float panelY = (this.height - panelH) / 2.0f;

        float scale = 0.90f + 0.10f * anim;
        context.getMatrices().push();
        context.getMatrices().translate(panelX + panelW / 2.0f, panelY + panelH / 2.0f, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(-(panelX + panelW / 2.0f), -(panelY + panelH / 2.0f), 0);

        Render2D.drawMattePanel(context, panelX, panelY, panelW, panelH, 10.0f);

        float sidebarW = 104.0f;
        Render2D.drawRoundedRect(context, panelX + 6, panelY + 6, sidebarW, panelH - 12, 7.0f, 0xFF050609);

        Render2D.drawStrictCenteredText(context, this.textRenderer, "AETHER // 2.5", panelX + 6 + sidebarW / 2.0f, panelY + 14, Render2D.ACCENT_CYAN);
        Render2D.drawStrictCenteredText(context, this.textRenderer, "VISUAL SYSTEM", panelX + 6 + sidebarW / 2.0f, panelY + 25, 0xFF70758A);

        float catY = panelY + 42;
        for (Category category : Category.values()) {
            boolean isSelected = category == selectedCategory && searchQuery.isEmpty();
            if (isSelected) {
                Render2D.drawRoundedRect(context, panelX + 12, catY, sidebarW - 12, 19, 5.0f, 0xFF281047);
                Render2D.drawRoundedOutline(context, panelX + 12, catY, sidebarW - 12, 19, 5.0f, 1.0f,
                        Render2D.ACCENT_PURPLE, Render2D.ACCENT_CYAN);
            }
            int textColor = isSelected ? 0xFFFFFFFF : 0xFF8A90A6;
            Render2D.drawStrictText(context, this.textRenderer, category.getDisplayName().toUpperCase(),
                    panelX + 20, catY + 6, textColor);
            catY += 22;
        }

        if (this.client != null && this.client.player != null) {
            int boxX = (int) (panelX + 12);
            int boxY = (int) (panelY + panelH - 68);
            Render2D.drawRoundedRect(context, boxX, boxY, sidebarW - 12, 56, 6.0f, Render2D.MATTE_CARD);
            InventoryScreen.drawEntity(context, boxX + 2, boxY + 2, boxX + 36, boxY + 46, 19,
                    0.0625f, mouseX, mouseY, this.client.player);
            String nick = this.client.player.getName().getString();
            if (this.textRenderer.getWidth(nick) > 48) {
                nick = this.textRenderer.trimToWidth(nick, 44) + "..";
            }
            Render2D.drawStrictText(context, this.textRenderer, nick, boxX + 39, boxY + 18, 0xFFFFFFFF);
            Render2D.drawStrictText(context, this.textRenderer, "ONLINE", boxX + 39, boxY + 30, Render2D.ACCENT_CYAN);
        }
                float contentX = panelX + sidebarW + 14;
        float contentW = panelW - sidebarW - 22;
        float searchY = panelY + 10;
        Render2D.drawRoundedRect(context, contentX, searchY, contentW, 20, 5.0f, Render2D.MATTE_CARD);
        Render2D.drawRoundedOutline(context, contentX, searchY, contentW, 20, 5.0f, 1.0f,
                searchFocused ? Render2D.ACCENT_CYAN : 0x558A2BE2, Render2D.ACCENT_CYAN);

        String searchText = searchQuery.isEmpty() && !searchFocused
                ? "SEARCH MODULE..."
                : searchQuery + (searchFocused ? "_" : "");
        int searchColor = searchQuery.isEmpty() && !searchFocused ? 0xFF656A80 : 0xFFFFFFFF;
        Render2D.drawStrictText(context, this.textRenderer, searchText, contentX + 8, searchY + 6, searchColor);

        float modY = searchY + 26;
        List<Module> modules = getFilteredModules();

        for (Module module : modules) {
            if (modY + 34 > panelY + panelH - 12) break;

            Render2D.drawRoundedRect(context, contentX, modY, contentW, 34, 6.0f, Render2D.MATTE_CARD);
            if (module.isEnabled()) {
                Render2D.drawRoundedOutline(context, contentX, modY, contentW, 34, 6.0f, 1.0f,
                        Render2D.ACCENT_PURPLE, Render2D.ACCENT_CYAN);
            }

            Render2D.drawStrictText(context, this.textRenderer, module.getName(),
                    contentX + 10, modY + 6, 0xFFFFFFFF);
            String desc = this.textRenderer.trimToWidth(module.getDescription(), (int) (contentW - 55));
            Render2D.drawStrictText(context, this.textRenderer, desc,
                    contentX + 10, modY + 19, 0xFF80869C);

            float switchW = 26.0f;
            float switchH = 13.0f;
            float switchX = contentX + contentW - switchW - 10;
            float switchY = modY + (34 - switchH) / 2.0f;

            int switchBg = module.isEnabled() ? Render2D.ACCENT_PURPLE : 0xFF222534;
            Render2D.drawRoundedRect(context, switchX, switchY, switchW, switchH, 6.5f, switchBg);
            float knobX = module.isEnabled() ? switchX + switchW - 11 : switchX + 2;
            Render2D.drawRoundedRect(context, knobX, switchY + 2, 9, 9, 4.5f, 0xFFFFFFFF);

            modY += 38;

            if (expandedModules.contains(module) && !module.getSettings().isEmpty()) {
                for (Setting<?> setting : module.getSettings()) {
                    if (!setting.isVisible() || modY + 20 > panelY + panelH - 10) continue;
                    Render2D.drawRoundedRect(context, contentX + 6, modY, contentW - 12, 20, 4.0f, 0xFF0A0B11);
                    Render2D.drawStrictText(context, this.textRenderer, setting.getName(),
                            contentX + 12, modY + 6, 0xFFC8CEE0);

                    if (setting instanceof BooleanSetting boolSet) {
                        String state = boolSet.getValue() ? "ON" : "OFF";
                        int col = boolSet.getValue() ? Render2D.ACCENT_CYAN : 0xFF656A80;
                        Render2D.drawStrictText(context, this.textRenderer, state,
                                contentX + contentW - 34, modY + 6, col);
                    } else if (setting instanceof ModeSetting modeSet) {
                        String val = modeSet.getValue();
                        Render2D.drawStrictText(context, this.textRenderer, val,
                                contentX + contentW - this.textRenderer.getWidth(val) - 14,
                                modY + 6, Render2D.ACCENT_CYAN);
                    } else if (setting instanceof NumberSetting numSet) {
                        String val = String.format("%.1f", numSet.getValue());
                        Render2D.drawStrictText(context, this.textRenderer, val,
                                contentX + contentW - this.textRenderer.getWidth(val) - 14,
                                modY + 6, Render2D.ACCENT_CYAN);
                    }
                    modY += 23;
                }
            }
        }

        context.getMatrices().pop();
    }

    private void highlightHudBounds(DrawContext context, int mouseX, int mouseY) {
        WatermarkModule wm = AetherVisuals.getInstance().getModuleManager().getModule(WatermarkModule.class);
        if (wm != null && wm.isEnabled()) drawDragBox(context, wm.x, wm.y, wm.width, wm.height, mouseX, mouseY, draggingModule == wm);
        CoordsModule cm = AetherVisuals.getInstance().getModuleManager().getModule(CoordsModule.class);
        if (cm != null && cm.isEnabled()) drawDragBox(context, cm.x, cm.y, cm.width, cm.height, mouseX, mouseY, draggingModule == cm);
        TargetHudModule th = AetherVisuals.getInstance().getModuleManager().getModule(TargetHudModule.class);
        if (th != null && th.isEnabled()) drawDragBox(context, th.x, th.y, th.width, th.height, mouseX, mouseY, draggingModule == th);
        KeystrokesModule ks = AetherVisuals.getInstance().getModuleManager().getModule(KeystrokesModule.class);
        if (ks != null && ks.isEnabled()) drawDragBox(context, ks.x, ks.y, ks.width, ks.height, mouseX, mouseY, draggingModule == ks);
        PotionHudModule ph = AetherVisuals.getInstance().getModuleManager().getModule(PotionHudModule.class);
        if (ph != null && ph.isEnabled()) drawDragBox(context, ph.x, ph.y, ph.width, ph.height, mouseX, mouseY, draggingModule == ph);
        ArmorHudModule ah = AetherVisuals.getInstance().getModuleManager().getModule(ArmorHudModule.class);
        if (ah != null && ah.isEnabled()) drawDragBox(context, ah.x, ah.y, ah.width, ah.height, mouseX, mouseY, draggingModule == ah);
        FTHelperModule ft = AetherVisuals.getInstance().getModuleManager().getModule(FTHelperModule.class);
        if (ft != null && ft.isEnabled()) drawDragBox(context, ft.x, ft.y, ft.width, ft.height, mouseX, mouseY, draggingModule == ft);
        EventsModule ev = AetherVisuals.getInstance().getModuleManager().getModule(EventsModule.class);
        if (ev != null && ev.isEnabled()) drawDragBox(context, ev.x, ev.y, ev.width, ev.height, mouseX, mouseY, draggingModule == ev);
    }

    private void drawDragBox(DrawContext context, float x, float y, float w, float h, int mouseX, int mouseY, boolean active) {
        if (active || isHovered(mouseX, mouseY, x - 4, y - 4, w + 8, h + 8)) {
            Render2D.drawRoundedOutline(context, x - 2, y - 2, w + 4, h + 4, 6.0f, 1.2f,
                    Render2D.ACCENT_CYAN, Render2D.ACCENT_PURPLE);
        }
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
            if (wm != null && wm.isEnabled() && isHovered(mouseX, mouseY, wm.x - 4, wm.y - 4, wm.width + 8, wm.height + 8)) {
                draggingModule = wm; dragOffsetX = (float) mouseX - wm.x; dragOffsetY = (float) mouseY - wm.y; return true;
            }
            CoordsModule cm = AetherVisuals.getInstance().getModuleManager().getModule(CoordsModule.class);
            if (cm != null && cm.isEnabled() && isHovered(mouseX, mouseY, cm.x - 4, cm.y - 4, cm.width + 8, cm.height + 8)) {
                draggingModule = cm; dragOffsetX = (float) mouseX - cm.x; dragOffsetY = (float) mouseY - cm.y; return true;
            }
            TargetHudModule th = AetherVisuals.getInstance().getModuleManager().getModule(TargetHudModule.class);
            if (th != null && th.isEnabled() && isHovered(mouseX, mouseY, th.x - 4, th.y - 4, th.width + 8, th.height + 8)) {
                draggingModule = th; dragOffsetX = (float) mouseX - th.x; dragOffsetY = (float) mouseY - th.y; return true;
            }
            KeystrokesModule ks = AetherVisuals.getInstance().getModuleManager().getModule(KeystrokesModule.class);
            if (ks != null && ks.isEnabled() && isHovered(mouseX, mouseY, ks.x - 4, ks.y - 4, ks.width + 8, ks.height + 8)) {
                draggingModule = ks; dragOffsetX = (float) mouseX - ks.x; dragOffsetY = (float) mouseY - ks.y; return true;
            }
            PotionHudModule ph = AetherVisuals.getInstance().getModuleManager().getModule(PotionHudModule.class);
            if (ph != null && ph.isEnabled() && isHovered(mouseX, mouseY, ph.x - 4, ph.y - 4, ph.width + 8, ph.height + 8)) {
                draggingModule = ph; dragOffsetX = (float) mouseX - ph.x; dragOffsetY = (float) mouseY - ph.y; return true;
            }
            ArmorHudModule ah = AetherVisuals.getInstance().getModuleManager().getModule(ArmorHudModule.class);
            if (ah != null && ah.isEnabled() && isHovered(mouseX, mouseY, ah.x - 4, ah.y - 4, ah.width + 8, ah.height + 8)) {
                draggingModule = ah; dragOffsetX = (float) mouseX - ah.x; dragOffsetY = (float) mouseY - ah.y; return true;
            }
            FTHelperModule ft = AetherVisuals.getInstance().getModuleManager().getModule(FTHelperModule.class);
            if (ft != null && ft.isEnabled() && isHovered(mouseX, mouseY, ft.x - 4, ft.y - 4, ft.width + 8, ft.height + 8)) {
                draggingModule = ft; dragOffsetX = (float) mouseX - ft.x; dragOffsetY = (float) mouseY - ft.y; return true;
            }
            EventsModule ev = AetherVisuals.getInstance().getModuleManager().getModule(EventsModule.class);
            if (ev != null && ev.isEnabled() && isHovered(mouseX, mouseY, ev.x - 4, ev.y - 4, ev.width + 8, ev.height + 8)) {
                draggingModule = ev; dragOffsetX = (float) mouseX - ev.x; dragOffsetY = (float) mouseY - ev.y; return true;
            }
        }

        float panelW = 396.0f;
        float panelH = 254.0f;
        float panelX = (this.width - panelW) / 2.0f;
        float panelY = (this.height - panelH) / 2.0f;
        float sidebarW = 104.0f;
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
                if (button == 0) { module.toggle(); return true; }
                else if (button == 1 && !module.getSettings().isEmpty()) {
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
            float nx = (float) mouseX - dragOffsetX;
            float ny = (float) mouseY - dragOffsetY;
            if (draggingModule instanceof WatermarkModule wm) { wm.x = nx; wm.y = ny; return true; }
            if (draggingModule instanceof CoordsModule cm) { cm.x = nx; cm.y = ny; return true; }
            if (draggingModule instanceof TargetHudModule th) { th.x = nx; th.y = ny; return true; }
            if (draggingModule instanceof KeystrokesModule ks) { ks.x = nx; ks.y = ny; return true; }
            if (draggingModule instanceof PotionHudModule ph) { ph.x = nx; ph.y = ny; return true; }
            if (draggingModule instanceof ArmorHudModule ah) { ah.x = nx; ah.y = ny; return true; }
            if (draggingModule instanceof FTHelperModule ft) { ft.x = nx; ft.y = ny; return true; }
            if (draggingModule instanceof EventsModule ev) { ev.x = nx; ev.y = ny; return true; }
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
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) { searchFocused = false; return true; }
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
