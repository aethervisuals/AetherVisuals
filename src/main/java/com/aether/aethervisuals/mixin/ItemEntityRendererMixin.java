package com.aether.aethervisuals.mixin;

import com.aether.aethervisuals.AetherVisuals;
import com.aether.aethervisuals.module.impl.visuals.ItemPhysicsModule;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), require = 0)
    private void onRenderItem(ItemEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ItemPhysicsModule ip = AetherVisuals.getInstance() != null
                ? AetherVisuals.getInstance().getModuleManager().getModule(ItemPhysicsModule.class)
                : null;
        if (ip != null && ip.isEnabled()) {
            matrices.translate(0.0f, 0.04f, 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        }
    }
}
