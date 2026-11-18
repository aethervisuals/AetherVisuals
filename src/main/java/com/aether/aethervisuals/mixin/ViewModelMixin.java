package com.aether.aethervisuals.mixin;

import com.aether.aethervisuals.AetherVisuals;
import com.aether.aethervisuals.module.impl.render.ViewModelModule;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class ViewModelMixin {

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), require = 0)
    private void onRenderFirstPersonItemHead(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ViewModelModule vm = AetherVisuals.getInstance() != null
                ? AetherVisuals.getInstance().getModuleManager().getModule(ViewModelModule.class)
                : null;
        if (vm != null && vm.isEnabled()) {
            float sign = (hand == Hand.MAIN_HAND && player.getMainArm() == Arm.RIGHT) ? 1.0f : -1.0f;
            matrices.translate(
                    vm.posX.getValue().floatValue() * sign,
                    vm.posY.getValue().floatValue(),
                    vm.posZ.getValue().floatValue()
            );

            String style = vm.swingStyle.getValue();
            if (swingProgress > 0.0f) {
                float sin = (float) Math.sin(swingProgress * Math.PI);
                if (style.equals("Плавный")) {
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-sin * 35.0f));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sin * 15.0f * sign));
                } else if (style.equals("Сильный")) {
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-sin * 65.0f));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin * 25.0f * sign));
                } else if (style.equals("Вращение")) {
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(swingProgress * 360.0f * sign));
                }
            }

            float s = vm.scale.getValue().floatValue();
            matrices.scale(s, s, s);
        }
    }
}
