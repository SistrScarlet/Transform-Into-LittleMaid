package net.sistr.transformintolittlemaid.network;

import dev.architectury.networking.NetworkManager;

public class Networking {

    public static void init() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                TransformLittleMaidPacket.ID,
                TransformLittleMaidPacket.CODEC,
                TransformLittleMaidPacket::receiveC2SPacket);
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                RequestSyncMultiModelPacket.ID,
                RequestSyncMultiModelPacket.CODEC,
                RequestSyncMultiModelPacket::receiveC2SPacket);
        NetworkManager.registerReceiver(
                NetworkManager.Side.C2S,
                ApplyMaidSizePacket.ID,
                ApplyMaidSizePacket.CODEC,
                ApplyMaidSizePacket::receiveC2SPacket);
    }
}
