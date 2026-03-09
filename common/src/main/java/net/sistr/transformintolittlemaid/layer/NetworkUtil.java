package net.sistr.transformintolittlemaid.layer;

import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public class NetworkUtil {

    // めんどくさいのでプレイヤー全員
    public static Stream<ServerPlayerEntity> getTracker(Entity entity) {
        return entity.getWorld().getPlayers().stream().map(p -> (ServerPlayerEntity) p);
    }
}
