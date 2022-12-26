package net.sistr.transformintolittlemaid.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
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
import net.sistr.transformintolittlemaid.network.RequestSyncMultiModelPacket;
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
    private static final TrackedData<Boolean> TRANSFORMED_LITTLEMAID
            = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int waitTime_TILM = 0;
    private boolean prevTransformedLittleMaid;
    private boolean syncMultiModel;

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(World world, BlockPos pos, float yaw, GameProfile gameProfile, CallbackInfo ci) {
        this.multiModel_TLM = new MultiModelCompound(this,
                LMTextureManager.INSTANCE.getTexture("default")
                        .orElseThrow(() -> new IllegalStateException("デフォルトモデルが存在しません。")),
                LMTextureManager.INSTANCE.getTexture("default")
                        .orElseThrow(() -> new IllegalStateException("デフォルトモデルが存在しません。")));
    }

    @Inject(method = "initDataTracker", at = @At("RETURN"))
    private void onInitDataTracker(CallbackInfo ci) {
        this.dataTracker.startTracking(TRANSFORMED_LITTLEMAID, false);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    public void onWriteCustomDataToTag(NbtCompound tag, CallbackInfo ci) {
        multiModel_TLM.writeToNbt(tag);

        tag.putBoolean("IsChanged_TLM", isTransformedLittleMaid_TLM());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void onReadCustomDataFromTag(NbtCompound tag, CallbackInfo ci) {
        multiModel_TLM.readFromNbt(tag);

        setTransformedLittleMaid_TLM(tag.getBoolean("IsChanged_TLM"));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        var bool = isTransformedLittleMaid_TLM();
        if (prevTransformedLittleMaid != bool) {
            prevTransformedLittleMaid = bool;
            calculateDimensions();
        }
        if (world.isClient && !syncMultiModel) {
            syncMultiModel = true;
            RequestSyncMultiModelPacket.sendC2SPacket((PlayerEntity) (Object) this);
        }
    }

    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    public void onGetDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (isTransformedLittleMaid_TLM()) {
            getModel(Layer.SKIN, Part.HEAD).ifPresent(model -> {
                IModelCaps caps = this.getCaps();
                cir.setReturnValue(EntityDimensions.changing(
                        model.getWidth(caps, MMPose.convertPose(pose)), model.getHeight(caps, MMPose.convertPose(pose))));
            });
        }
    }

    @Inject(method = "getActiveEyeHeight", at = @At("HEAD"), cancellable = true)
    public void onGetActiveEyeHeight(EntityPose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
        if (isTransformedLittleMaid_TLM()) {
            getModel(Layer.SKIN, Part.HEAD)
                    .ifPresent(model -> cir.setReturnValue(model.getEyeHeight(this.getCaps(), MMPose.convertPose(pose))));
        }
    }

    //上になんか乗ってるやつのオフセット
    @Override
    public double getMountedHeightOffset() {
        if (!isTransformedLittleMaid_TLM()) {
            return super.getMountedHeightOffset();
        }
        IMultiModel model = getModel(Layer.SKIN, Part.HEAD)
                .orElse(LMModelManager.INSTANCE.getDefaultModel());
        return model.getMountedYOffset(getCaps());
    }

    //騎乗時のオフセット
    @Inject(method = "getHeightOffset", at = @At("HEAD"), cancellable = true)
    public void getHeightOffset(CallbackInfoReturnable<Double> cir) {
        if (isTransformedLittleMaid_TLM()) {
            IMultiModel model = getModel(Layer.SKIN, Part.HEAD)
                    .orElse(LMModelManager.INSTANCE.getDefaultModel());
            cir.setReturnValue((double) (model.getyOffset(getCaps()) - getHeight()));
        }
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
        calculateDimensions();
    }

    @Override
    public TextureHolder getTextureHolder(Layer layer, Part part) {
        return multiModel_TLM.getTextureHolder(layer, part);
    }

    @Override
    public TextureColors getColorMM() {
        return multiModel_TLM.getColorMM();
    }

    @Override
    public void setColorMM(TextureColors textureColors) {
        multiModel_TLM.setColorMM(textureColors);
    }

    @Override
    public boolean isContractMM() {
        return multiModel_TLM.isContractMM();
    }

    @Override
    public void setContractMM(boolean b) {
        multiModel_TLM.setContractMM(b);
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
        return this.dataTracker.get(TRANSFORMED_LITTLEMAID);
    }

    @Override
    public void setTransformedLittleMaid_TLM(boolean isChanged) {
        this.dataTracker.set(TRANSFORMED_LITTLEMAID, isChanged);
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    public void onTickMovement(CallbackInfo ci) {
        if (MathHelper.approximatelyEquals(0, stepBobbingAmount) && !handSwinging && !isUsingItem()) {
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
