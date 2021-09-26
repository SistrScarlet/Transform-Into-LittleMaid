package net.sistr.transformintolittlemaid.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.littlemaidmodelloader.resource.manager.LMTextureManager;
import net.sistr.transformintolittlemaid.util.AdditionalPlayerSpawnPacket;
import net.sistr.transformintolittlemaid.util.LittleMaidTransformable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    private ClientPlayerEntity oldPlayer_TLM;

    private static void readAdditionalPacket(PlayerEntity player, IHasMultiModel multiModel, AdditionalPlayerSpawnPacket additional) {
        multiModel.setColor(additional.getColor_TLM());
        multiModel.setContract(additional.isContract_TLM());
        LMTextureManager textureManager = LMTextureManager.INSTANCE;
        textureManager.getTexture(additional.getTextureName_TLM()).filter(textureHolder ->
                multiModel.isAllowChangeTexture(player, textureHolder, IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD))
                .ifPresent(textureHolder -> multiModel.setTextureHolder(textureHolder, IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD));
        for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
            String armorName = additional.getArmorTextureName_TLM().getArmor(part)
                    .orElseThrow(() -> new IllegalStateException("テクスチャが存在しません。"));
            textureManager.getTexture(armorName).filter(textureHolder ->
                    multiModel.isAllowChangeTexture(player, textureHolder, IHasMultiModel.Layer.INNER, part))
                    .ifPresent(textureHolder -> multiModel.setTextureHolder(textureHolder, IHasMultiModel.Layer.INNER, part));
        }
        ((LittleMaidTransformable) player).setTransformedLittleMaid_TLM(additional.isTransformedLittleMaid_TLM());
    }

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
        ((LittleMaidTransformable) player).setTransformedLittleMaid_TLM(((LittleMaidTransformable) oldPlayer_TLM).isTransformedLittleMaid_TLM());
        oldPlayer_TLM = null;
    }

    //マルチプレイ時の他プレイヤー
    @Inject(method = "onPlayerSpawn", at = @At("RETURN"))
    public void onHandleSpawnPlayer(PlayerSpawnS2CPacket packet, CallbackInfo ci) {
        assert MinecraftClient.getInstance().world != null;
        PlayerEntity player = MinecraftClient.getInstance().world.getPlayerByUuid(packet.getPlayerUuid());
        if (player == null) return;
        IHasMultiModel multiModel = (IHasMultiModel) player;
        AdditionalPlayerSpawnPacket additional = (AdditionalPlayerSpawnPacket) packet;
        readAdditionalPacket(player, multiModel, additional);
    }

}
