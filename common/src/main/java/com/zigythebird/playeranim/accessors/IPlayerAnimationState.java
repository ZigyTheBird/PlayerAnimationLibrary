package com.zigythebird.playeranim.accessors;

import com.zigythebird.playeranim.animation.PlayerAnimManager;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of PlayerRenderState
 */
public interface IPlayerAnimationState {

    // bool isLocalPlayer
    boolean playerAnimLib$isLocalPlayer();
    void playerAnimLib$setLocalPlayer(boolean value);

    boolean playerAnimLib$isCameraEntity();
    void playerAnimLib$setCameraEntity(boolean value);

    // AnimationApplier animationApplier
    void playerAnimLib$setAnimManager(PlayerAnimManager manager);
    @Nullable PlayerAnimManager playerAnimLib$getAnimManager();
}
