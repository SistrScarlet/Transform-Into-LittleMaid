package net.sistr.transformintolittlemaid.setup;

import net.sistr.transformintolittlemaid.TransformIntoLittleMaidMod;
import net.sistr.transformintolittlemaid.network.Networking;

public class ModSetup {

    public static void init() {
        TransformIntoLittleMaidMod.initConfig();
        Networking.init();
    }
}
