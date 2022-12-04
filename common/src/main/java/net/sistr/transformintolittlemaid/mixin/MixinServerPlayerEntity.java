package net.sistr.transformintolittlemaid.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.transformintolittlemaid.util.LittleMaidTransformable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends MixinPlayerEntity {
    protected MixinServerPlayerEntity(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Inject(method = "copyFrom", at = @At("RETURN"))
    private void onCopyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        IHasMultiModel multiModel = this;
        IHasMultiModel oldMultiModel = (IHasMultiModel) oldPlayer;
        multiModel.setColorMM(oldMultiModel.getColorMM());
        multiModel.setContractMM(oldMultiModel.isContractMM());
        multiModel.setTextureHolder(oldMultiModel.getTextureHolder(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD),
                IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD);
        for (IHasMultiModel.Part part : IHasMultiModel.Part.values()) {
            multiModel.setTextureHolder(oldMultiModel.getTextureHolder(IHasMultiModel.Layer.INNER, part),
                    IHasMultiModel.Layer.INNER, part);
        }
        ((LittleMaidTransformable) this).setTransformedLittleMaid_TLM(
                ((LittleMaidTransformable) oldPlayer).isTransformedLittleMaid_TLM());
    }
}
