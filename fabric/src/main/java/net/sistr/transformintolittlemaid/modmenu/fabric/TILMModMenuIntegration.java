package net.sistr.transformintolittlemaid.modmenu.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.sistr.transformintolittlemaid.config.TILMConfig;

public class TILMModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(TILMConfig.class, parent).get();
    }
}
