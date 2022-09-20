package net.sistr.transformintolittlemaid.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.sistr.transformintolittlemaid.util.LittleMaidTransformable;

import static net.sistr.transformintolittlemaid.TransformIntoLittleMaidMod.MODID;

public class TransformLittleMaidPacket {
    public static final Identifier ID = new Identifier(MODID, "transform_littlemaid");

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

}
