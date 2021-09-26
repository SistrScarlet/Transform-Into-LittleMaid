package net.sistr.transformintolittlemaid.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.sistr.transformintolittlemaid.layer.EventRegister;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(method = "handleInputEvents", at = @At("RETURN"))
    public void onHandleInputEvents(CallbackInfo ci) {
        EventRegister.keyTick();
    }

}
