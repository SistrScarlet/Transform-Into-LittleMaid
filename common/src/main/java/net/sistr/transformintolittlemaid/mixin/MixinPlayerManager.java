package net.sistr.transformintolittlemaid.mixin;

import me.shedaniel.architectury.networking.NetworkManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.littlemaidmodelloader.network.SyncMultiModelPacket;
import net.sistr.transformintolittlemaid.layer.NetworkUtil;
import net.sistr.transformintolittlemaid.network.TransformLittleMaidPacket;
import net.sistr.transformintolittlemaid.util.LittleMaidTransformable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {

    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    public void onInitializeConnectionToPlayer(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        PacketByteBuf buf = SyncMultiModelPacket.createS2CPacket(player, (IHasMultiModel) player);
        NetworkManager.sendToPlayer(player, SyncMultiModelPacket.ID, buf);
        NetworkUtil.getTracker(player).forEach(spe ->
                NetworkManager.sendToPlayer(spe, SyncMultiModelPacket.ID, buf));
        TransformLittleMaidPacket.sendS2CPacket(player, ((LittleMaidTransformable) player).isTransformedLittleMaid_TLM());
    }

}
