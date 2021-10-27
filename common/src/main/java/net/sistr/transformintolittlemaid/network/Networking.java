package net.sistr.transformintolittlemaid.network;

import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.platform.Platform;
import net.fabricmc.api.EnvType;

public class Networking {

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, TransformLittleMaidPacket.ID, TransformLittleMaidPacket::receiveC2SPacket);
        if (Platform.getEnv() == EnvType.CLIENT) {
            NetworkManager.registerReceiver(NetworkManager.Side.S2C, TransformLittleMaidPacket.ID, TransformLittleMaidPacket::receiveS2CPacket);
        }
    }

}
