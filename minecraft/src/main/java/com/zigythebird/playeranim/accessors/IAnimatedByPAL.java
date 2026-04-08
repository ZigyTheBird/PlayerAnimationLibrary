package com.zigythebird.playeranim.accessors;

import com.zigythebird.playeranimcore.bones.PlayerAnimBone;

import java.util.Map;

public interface IAnimatedByPAL {
    default Map<String, PlayerAnimBone> pal$getCurrentBoneStates() {
        return null;
    }
}
