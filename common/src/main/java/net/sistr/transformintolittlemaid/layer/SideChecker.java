package net.sistr.transformintolittlemaid.layer;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class SideChecker {

    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

}
