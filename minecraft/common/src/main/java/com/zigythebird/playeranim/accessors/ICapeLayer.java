package com.zigythebird.playeranim.accessors;

import com.zigythebird.playeranim.animation.AvatarAnimManager;

@Deprecated(forRemoval = true)
public interface ICapeLayer {
    default void applyBend(AvatarAnimManager manager, float bend) {}
    default void resetBend() {}
}
