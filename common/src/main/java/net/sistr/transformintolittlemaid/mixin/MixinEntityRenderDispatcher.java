package net.sistr.transformintolittlemaid.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import net.sistr.transformintolittlemaid.client.renderer.LittleMaidPlayerRenderer;
import net.sistr.transformintolittlemaid.util.LittleMaidTransformable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
    private LittleMaidPlayerRenderer renderer_TLM;

    @Inject(method = "getRenderer", at = @At("HEAD"), cancellable = true)
    public <T extends Entity> void onGetRenderer(T entity, CallbackInfoReturnable<EntityRenderer<? super T>> cir) {
        if (entity instanceof PlayerEntity && ((LittleMaidTransformable) entity).isTransformedLittleMaid_TLM()) {
            cir.setReturnValue((EntityRenderer<? super T>) renderer_TLM);
        }
    }

    @Inject(method = "reload", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onReload(ResourceManager manager, CallbackInfo ci, EntityRendererFactory.Context context) {
        renderer_TLM = new LittleMaidPlayerRenderer(context);
    }


}
