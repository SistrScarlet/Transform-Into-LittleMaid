package net.sistr.transformintolittlemaid.network;

import static net.sistr.transformintolittlemaid.TransformIntoLittleMaidMod.MODID;

import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.sistr.transformintolittlemaid.util.LittleMaidTransformable;

public record TransformLittleMaidPacket(boolean changed) implements CustomPayload {
    public static final CustomPayload.Id<TransformLittleMaidPacket> ID =
            new CustomPayload.Id<>(Identifier.of(MODID, "transform_littlemaid"));

    public static final PacketCodec<RegistryByteBuf, TransformLittleMaidPacket> CODEC =
            PacketCodec.of(
                    (packet, buf) -> buf.writeBoolean(packet.changed()),
                    buf -> new TransformLittleMaidPacket(buf.readBoolean()));

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    @Environment(EnvType.CLIENT)
    public static void sendC2SPacket(boolean changed) {
        NetworkManager.sendToServer(new TransformLittleMaidPacket(changed));
    }

    public static void receiveC2SPacket(
            TransformLittleMaidPacket payload, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> transformC2S(ctx.getPlayer(), payload.changed()));
    }

    public static void transformC2S(PlayerEntity player, boolean transformed) {
        ((LittleMaidTransformable) player).setTransformedLittleMaid_TLM(transformed);
        player.calculateDimensions();
    }
}
