package net.sistr.transformintolittlemaid.fabric;

import net.fabricmc.api.ModInitializer;
import net.sistr.transformintolittlemaid.setup.ModSetup;

public class TransformIntoLittleMaidModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModSetup.init();
    }
}
