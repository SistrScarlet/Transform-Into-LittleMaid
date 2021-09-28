package net.sistr.transformintolittlemaid.layer;

import me.shedaniel.architectury.annotations.ExpectPlatform;

public class SideChecker {

    @ExpectPlatform
    public static boolean isClient() {
        throw new AssertionError();
    }

}
