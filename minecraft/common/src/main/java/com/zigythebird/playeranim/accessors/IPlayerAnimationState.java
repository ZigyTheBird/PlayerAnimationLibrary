package com.zigythebird.playeranim.accessors;

import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of PlayerRenderState
 */
public interface IPlayerAnimationState {
    boolean playerAnimLib$isCameraEntity();
    void playerAnimLib$setCameraEntity(boolean value);

    // AnimationApplier animationApplier
    void playerAnimLib$setAnimManager(PlayerAnimManager manager);
    @Nullable PlayerAnimManager playerAnimLib$getAnimManager();

    void playerAnimLib$setAnimProcessor(AnimationProcessor processor);
    AnimationProcessor playerAnimLib$getAnimProcessor();
}
