package net.sistr.transformintolittlemaid.layer;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.stream.Stream;

public class NetworkUtil {

    //めんどくさいのでプレイヤー全員
    public static Stream<ServerPlayerEntity> getTracker(Entity entity) {
        return entity.world.getPlayers().stream().map(p -> (ServerPlayerEntity) p);
    }

}
