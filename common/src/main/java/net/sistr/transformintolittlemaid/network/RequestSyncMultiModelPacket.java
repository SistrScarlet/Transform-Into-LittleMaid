package net.sistr.transformintolittlemaid.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.sistr.littlemaidmodelloader.entity.compound.IHasMultiModel;
import net.sistr.littlemaidmodelloader.network.SyncMultiModelPacket;

import java.util.UUID;

import static net.sistr.transformintolittlemaid.TransformIntoLittleMaidMod.MODID;

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
        var other = player.world.getPlayerByUuid(uuid);
        if (other != null)
            NetworkManager.sendToPlayer((ServerPlayerEntity) other,
                    SyncMultiModelPacket.ID,
                    SyncMultiModelPacket.createS2CPacket(other, (IHasMultiModel) other));
    }
}
