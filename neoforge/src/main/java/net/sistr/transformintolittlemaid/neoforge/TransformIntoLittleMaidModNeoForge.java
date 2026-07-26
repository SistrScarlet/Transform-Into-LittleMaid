package net.sistr.transformintolittlemaid.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.sistr.transformintolittlemaid.TransformIntoLittleMaidMod;
import net.sistr.transformintolittlemaid.neoforge.client.ClientConfigScreenSetup;
import net.sistr.transformintolittlemaid.setup.ClientSetup;
import net.sistr.transformintolittlemaid.setup.ModSetup;

@Mod(TransformIntoLittleMaidMod.MODID)
public class TransformIntoLittleMaidModNeoForge {

    public TransformIntoLittleMaidModNeoForge(IEventBus modBus, ModContainer container) {
        // コンフィグ画面の登録はコンストラクタでしか受け付けられない
        TransformIntoLittleMaidMod.initConfig();

        if (FMLEnvironment.dist.isClient()) {
            ClientConfigScreenSetup.register(container);
        }

        modBus.addListener(this::commonInit);
        modBus.addListener(this::clientInit);
    }

    public void commonInit(FMLCommonSetupEvent event) {
        ModSetup.init();
    }

    public void clientInit(FMLClientSetupEvent event) {
        ClientSetup.init();
    }
}
