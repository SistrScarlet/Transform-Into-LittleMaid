package net.sistr.transformintolittlemaid.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.sistr.transformintolittlemaid.setup.ClientSetup;
import net.sistr.transformintolittlemaid.setup.ModSetup;

public class TransformIntoLittleMaidModFabric implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize() {
        ModSetup.init();
    }

    @Override
    public void onInitializeClient() {
        ClientSetup.init();
    }
}
