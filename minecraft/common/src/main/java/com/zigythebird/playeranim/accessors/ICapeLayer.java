package com.zigythebird.playeranim.accessors;

import com.zigythebird.playeranim.animation.AvatarAnimManager;

@SuppressWarnings("unused")
public interface ICapeLayer {
    default void applyBend(AvatarAnimManager manager, float bend) {}
    default void resetBend() {}
}
