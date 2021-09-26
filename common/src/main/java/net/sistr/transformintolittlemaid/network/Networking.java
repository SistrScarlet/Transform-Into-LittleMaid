package net.sistr.transformintolittlemaid.network;

import me.shedaniel.architectury.networking.NetworkManager;
import net.sistr.transformintolittlemaid.layer.SideChecker;

public class Networking {

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, TransformLittleMaidPacket.ID, TransformLittleMaidPacket::receiveC2SPacket);
        if (!SideChecker.isClient()) {
            return;
        }
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, TransformLittleMaidPacket.ID, TransformLittleMaidPacket::receiveS2CPacket);
    }

}
