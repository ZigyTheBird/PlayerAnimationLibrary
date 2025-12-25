package com.zigythebird.playeranim;

@Deprecated(forRemoval = true)
public class PlayerAnimLibPlatform {
    @Deprecated(forRemoval = true)
    public static boolean isModLoaded(String id) {
        return PlayerAnimLibService.INSTANCE.isModLoaded(id);
    }
}
