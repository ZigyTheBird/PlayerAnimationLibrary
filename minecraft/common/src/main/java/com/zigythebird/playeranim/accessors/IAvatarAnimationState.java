package com.zigythebird.playeranim.accessors;

import com.zigythebird.playeranim.animation.AvatarAnimManager;

/**
 * Extension of PlayerRenderState
 */
public interface IAvatarAnimationState {
    boolean playerAnimLib$isCameraEntity();
    void playerAnimLib$setCameraEntity(boolean value);

    // AnimationApplier animationApplier
    void playerAnimLib$setAnimManager(AvatarAnimManager manager);
    AvatarAnimManager playerAnimLib$getAnimManager();
}
