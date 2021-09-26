package net.sistr.transformintolittlemaid.client.layer;

import me.shedaniel.architectury.registry.KeyBindings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.options.KeyBinding;

@Environment(EnvType.CLIENT)
public class KeyRegister {

    public static void register(KeyBinding key) {
        KeyBindings.registerKeyBinding(key);
    }

}
