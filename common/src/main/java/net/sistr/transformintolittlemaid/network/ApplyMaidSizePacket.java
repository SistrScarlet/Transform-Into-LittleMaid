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

/**
 * サイズ適用設定をクライアントからサーバーへ送る。
 *
 * <p>当たり判定に関わる設定をクライアント単独で判断すると、サーバーの当たり判定とずれる。設定はクライアント側の 好みなので、値の出所はクライアント、権威はサーバー、という形にしている。
 */
public record ApplyMaidSizePacket(boolean applyMaidSize) implements CustomPayload {
    public static final CustomPayload.Id<ApplyMaidSizePacket> ID =
            new CustomPayload.Id<>(Identifier.of(MODID, "apply_maid_size"));

    public static final PacketCodec<RegistryByteBuf, ApplyMaidSizePacket> CODEC =
            PacketCodec.of(
                    (packet, buf) -> buf.writeBoolean(packet.applyMaidSize()),
                    buf -> new ApplyMaidSizePacket(buf.readBoolean()));

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    @Environment(EnvType.CLIENT)
    public static void sendC2SPacket(boolean applyMaidSize) {
        NetworkManager.sendToServer(new ApplyMaidSizePacket(applyMaidSize));
    }

    public static void receiveC2SPacket(
            ApplyMaidSizePacket payload, NetworkManager.PacketContext ctx) {
        ctx.queue(() -> applyC2S(ctx.getPlayer(), payload.applyMaidSize()));
    }

    public static void applyC2S(PlayerEntity player, boolean applyMaidSize) {
        ((LittleMaidTransformable) player).setApplyMaidSize_TLM(applyMaidSize);
        player.calculateDimensions();
    }
}
