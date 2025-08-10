package com.zigythebird.playeranim;

import dev.architectury.injectables.annotations.ExpectPlatform;

public final class PlayerAnimLibPlatform {
    @ExpectPlatform
    public static boolean isModLoaded(String id) {
        throw new AssertionError();
    }
}
