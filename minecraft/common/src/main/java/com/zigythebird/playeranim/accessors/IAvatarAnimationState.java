package com.zigythebird.playeranim.accessors;

import com.zigythebird.playeranim.animation.AvatarAnimManager;
import com.zigythebird.playeranimcore.animation.AnimationData;
import org.jetbrains.annotations.ApiStatus;

/**
 * Extension of PlayerRenderState
 */
public interface IAvatarAnimationState {
    boolean playerAnimLib$isFirstPersonPass();

    @ApiStatus.Internal
    void playerAnimLib$setAnimManager(AvatarAnimManager manager);
    AvatarAnimManager playerAnimLib$getAnimManager();

    @ApiStatus.Internal
    void playerAnimLib$setAnimData(AnimationData data);
    AnimationData playerAnimLib$getAnimData();
}
