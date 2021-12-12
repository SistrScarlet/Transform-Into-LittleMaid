package net.sistr.transformintolittlemaid.client.layer;

import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.KeyBinding;

@Environment(EnvType.CLIENT)
public class KeyRegister {

    public static void register(KeyBinding key) {
        KeyMappingRegistry.register(key);
    }

}
