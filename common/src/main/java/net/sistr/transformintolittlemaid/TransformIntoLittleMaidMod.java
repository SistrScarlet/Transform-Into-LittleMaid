package net.sistr.transformintolittlemaid;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.sistr.transformintolittlemaid.config.TILMConfig;

public class TransformIntoLittleMaidMod {

    public static final String MODID = "transformintolittlemaid";
    private static ConfigHolder<TILMConfig> configHolder;

    /** NeoForge ではコンストラクタから、Fabric では ModSetup から呼ばれるので二重登録を避ける */
    public static void initConfig() {
        if (configHolder != null) {
            return;
        }
        AutoConfig.register(TILMConfig.class, Toml4jConfigSerializer::new);
        configHolder = AutoConfig.getConfigHolder(TILMConfig.class);
    }

    public static TILMConfig getConfig() {
        return configHolder.getConfig();
    }

    public static ConfigHolder<TILMConfig> getConfigHolder() {
        return configHolder;
    }
}
