package net.sistr.transformintolittlemaid.layer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public class EventRegister {
    private static final ObjectList<Runnable> keyTickerList = new ObjectArrayList<>();

    public static void registerKeyTick(Runnable runnable) {
        keyTickerList.add(runnable);
    }

    public static void keyTick() {
        keyTickerList.forEach(Runnable::run);
    }

}
