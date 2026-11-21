package com.karl.playertagger.mixin;

import com.karl.playertagger.config.PlayerTaggerConfig;
import com.karl.playertagger.data.TagManager;
import com.karl.playertagger.render.NametagRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RendererLivingEntity.class)
public class MixinRendererLivingEntity<T extends EntityLivingBase> {

    @Inject(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V", at = @At("HEAD"), cancellable = true)
    private void playertagger$onRenderName(T entity, double x, double y, double z, CallbackInfo ci) {
        if (NametagRenderer.INSTANCE.onRenderLivingName(entity, x, y, z)) {
            ci.cancel();
        }
    }

    @Inject(method = "canRenderName(Lnet/minecraft/entity/EntityLivingBase;)Z", at = @At("HEAD"), cancellable = true)
    private void playertagger$onCanRenderName(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (PlayerTaggerConfig.enabled && PlayerTaggerConfig.nametagTagging && entity instanceof EntityPlayer) {
            Minecraft mc = Minecraft.getMinecraft();
            if (entity == mc.thePlayer && mc.gameSettings != null && mc.gameSettings.thirdPersonView > 0) {
                if (TagManager.getInstance().getTaggedPlayer(entity.getName()) != null) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
