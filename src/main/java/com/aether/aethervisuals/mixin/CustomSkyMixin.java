package com.aether.aethervisuals.mixin;

import com.aether.aethervisuals.AetherVisuals;
import com.aether.aethervisuals.module.impl.world.CustomSkyModule;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class CustomSkyMixin {

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true, require = 0)
    private void onGetSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Integer> cir) {
        CustomSkyModule sky = AetherVisuals.getInstance() != null
                ? AetherVisuals.getInstance().getModuleManager().getModule(CustomSkyModule.class)
                : null;
        if (sky != null && sky.isEnabled()) {
            cir.setReturnValue(sky.getSkyColorInt());
        }
    }
}
