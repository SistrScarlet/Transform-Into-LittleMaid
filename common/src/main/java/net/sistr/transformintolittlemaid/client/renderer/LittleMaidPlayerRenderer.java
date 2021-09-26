package net.sistr.transformintolittlemaid.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
import net.sistr.littlemaidmodelloader.LittleMaidModelLoader;
import net.sistr.littlemaidmodelloader.client.renderer.MultiModelRenderer;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.littlemaidmodelloader.maidmodel.IModelCaps;
import net.sistr.littlemaidmodelloader.maidmodel.ModelMultiBase;
import net.sistr.littlemaidmodelloader.maidmodel.ModelRenderer;
import net.sistr.littlemaidmodelloader.multimodel.layer.MMMatrixStack;
import net.sistr.littlemaidmodelloader.multimodel.layer.MMVertexConsumer;

//MixinでPlayerEntityにIHasMultiModelをブチこんでるため、ジェネリクスはRawで使う
@Environment(EnvType.CLIENT)
public class LittleMaidPlayerRenderer extends PlayerEntityRenderer {
    private static final Identifier NULL_TEXTURE = new Identifier(LittleMaidModelLoader.MODID, "null");
    private final LittleMaidMultiModel model;

    public LittleMaidPlayerRenderer(EntityRenderDispatcher dispatcher) {
        super(dispatcher);
        this.model = new LittleMaidMultiModel(dispatcher);
    }

    @Override
    public void render(AbstractClientPlayerEntity abstractClientPlayerEntity, float f, float g,
                       MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        this.model.render(abstractClientPlayerEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public void renderRightArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player) {
        ModelRenderer.setParam(new MMMatrixStack(matrices), new MMVertexConsumer(vertexConsumers.getBuffer(getModel().getLayer(getTexture(player)))),
                light, OverlayTexture.DEFAULT_UV, 1F, 1F, 1F, 1F);
        IHasMultiModel hasMultiModel = ((IHasMultiModel) player);
        hasMultiModel.getModel(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD)
                .filter(m -> m instanceof ModelMultiBase)
                .ifPresent(model -> ((ModelMultiBase) model).renderFirstPersonHand(hasMultiModel.getCaps()));
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
                .ifPresent(model -> ((ModelMultiBase) model).renderFirstPersonHand(hasMultiModel.getCaps()));
        matrices.pop();
    }


    @Override
    public Identifier getTexture(AbstractClientPlayerEntity abstractClientPlayerEntity) {
        return ((IHasMultiModel) abstractClientPlayerEntity).getTexture(IHasMultiModel.Layer.SKIN, IHasMultiModel.Part.HEAD, false)
                .orElse(NULL_TEXTURE);
    }

    private static class LittleMaidMultiModel extends MultiModelRenderer {

        public LittleMaidMultiModel(EntityRenderDispatcher dispatcher) {
            super(dispatcher);
        }

        @Override
        public void syncCaps(LivingEntity entity, ModelMultiBase model, float partialTicks) {
            super.syncCaps(entity, model, partialTicks);
            if (!(entity instanceof PlayerEntity)) return;
            ItemStack activeStack = entity.getActiveItem();
            UseAction action = activeStack.getItem().getUseAction(activeStack);
            model.setCapsValue(IModelCaps.caps_aimedBow,
                    action == UseAction.BOW || action == UseAction.CROSSBOW);
            model.setCapsValue(IModelCaps.caps_isContract, ((IHasMultiModel) entity).isContract());
        }
    }

}
