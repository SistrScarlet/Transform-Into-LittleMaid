package net.sistr.transformintolittlemaid.util;

import net.minecraft.entity.player.PlayerEntity;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel.Layer;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel.Part;

public class MultiModelCopyUtil {

    public static void copyMultiModelState(PlayerEntity from, PlayerEntity to) {
        IHasMultiModel oldModel = (IHasMultiModel) from;
        IHasMultiModel newModel = (IHasMultiModel) to;
        newModel.setColorMM(oldModel.getColorMM());
        newModel.setContractMM(oldModel.isContractMM());
        newModel.setTextureHolder(
                oldModel.getTextureHolder(Layer.SKIN, Part.HEAD), Layer.SKIN, Part.HEAD);
        for (Part part : Part.values()) {
            newModel.setTextureHolder(
                    oldModel.getTextureHolder(Layer.INNER, part), Layer.INNER, part);
        }
        ((LittleMaidTransformable) to)
                .setTransformedLittleMaid_TLM(
                        ((LittleMaidTransformable) from).isTransformedLittleMaid_TLM());
    }
}
