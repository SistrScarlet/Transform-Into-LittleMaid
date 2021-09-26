package net.sistr.transformintolittlemaid.util;

import net.sistr.littlemaidmodelloader.resource.util.ArmorSets;
import net.sistr.littlemaidmodelloader.resource.util.TextureColors;

public interface AdditionalPlayerSpawnPacket {

    String getTextureName_TLM();

    ArmorSets<String> getArmorTextureName_TLM();

    TextureColors getColor_TLM();

    boolean isContract_TLM();

    boolean isTransformedLittleMaid_TLM();

}
