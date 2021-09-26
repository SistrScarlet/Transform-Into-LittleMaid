package net.sistr.transformintolittlemaid.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.sistr.transformintolittlemaid.setup.ClientSetup;

public class TransformIntoLittleMaidModClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientSetup.init();
    }
}
