package net.sistr.transformintolittlemaid.network;

import static net.sistr.transformintolittlemaid.TransformIntoLittleMaidMod.MODID;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.littlemaidmodelloader.network.SyncMultiModelPacket;

public class RequestSyncMultiModelPacket {
    public static final Identifier ID = new Identifier(MODID, "request_sync_multi_model");

    @Environment(EnvType.CLIENT)
    public static void sendC2SPacket(PlayerEntity player) {
        PacketByteBuf buf = createC2SPacket(player);
        NetworkManager.sendToServer(ID, buf);
    }

    @Environment(EnvType.CLIENT)
    public static PacketByteBuf createC2SPacket(PlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeUuid(player.getUuid());
        return buf;
    }

    public static void receiveC2SPacket(PacketByteBuf buf, NetworkManager.PacketContext ctx) {
        var uuid = buf.readUuid();
        ctx.queue(() -> receiveC2S(ctx.getPlayer(), uuid));
    }

    public static void receiveC2S(PlayerEntity player, UUID uuid) {
        var target = player.getWorld().getPlayerByUuid(uuid);
        if (target != null)
            NetworkManager.sendToPlayer(
                    (ServerPlayerEntity) player,
                    SyncMultiModelPacket.ID,
                    SyncMultiModelPacket.createS2CPacket(target, (IHasMultiModel) target));
    }
}
