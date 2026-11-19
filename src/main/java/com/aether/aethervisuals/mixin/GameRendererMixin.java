package com.aether.aethervisuals.mixin;

import com.aether.aethervisuals.AetherVisuals;
import com.aether.aethervisuals.module.impl.render.ZoomModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true, require = 0)
    private void onGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        ZoomModule zoom = AetherVisuals.getInstance() != null
                ? AetherVisuals.getInstance().getModuleManager().getModule(ZoomModule.class)
                : null;
        if (zoom != null) {
            float mult = zoom.getSmoothFovMultiplier();
            if (Math.abs(mult - 1.0f) > 0.005f) {
                cir.setReturnValue(cir.getReturnValue() * mult);
            }
        }
    }
}
