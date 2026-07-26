package net.sistr.transformintolittlemaid.setup;

import dev.architectury.event.events.client.ClientPlayerEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import net.sistr.transformintolittlemaid.TransformIntoLittleMaidMod;
import net.sistr.transformintolittlemaid.client.layer.KeyRegister;
import net.sistr.transformintolittlemaid.client.manager.KeyManager;
import net.sistr.transformintolittlemaid.layer.EventRegister;
import net.sistr.transformintolittlemaid.network.ApplyMaidSizePacket;

@Environment(EnvType.CLIENT)
public class ClientSetup {

    public static void init() {
        // キーの登録
        KeyRegister.register(KeyManager.TRANSFORM);
        KeyRegister.register(KeyManager.SELECT_MODEL);

        EventRegister.registerKeyTick(KeyManager::keyTick);

        // サイズ適用はクライアント側の設定だが、当たり判定に関わるためサーバーへ送って一致させる
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(player -> sendApplyMaidSize());
        ClientPlayerEvent.CLIENT_PLAYER_RESPAWN.register(
                (oldPlayer, newPlayer) -> sendApplyMaidSize());
        TransformIntoLittleMaidMod.getConfigHolder()
                .registerSaveListener(
                        (holder, config) -> {
                            sendApplyMaidSize();
                            return ActionResult.PASS;
                        });
    }

    private static void sendApplyMaidSize() {
        // ワールドに入る前に設定画面を開いた場合は送る相手がいない
        if (MinecraftClient.getInstance().getNetworkHandler() == null) {
            return;
        }
        ApplyMaidSizePacket.sendC2SPacket(TransformIntoLittleMaidMod.getConfig().isApplyMaidSize());
    }
}
