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
import net.minecraft.util.Identifier;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.littlemaidmodelloader.network.SyncMultiModelPacket;

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
        if (target != null) {
            SyncMultiModelPacket.sendS2CPacket(target, (IHasMultiModel) target);
        }
    }
}
