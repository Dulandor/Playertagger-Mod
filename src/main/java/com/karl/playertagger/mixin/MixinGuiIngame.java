package com.karl.playertagger.mixin;

import com.karl.playertagger.render.AlertManager;
import net.minecraft.client.gui.GuiIngame;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {

    @Inject(method = "renderGameOverlay(F)V", at = @At("RETURN"))
    private void playertagger$onRenderGameOverlay(float partialTicks, CallbackInfo ci) {
        AlertManager.INSTANCE.onRenderOverlay();
    }
}
