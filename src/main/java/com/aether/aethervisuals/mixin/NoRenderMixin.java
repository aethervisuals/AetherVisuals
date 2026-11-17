package com.aether.aethervisuals.mixin;

import com.aether.aethervisuals.AetherVisuals;
import com.aether.aethervisuals.module.impl.render.NoRenderModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class NoRenderMixin {

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void onHurtCam(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        NoRenderModule nr = AetherVisuals.getInstance() != null
                ? AetherVisuals.getInstance().getModuleManager().getModule(NoRenderModule.class)
                : null;
        if (nr != null && nr.isEnabled() && nr.hurtCam.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "showFloatingItem", at = @At("HEAD"), cancellable = true)
    private void onShowFloatingItem(ItemStack floatingItem, CallbackInfo ci) {
        NoRenderModule nr = AetherVisuals.getInstance() != null
                ? AetherVisuals.getInstance().getModuleManager().getModule(NoRenderModule.class)
                : null;
        if (nr != null && nr.isEnabled() && nr.totem.getValue() && floatingItem.isOf(Items.TOTEM_OF_UNDYING)) {
            ci.cancel();
        }
    }
}
