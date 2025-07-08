package com.zigythebird.playeranim.accessors;

import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;

/**
 * Extension of PlayerRenderState
 */
public interface IPlayerAnimationState {
    boolean playerAnimLib$isCameraEntity();
    void playerAnimLib$setCameraEntity(boolean value);

    // AnimationApplier animationApplier
    void playerAnimLib$setAnimManager(PlayerAnimManager manager);
    PlayerAnimManager playerAnimLib$getAnimManager();

    void playerAnimLib$setAnimProcessor(AnimationProcessor processor);
    AnimationProcessor playerAnimLib$getAnimProcessor();
}
