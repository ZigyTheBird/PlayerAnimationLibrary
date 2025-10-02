package com.zigythebird.playeranim.accessors;

import com.zigythebird.playeranim.animation.AvatarAnimManager;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;

/**
 * Extension of PlayerRenderState
 */
public interface IAvatarAnimationState {
    boolean playerAnimLib$isCameraEntity();
    void playerAnimLib$setCameraEntity(boolean value);

    // AnimationApplier animationApplier
    void playerAnimLib$setAnimManager(AvatarAnimManager manager);
    AvatarAnimManager playerAnimLib$getAnimManager();

    void playerAnimLib$setAnimProcessor(AnimationProcessor processor);
    AnimationProcessor playerAnimLib$getAnimProcessor();
}
