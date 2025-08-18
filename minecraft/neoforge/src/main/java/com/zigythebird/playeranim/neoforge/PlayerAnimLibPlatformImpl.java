package com.zigythebird.playeranim.neoforge;

import net.neoforged.fml.loading.LoadingModList;

public class PlayerAnimLibPlatformImpl {
    public static boolean isModLoaded(String id) {
        return LoadingModList.get().getModFileById(id) != null;
    }
}
