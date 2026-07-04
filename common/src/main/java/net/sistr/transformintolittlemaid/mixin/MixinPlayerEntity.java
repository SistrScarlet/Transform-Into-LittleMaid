package net.sistr.transformintolittlemaid.mixin;

import com.mojang.authlib.GameProfile;
import java.util.Optional;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.littlemaidmodelloader.entity.compound.MultiModelCompound;
import net.sistr.littlemaidmodelloader.maidmodel.IModelCaps;
import net.sistr.littlemaidmodelloader.multimodel.IMultiModel;
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

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity
        implements IHasMultiModel, LittleMaidTransformable, WaitTime {
    private MultiModelCompound multiModel_TLM;
    private static final TrackedData<Boolean> TRANSFORMED_LITTLEMAID =
            DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int waitTime_TILM = 0;
    private boolean prevTransformedLittleMaid;
    private boolean syncMultiModel;

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(
            World world, BlockPos pos, float yaw, GameProfile gameProfile, CallbackInfo ci) {
        TextureHolder defaultTexture =
                LMTextureManager.INSTANCE
                        .getTexture("default")
                        .orElseThrow(() -> new IllegalStateException("デフォルトモデルが存在しません。"));
        this.multiModel_TLM = new MultiModelCompound(this, defaultTexture, defaultTexture);
    }

    @Inject(method = "initDataTracker", at = @At("RETURN"))
    private void onInitDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(TRANSFORMED_LITTLEMAID, false);
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
        if (getWorld().isClient && !syncMultiModel) {
            syncMultiModel = true;
            RequestSyncMultiModelPacket.sendC2SPacket((PlayerEntity) (Object) this);
        }
    }

    // 1.21 で getDimensions(EntityPose)/getActiveEyeHeight は Entity/LivingEntity 側の
    // final メソッドとなり、PlayerEntity 自身はオーバーライドを持たなくなったため
    // Mixin @Inject の対象にできない(remap時に "does not exists in any of the targets" 警告)。
    // 変身時のサイズ・視点変更は機能ロスとして受容する。TODO.md参照。

    // 1.21 で Entity#getMountedHeightOffset は廃止。
    // 乗客の乗車位置を返す getPassengerRidingPos で代替する(上になんか乗ってるやつのオフセット)
    @Override
    public Vec3d getPassengerRidingPos(Entity passenger) {
        if (!isTransformedLittleMaid_TLM()) {
            return super.getPassengerRidingPos(passenger);
        }
        IMultiModel model =
                getModel(Layer.SKIN, Part.HEAD).orElse(LMModelManager.INSTANCE.getDefaultModel());
        return getPos().add(0, model.getMountedYOffset(getCaps()), 0);
    }

    // 1.21 で Entity#getHeightOffset(騎乗時の自身のYオフセット)は廃止。代替APIなし。TODO.md参照。

    // 防具の更新
    @Inject(method = "equipStack", at = @At("HEAD"))
    public void onEquipStack(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        if (slot.isArmorSlot()) {
            multiModel_TLM.updateArmor();
        }
    }

    @Override
    public boolean isAllowChangeTexture(
            Entity entity, TextureHolder textureHolder, Layer layer, Part part) {
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
        return multiModel_TLM.isArmorGlint(part);
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
        if (MathHelper.approximatelyEquals(0, stepBobbingAmount)
                && !handSwinging
                && !isUsingItem()) {
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
