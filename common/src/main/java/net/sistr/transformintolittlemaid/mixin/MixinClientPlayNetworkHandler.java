package net.sistr.transformintolittlemaid.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.sistr.transformintolittlemaid.util.MultiModelCopyUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    private ClientPlayerEntity oldPlayer_TLM;

    // リスポ時にコピー、やや無理やり
    @Inject(method = "onPlayerRespawn", at = @At("HEAD"))
    public void preHandleRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
        oldPlayer_TLM = MinecraftClient.getInstance().player;
    }

    @Inject(method = "onPlayerRespawn", at = @At("RETURN"))
    public void postHandleRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || oldPlayer_TLM == null) return;
        MultiModelCopyUtil.copyMultiModelState(oldPlayer_TLM, player);
        oldPlayer_TLM = null;
    }
}
