package net.sistr.transformintolittlemaid.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
import net.sistr.littlemaidmodelloader.LMMLMod;
import net.sistr.littlemaidmodelloader.client.renderer.MultiModelRenderer;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.littlemaidmodelloader.maidmodel.IModelCaps;
import net.sistr.littlemaidmodelloader.maidmodel.ModelMultiBase;
import net.sistr.littlemaidmodelloader.maidmodel.ModelRenderer;
import net.sistr.littlemaidmodelloader.multimodel.layer.MMMatrixStack;
import net.sistr.littlemaidmodelloader.multimodel.layer.MMVertexConsumer;
import net.sistr.transformintolittlemaid.util.WaitTime;

//MixinでPlayerEntityにIHasMultiModelをブチこんでるため、ジェネリクスはRawで使う
@Environment(EnvType.CLIENT)
public class LittleMaidPlayerRenderer extends PlayerEntityRenderer {
    private static final Identifier NULL_TEXTURE = new Identifier(LMMLMod.MODID, "null");
    private final LittleMaidMultiModel model;

    public LittleMaidPlayerRenderer(EntityRendererFactory.Context context) {
        super(context, false);
        this.model = new LittleMaidMultiModel(context);
    }

    @Override
    public void render(AbstractClientPlayerEntity abstractClientPlayerEntity, float yaw, float tickDelta,
                       MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        this.model.render(abstractClientPlayerEntity, yaw, tickDelta, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public void renderRightArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player) {
        ModelRenderer.setParam(new MMMatrixStack(matrices), new MMVertexConsumer(vertexConsumers.getBuffer(getModel().getLayer(getTexture(player)))),
                light, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F);
        IHasMultiModel hasMultiModel = ((IHasMultiModel) player);
        hasMultiModel.getModel(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD)
                .filter(m -> m instanceof ModelMultiBase)
                .map(model -> ((ModelMultiBase) model))
                .ifPresent(model -> {
                    model.animateModel(hasMultiModel.getCaps(), 0f, 0f, 0f);
                    model.setAngles(hasMultiModel.getCaps(), 0f, 0f, 0f, 0f, 0f);
                    model.renderFirstPersonHand(hasMultiModel.getCaps());
                });
    }

    @Override
    public void renderLeftArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player) {
        matrices.push();
        matrices.scale(-1F, 1F, 1F);
        ModelRenderer.setParam(new MMMatrixStack(matrices), new MMVertexConsumer(vertexConsumers.getBuffer(getModel().getLayer(getTexture(player)))),
                light, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F);
        IHasMultiModel hasMultiModel = ((IHasMultiModel) player);
        hasMultiModel.getModel(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD)
                .filter(m -> m instanceof ModelMultiBase)
                .ifPresent(model -> model.renderFirstPersonHand(hasMultiModel.getCaps()));
        matrices.pop();
    }


    @Override
    public Identifier getTexture(AbstractClientPlayerEntity abstractClientPlayerEntity) {
        return ((IHasMultiModel) abstractClientPlayerEntity).getTexture(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD, false)
                .orElse(NULL_TEXTURE);
    }

    private static class LittleMaidMultiModel extends MultiModelRenderer {

        public LittleMaidMultiModel(EntityRendererFactory.Context context) {
            super(context);
        }

        @Override
        public void syncCaps(LivingEntity entity, ModelMultiBase model, float partialTicks) {
            super.syncCaps(entity, model, partialTicks);
            if (!(entity instanceof PlayerEntity)) return;
            ItemStack activeStack = entity.getActiveItem();
            UseAction action = activeStack.getItem().getUseAction(activeStack);
            model.setCapsValue(IModelCaps.caps_aimedBow,
                    action == UseAction.BOW || action == UseAction.CROSSBOW);
            model.setCapsValue(IModelCaps.caps_isContract, ((IHasMultiModel) entity).isContractMM());
            model.setCapsValue(IModelCaps.caps_isWait, entity instanceof WaitTime && 60 < ((WaitTime) entity).getWaitTime_TILM());
        }
    }

}
