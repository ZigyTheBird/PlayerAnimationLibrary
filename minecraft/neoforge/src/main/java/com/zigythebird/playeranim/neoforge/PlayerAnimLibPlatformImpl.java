package com.zigythebird.playeranim.neoforge;

import net.neoforged.fml.loading.FMLLoader;

public class PlayerAnimLibPlatformImpl {
    public static boolean isModLoaded(String id) {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById(id) != null;
    }
}
