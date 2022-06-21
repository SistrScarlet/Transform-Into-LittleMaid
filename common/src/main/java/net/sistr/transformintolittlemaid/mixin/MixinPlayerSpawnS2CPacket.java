package net.sistr.transformintolittlemaid.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlayerSpawnS2CPacket;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.littlemaidmodelloader.resource.util.ArmorSets;
import net.sistr.littlemaidmodelloader.resource.util.TextureColors;
import net.sistr.transformintolittlemaid.util.AdditionalPlayerSpawnPacket;
import net.sistr.transformintolittlemaid.util.LittleMaidTransformable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerSpawnS2CPacket.class)
public class MixinPlayerSpawnS2CPacket implements AdditionalPlayerSpawnPacket {
    private final ArmorSets<String> armorTextureName_TLM = new ArmorSets<>();
    private String textureName_TLM;
    private TextureColors color_TLM;
    private boolean isContract_TLM;
    private boolean isTransformedLittleMaid_TLM;

    @Inject(method = "<init>(Lnet/minecraft/entity/player/PlayerEntity;)V", at = @At("RETURN"))
    public void onInit(PlayerEntity player, CallbackInfo ci) {
        IHasMultiModel hasMultiModel = (IHasMultiModel) player;
        textureName_TLM = hasMultiModel.getTextureHolder(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD)
                .getTextureName();
        for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
            armorTextureName_TLM.setArmor(hasMultiModel.getTextureHolder(IHasMultiModel.Layer.INNER, part)
                    .getTextureName(), part);
        }
        color_TLM = hasMultiModel.getColor();
        isContract_TLM = hasMultiModel.isContract();
        isTransformedLittleMaid_TLM = ((LittleMaidTransformable) player).isTransformedLittleMaid_TLM();
    }

    @Inject(method = "read", at = @At("RETURN"))
    public void onReadPacketData(PacketByteBuf buf, CallbackInfo ci) {
        textureName_TLM = buf.readString(32767);
        for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
            armorTextureName_TLM.setArmor(buf.readString(32767), part);
        }
        color_TLM = buf.readEnumConstant(TextureColors.class);
        isContract_TLM = buf.readBoolean();
        isTransformedLittleMaid_TLM = buf.readBoolean();
    }

    @Inject(method = "write", at = @At("RETURN"))
    public void onWritePacketData(PacketByteBuf buf, CallbackInfo ci) {
        buf.writeString(textureName_TLM);
        for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
            buf.writeString(armorTextureName_TLM.getArmor(part).orElseThrow(IllegalArgumentException::new));
        }
        buf.writeEnumConstant(color_TLM);
        buf.writeBoolean(isContract_TLM);
        buf.writeBoolean(isTransformedLittleMaid_TLM);
    }

    @Environment(EnvType.CLIENT)
    @Override
    public String getTextureName_TLM() {
        return textureName_TLM;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public ArmorSets<String> getArmorTextureName_TLM() {
        return armorTextureName_TLM;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public TextureColors getColor_TLM() {
        return color_TLM;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean isContract_TLM() {
        return isContract_TLM;
    }

    @Override
    public boolean isTransformedLittleMaid_TLM() {
        return isTransformedLittleMaid_TLM;
    }
}
