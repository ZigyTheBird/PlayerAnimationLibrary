package com.zigythebird.playeranim.neoforge;

import com.zigythebird.playeranim.PlayerAnimLibPlatform;
import net.neoforged.fml.loading.FMLLoader;

public final class PlayerAnimLibPlatformImpl implements PlayerAnimLibPlatform {
    @Override
    public boolean isModLoaded(String id) {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById(id) != null;
    }
}
