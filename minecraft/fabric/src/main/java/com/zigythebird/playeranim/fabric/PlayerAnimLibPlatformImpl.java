package com.zigythebird.playeranim.fabric;

import net.fabricmc.loader.api.FabricLoader;

public class PlayerAnimLibPlatformImpl {
    public static boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }
}
