package com.aether.aethervisuals.mixin;

import com.aether.aethervisuals.AetherVisuals;
import com.aether.aethervisuals.module.impl.render.AnimationModule;
import com.aether.aethervisuals.util.animation.Animation;
import com.aether.aethervisuals.util.animation.Easing;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {

    @Unique
    private final Animation aetherOpenAnim = new Animation(Easing.EASE_OUT_CUBIC, 220);

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        aetherOpenAnim.setValue(0.0);
        aetherOpenAnim.animateTo(1.0);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        AnimationModule animMod = AetherVisuals.getInstance() != null
                ? AetherVisuals.getInstance().getModuleManager().getModule(AnimationModule.class)
                : null;
        if (animMod != null && animMod.isEnabled()) {
            float progress = aetherOpenAnim.getFloat();
            float scale = 0.85f + 0.15f * progress;
            context.getMatrices().push();
            context.getMatrices().translate(this.width / 2.0f, this.height / 2.0f, 0);
            context.getMatrices().scale(scale, scale, 1.0f);
            context.getMatrices().translate(-this.width / 2.0f, -this.height / 2.0f, 0);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderReturn(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        AnimationModule animMod = AetherVisuals.getInstance() != null
                ? AetherVisuals.getInstance().getModuleManager().getModule(AnimationModule.class)
                : null;
        if (animMod != null && animMod.isEnabled()) {
            context.getMatrices().pop();
        }
    }
}
