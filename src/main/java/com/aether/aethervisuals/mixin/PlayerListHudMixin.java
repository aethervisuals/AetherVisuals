package com.aether.aethervisuals.mixin;

import com.aether.aethervisuals.network.AetherNetwork;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true, require = 0)
    private void onGetPlayerName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
        if (entry == null || entry.getProfile() == null) return;
        String name = entry.getProfile().getName();
        if (AetherNetwork.isAetherUser(name)) {
            MutableText badge = Text.literal("[AV] ")
                    .formatted(Formatting.RED, Formatting.BOLD);
            cir.setReturnValue(badge.append(cir.getReturnValue()));
        }
    }
}
