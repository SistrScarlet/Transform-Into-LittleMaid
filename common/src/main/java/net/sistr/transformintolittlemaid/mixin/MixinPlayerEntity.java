package net.sistr.transformintolittlemaid.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.littlemaidmodelloader.entity.compound.MultiModelCompound;
import net.sistr.littlemaidmodelloader.maidmodel.IModelCaps;
import net.sistr.littlemaidmodelloader.multimodel.IMultiModel;
import net.sistr.littlemaidmodelloader.multimodel.layer.MMPose;
import net.sistr.littlemaidmodelloader.resource.holder.TextureHolder;
import net.sistr.littlemaidmodelloader.resource.manager.LMModelManager;
import net.sistr.littlemaidmodelloader.resource.manager.LMTextureManager;
import net.sistr.littlemaidmodelloader.resource.util.TextureColors;
import net.sistr.transformintolittlemaid.util.LittleMaidTransformable;
import net.sistr.transformintolittlemaid.util.WaitTime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity implements IHasMultiModel, LittleMaidTransformable, WaitTime {
    private MultiModelCompound multiModel_TLM;
    private boolean isTransformedLittleMaid_TLM;
    private int waitTime_TILM = 0;

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(World world, BlockPos pos, float yaw, GameProfile profile, CallbackInfo ci) {
        this.multiModel_TLM = new MultiModelCompound(this,
                LMTextureManager.INSTANCE.getTexture("default")
                        .orElseThrow(() -> new IllegalStateException("デフォルトモデルが存在しません。")),
                LMTextureManager.INSTANCE.getTexture("default")
                        .orElseThrow(() -> new IllegalStateException("デフォルトモデルが存在しません。")));
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    public void onWriteCustomDataToTag(NbtCompound tag, CallbackInfo ci) {
        tag.putByte("SkinColor", (byte) getColor().getIndex());
        tag.putBoolean("IsContract", isContract());
        tag.putString("SkinTexture", getTextureHolder(Layer.SKIN, Part.HEAD).getTextureName());
        for (Part part : Part.values()) {
            tag.putString("ArmorTextureInner" + part.getPartName(),
                    getTextureHolder(Layer.INNER, part).getTextureName());
            tag.putString("ArmorTextureOuter" + part.getPartName(),
                    getTextureHolder(Layer.OUTER, part).getTextureName());
        }

        tag.putBoolean("IsChanged_TLM", isTransformedLittleMaid_TLM);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void onReadCustomDataFromTag(NbtCompound tag, CallbackInfo ci) {
        if (tag.contains("SkinColor")) {
            setColor(TextureColors.getColor(tag.getByte("SkinColor")));
        }
        setContract(tag.getBoolean("IsContract"));
        LMTextureManager textureManager = LMTextureManager.INSTANCE;
        if (tag.contains("SkinTexture")) {
            textureManager.getTexture(tag.getString("SkinTexture"))
                    .ifPresent(textureHolder -> setTextureHolder(textureHolder, Layer.SKIN, Part.HEAD));
        }
        for (Part part : Part.values()) {
            String inner = "ArmorTextureInner" + part.getPartName();
            String outer = "ArmorTextureOuter" + part.getPartName();
            if (tag.contains(inner)) {
                textureManager.getTexture(tag.getString(inner))
                        .ifPresent(textureHolder -> setTextureHolder(textureHolder, Layer.INNER, part));
            }
            if (tag.contains(outer)) {
                textureManager.getTexture(tag.getString(outer))
                        .ifPresent(textureHolder -> setTextureHolder(textureHolder, Layer.OUTER, part));
            }
        }

        isTransformedLittleMaid_TLM = tag.getBoolean("IsChanged_TLM");
        calculateDimensions();
    }

    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    public void onGetDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (this.isTransformedLittleMaid_TLM) {
            getModel(Layer.SKIN, Part.HEAD).ifPresent(model -> {
                IModelCaps caps = this.getCaps();
                cir.setReturnValue(EntityDimensions.changing(
                        model.getWidth(caps, MMPose.convertPose(pose)), model.getHeight(caps, MMPose.convertPose(pose))));
            });
        }
    }

    @Inject(method = "getActiveEyeHeight", at = @At("HEAD"), cancellable = true)
    public void onGetActiveEyeHeight(EntityPose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
        if (this.isTransformedLittleMaid_TLM) {
            getModel(Layer.SKIN, Part.HEAD)
                    .ifPresent(model -> cir.setReturnValue(model.getEyeHeight(this.getCaps(), MMPose.convertPose(pose))));
        }
    }

    //上になんか乗ってるやつのオフセット
    @Override
    public double getMountedHeightOffset() {
        IMultiModel model = getModel(Layer.SKIN, Part.HEAD)
                .orElse(LMModelManager.INSTANCE.getDefaultModel());
        return model.getMountedYOffset(getCaps());
    }

    //騎乗時のオフセット
    @Inject(method = "getHeightOffset", at = @At("HEAD"), cancellable = true)
    public void getHeightOffset(CallbackInfoReturnable<Double> cir) {
        IMultiModel model = getModel(Layer.SKIN, Part.HEAD)
                .orElse(LMModelManager.INSTANCE.getDefaultModel());
        cir.setReturnValue((double) (model.getyOffset(getCaps()) - getHeight()));
    }

    //防具の更新
    @Inject(method = "equipStack", at = @At("HEAD"))
    public void onEquipStack(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        if (slot.getType() == EquipmentSlot.Type.ARMOR) {
            multiModel_TLM.updateArmor();
        }
    }

    @Override
    public boolean isAllowChangeTexture(Entity entity, TextureHolder textureHolder, Layer layer, Part part) {
        return multiModel_TLM.isAllowChangeTexture(entity, textureHolder, layer, part);
    }

    @Override
    public void setTextureHolder(TextureHolder textureHolder, Layer layer, Part part) {
        multiModel_TLM.setTextureHolder(textureHolder, layer, part);
    }

    @Override
    public TextureHolder getTextureHolder(Layer layer, Part part) {
        return multiModel_TLM.getTextureHolder(layer, part);
    }

    @Override
    public TextureColors getColor() {
        return multiModel_TLM.getColor();
    }

    @Override
    public void setColor(TextureColors textureColors) {
        multiModel_TLM.setColor(textureColors);
    }

    @Override
    public boolean isContract() {
        return multiModel_TLM.isContract();
    }

    @Override
    public void setContract(boolean b) {
        multiModel_TLM.setContract(b);
    }

    @Override
    public Optional<IMultiModel> getModel(Layer layer, Part part) {
        return multiModel_TLM.getModel(layer, part);
    }

    @Override
    public Optional<Identifier> getTexture(Layer layer, Part part, boolean b) {
        return multiModel_TLM.getTexture(layer, part, b);
    }

    @Override
    public IModelCaps getCaps() {
        return multiModel_TLM.getCaps();
    }

    @Override
    public boolean isArmorVisible(Part part) {
        return multiModel_TLM.isArmorVisible(part);
    }

    @Override
    public boolean isArmorGlint(Part part) {
        return multiModel_TLM.isArmorVisible(part);
    }

    @Override
    public boolean isTransformedLittleMaid_TLM() {
        return isTransformedLittleMaid_TLM;
    }

    @Override
    public void setTransformedLittleMaid_TLM(boolean isChanged) {
        isTransformedLittleMaid_TLM = isChanged;
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void onTickMovement(CallbackInfo ci) {
        if (MathHelper.approximatelyEquals(0, stepBobbingAmount)) {
            waitTime_TILM++;
        } else {
            waitTime_TILM = 0;
        }
    }

    @Override
    public int getWaitTime_TILM() {
        return waitTime_TILM;
    }
}
