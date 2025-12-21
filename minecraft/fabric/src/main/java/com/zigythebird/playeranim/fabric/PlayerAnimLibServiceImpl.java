package com.zigythebird.playeranim.fabric;

import com.zigythebird.playeranim.PlayerAnimLibService;
import net.fabricmc.loader.api.FabricLoader;

public final class PlayerAnimLibServiceImpl implements PlayerAnimLibService {
    @Override
    public boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }
}
