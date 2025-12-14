package com.zigythebird.playeranim.accessors;

import com.zigythebird.playeranim.animation.AvatarAnimManager;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;

/**
 * Extension of PlayerRenderState
 */
public interface IAvatarAnimationState {
    @Deprecated(forRemoval = true)
    boolean playerAnimLib$isCameraEntity();
    void playerAnimLib$setCameraEntity(boolean value);

    boolean playerAnimLib$isFirstPersonPass();
    void playerAnimLib$setFirstPersonPass(boolean value);

    // AnimationApplier animationApplier
    void playerAnimLib$setAnimManager(AvatarAnimManager manager);
    AvatarAnimManager playerAnimLib$getAnimManager();

    void playerAnimLib$setAnimProcessor(AnimationProcessor processor);
    AnimationProcessor playerAnimLib$getAnimProcessor();
}
