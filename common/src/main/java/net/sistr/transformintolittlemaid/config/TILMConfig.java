package net.sistr.transformintolittlemaid.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.sistr.transformintolittlemaid.TransformIntoLittleMaidMod;

/** TILM のコンフィグ */
@Config(name = TransformIntoLittleMaidMod.MODID)
public class TILMConfig implements ConfigData {

    @ConfigEntry.Category("transform")
    @ConfigEntry.Gui.Tooltip
    public boolean applyMaidSize = true;

    public boolean isApplyMaidSize() {
        return applyMaidSize;
    }
}
