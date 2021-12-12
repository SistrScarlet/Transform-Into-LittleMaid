package net.sistr.transformintolittlemaid.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.sistr.transformintolittlemaid.layer.NetworkUtil;
import net.sistr.transformintolittlemaid.util.LittleMaidTransformable;

import java.util.Optional;

import static net.sistr.transformintolittlemaid.TransformIntoLittleMaidMod.MODID;

public class TransformLittleMaidPacket {
    public static final Identifier ID = new Identifier(MODID, "transform_littlemaid");

    public static void sendS2CPacket(ServerPlayerEntity player, boolean changed) {
        PacketByteBuf buf = createS2CPacket(player, changed);
        NetworkManager.sendToPlayer(player, ID, buf);
        NetworkUtil.getTracker(player).forEach(spe -> NetworkManager.sendToPlayer(spe, ID, buf));
    }

    public static PacketByteBuf createS2CPacket(ServerPlayerEntity player, boolean changed) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(player.getId());
        buf.writeBoolean(changed);
        return buf;
    }

    public static void sendC2SPacket(boolean changed) {
        PacketByteBuf buf = createC2SPacket(changed);
        NetworkManager.sendToServer(ID, buf);
    }

    public static PacketByteBuf createC2SPacket(boolean changed) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBoolean(changed);
        return buf;
    }

    public static void receiveC2SPacket(PacketByteBuf buf, NetworkManager.PacketContext ctx) {
        boolean transformed = buf.readBoolean();
        ctx.queue(() -> transformC2S(ctx.getPlayer(), transformed));
    }

    public static void transformC2S(PlayerEntity player, boolean transformed) {
        ((LittleMaidTransformable) player).setTransformedLittleMaid_TLM(transformed);
        player.calculateDimensions();
    }

    @Environment(EnvType.CLIENT)
    public static void receiveS2CPacket(PacketByteBuf buf, NetworkManager.PacketContext ctx) {
        int entityId = buf.readVarInt();
        boolean transformed = buf.readBoolean();
        ctx.queue(() -> transformS2C(entityId, transformed));
    }

    @Environment(EnvType.CLIENT)
    public static void transformS2C(int entityId, boolean transformed) {
        getWorld().ifPresent(world -> {
            Entity entity = world.getEntityById(entityId);
            if (entity instanceof LittleMaidTransformable) {
                ((LittleMaidTransformable) entity).setTransformedLittleMaid_TLM(transformed);
            }
        });
    }

    @Environment(EnvType.CLIENT)
    public static Optional<ClientWorld> getWorld() {
        return Optional.ofNullable(MinecraftClient.getInstance().world);
    }


}
