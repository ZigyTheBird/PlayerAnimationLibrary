package com.zigythebird.playeranim.fabric;

import com.zigythebird.playeranim.PlayerAnimLibPlatform;
import net.fabricmc.loader.api.FabricLoader;

public final class PlayerAnimLibPlatformImpl implements PlayerAnimLibPlatform {
    @Override
    public boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }
}
