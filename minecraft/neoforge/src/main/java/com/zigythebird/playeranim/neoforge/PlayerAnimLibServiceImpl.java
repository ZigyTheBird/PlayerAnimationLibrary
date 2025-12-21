package com.zigythebird.playeranim.neoforge;

import com.zigythebird.playeranim.PlayerAnimLibService;
import net.neoforged.fml.loading.FMLLoader;

public final class PlayerAnimLibServiceImpl implements PlayerAnimLibService {
    @Override
    public boolean isModPresent(String id) {
        return FMLLoader.getCurrent().getLoadingModList().getModFileById(id) != null;
    }
}
