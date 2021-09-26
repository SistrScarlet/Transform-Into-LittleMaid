package net.sistr.transformintolittlemaid.setup;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.sistr.transformintolittlemaid.client.layer.KeyRegister;
import net.sistr.transformintolittlemaid.client.manager.KeyManager;
import net.sistr.transformintolittlemaid.layer.EventRegister;

@Environment(EnvType.CLIENT)
public class ClientSetup {

    public static void init() {
        //キーの登録
        KeyRegister.register(KeyManager.TRANSFORM);
        KeyRegister.register(KeyManager.SELECT_MODEL);

        EventRegister.registerKeyTick(KeyManager::keyTick);

    }

}
