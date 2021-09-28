package net.sistr.transformintolittlemaid.layer.forge;

import net.minecraftforge.fml.loading.FMLEnvironment;

public class SideCheckerImpl {

    public static boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }

}
