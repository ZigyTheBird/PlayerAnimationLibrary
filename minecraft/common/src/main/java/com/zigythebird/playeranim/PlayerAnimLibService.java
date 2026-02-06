package com.zigythebird.playeranim;

import com.zigythebird.playeranimcore.util.ServiceUtil;

public interface PlayerAnimLibService extends ServiceUtil.ActiveService {
    PlayerAnimLibService INSTANCE = ServiceUtil.loadService(PlayerAnimLibService.class);

    boolean isModLoaded(String id);
}
