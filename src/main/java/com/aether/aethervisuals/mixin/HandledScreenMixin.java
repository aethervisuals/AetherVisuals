package com.aether.aethervisuals.mixin;

import com.aether.aethervisuals.AetherVisuals;
import com.aether.aethervisuals.module.impl.render.AnimationModule;
import com.aether.aethervisuals.module.impl.world.AucHelperModule;
import com.aether.aethervisuals.util.animation.Animation;
import com.aether.aethervisuals.util.animation.Easing;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Unique
    private final Animation openAnim = new Animation(Easing.EASE_OUT_CUBIC, 220);

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        openAnim.setValue(0.0);
        openAnim.animateTo(1.0);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        AnimationModule anim = AetherVisuals.getInstance() != null
                ? AetherVisuals.getInstance().getModuleManager().getModule(AnimationModule.class)
                : null;
        if (anim != null && anim.isEnabled()) {
            float progress = openAnim.getFloat();
            float scale = 0.88f + 0.12f * progress;
            int w = context.getScaledWindowWidth();
            int h = context.getScaledWindowHeight();
            context.getMatrices().push();
            context.getMatrices().translate(w / 2.0f, h / 2.0f, 0);
            context.getMatrices().scale(scale, scale, 1.0f);
            context.getMatrices().translate(-w / 2.0f, -h / 2.0f, 0);
        }
    }

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void onDrawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        AucHelperModule auc = AetherVisuals.getInstance() != null
                ? AetherVisuals.getInstance().getModuleManager().getModule(AucHelperModule.class)
                : null;
        if (auc != null && auc.isEnabled() && slot.id == auc.getBestSlotIndex()) {
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x6600FF66);
            context.drawBorder(slot.x - 1, slot.y - 1, 18, 18, 0xFF00FF66);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        AnimationModule anim = AetherVisuals.getInstance() != null
                ? AetherVisuals.getInstance().getModuleManager().getModule(AnimationModule.class)
                : null;
        if (anim != null && anim.isEnabled()) {
            context.getMatrices().pop();
        }
    }
}
