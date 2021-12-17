package net.sistr.transformintolittlemaid.util;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.sistr.littlemaidmodelloader.maidmodel.EntityCaps;

import java.util.HashMap;
import java.util.Map;

public class PlayerEntityCaps extends EntityCaps {
    private static final Map<String, Integer> caps = new HashMap<>();
    private static final Int2ObjectOpenHashMap<Getter> capGetter = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectOpenHashMap<EntityCaps.Setter> capSetter = new Int2ObjectOpenHashMap<>();
    private final PlayerEntity player;

    public PlayerEntityCaps(PlayerEntity pOwner) {
        super(pOwner);
        this.player = pOwner;
    }

    private static void register(String name, int index, EntityCaps.Getter getter) {
        register(name, index, getter, EMPTY_SETTER);
    }

    private static void register(String name, int index, EntityCaps.Getter getter, EntityCaps.Setter setter) {
        caps.putIfAbsent(name, index);
        capGetter.put(index, getter);
        capSetter.put(index, setter);
    }

    @Override
    public Map<String, Integer> getModelCaps() {
        return ImmutableMap.<String, Integer>builder().putAll(super.getModelCaps()).putAll(caps).build();
    }

    @Override
    public Object getCapsValue(int index, Object... arg) {
        if (capGetter.containsKey(index)) {
            return capGetter.get(index).get(this.player, arg);
        }
        return super.getCapsValue(index, arg);
    }

    static {

    }

}
