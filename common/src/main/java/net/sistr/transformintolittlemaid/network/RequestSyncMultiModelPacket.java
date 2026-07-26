package net.sistr.transformintolittlemaid.network;

import static net.sistr.transformintolittlemaid.TransformIntoLittleMaidMod.MODID;

import dev.architectury.networking.NetworkManager;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.littlemaidmodelloader.network.SyncMultiModelData;
import net.sistr.littlemaidmodelloader.network.SyncMultiModelPacket;
import net.sistr.littlemaidmodelloader.network.SyncMultiModelS2CPacket;

public record RequestSyncMultiModelPacket(UUID playerUuid) implements CustomPayload {
    public static final CustomPayload.Id<RequestSyncMultiModelPacket> ID =
            new CustomPayload.Id<>(Identifier.of(MODID, "request_sync_multi_model"));

    public static final PacketCodec<RegistryByteBuf, RequestSyncMultiModelPacket> CODEC =
            PacketCodec.of(
                    (packet, buf) -> buf.writeUuid(packet.playerUuid()),
                    buf -> new RequestSyncMultiModelPacket(buf.readUuid()));

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    @Environment(EnvType.CLIENT)
    public static void sendC2SPacket(PlayerEntity player) {
        NetworkManager.sendToServer(new RequestSyncMultiModelPacket(player.getUuid()));
    }

    public static void receiveC2SPacket(
            RequestSyncMultiModelPacket payload, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> receiveC2S(ctx.getPlayer(), payload.playerUuid()));
    }

    public static void receiveC2S(PlayerEntity player, UUID uuid) {
        var target = player.getWorld().getPlayerByUuid(uuid);
        if (target == null) {
            return;
        }
        SyncMultiModelPacket.sendS2CPacket(target, (IHasMultiModel) target);
        // sendS2CPacket の宛先は target を tracking しているプレイヤーだが、Fabric の
        // PlayerLookup.tracking はプレイヤー自身を含まない (バニラの tracker が自分を listener に
        // 入れないため)。自分のモデルを要求した場合に返信が届かないので、要求元へ直接も送る。
        if (player == target && player instanceof ServerPlayerEntity requester) {
            NetworkManager.sendToPlayer(
                    requester,
                    new SyncMultiModelS2CPacket(
                            SyncMultiModelData.of(target, (IHasMultiModel) target)));
        }
    }
}
