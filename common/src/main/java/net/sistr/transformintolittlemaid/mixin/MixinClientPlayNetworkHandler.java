package net.sistr.transformintolittlemaid.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.transformintolittlemaid.util.LittleMaidTransformable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    private ClientPlayerEntity oldPlayer_TLM;

    //リスポ時にコピー、やや無理やり
    @Inject(method = "onPlayerRespawn", at = @At("HEAD"))
    public void preHandleRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
        oldPlayer_TLM = MinecraftClient.getInstance().player;
    }

    @Inject(method = "onPlayerRespawn", at = @At("RETURN"))
    public void postHandleRespawn(PlayerRespawnS2CPacket packet, CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || oldPlayer_TLM == null) return;
        IHasMultiModel multiModel = (IHasMultiModel) player;
        IHasMultiModel oldMultiModel = (IHasMultiModel) oldPlayer_TLM;
        multiModel.setColor(oldMultiModel.getColor());
        multiModel.setContract(oldMultiModel.isContract());
        multiModel.setTextureHolder(oldMultiModel.getTextureHolder(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD),
                IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD);
        for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
            multiModel.setTextureHolder(oldMultiModel.getTextureHolder(IHasMultiModel.Layer.INNER, part),
                    IHasMultiModel.Layer.INNER, part);
        }
        ((LittleMaidTransformable) player).setTransformedLittleMaid_TLM(
                ((LittleMaidTransformable) oldPlayer_TLM).isTransformedLittleMaid_TLM());
        oldPlayer_TLM = null;
    }

}
