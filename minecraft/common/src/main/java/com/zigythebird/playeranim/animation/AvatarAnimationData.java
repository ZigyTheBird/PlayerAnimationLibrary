package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.accessors.IAnimatedAvatar;
import com.zigythebird.playeranimcore.animation.AnimationData;
import net.minecraft.world.entity.Avatar;

public class AvatarAnimationData extends AnimationData {
    private final Avatar player;

    public AvatarAnimationData(Avatar player, float velocity, float partialTick) {
        super(velocity, partialTick);
        this.player = player;
    }

    /**
     * Gets the current player being animated
     */
    public Avatar getPlayer() {
        return this.player;
    }

    /**
     * Gets the current player animation manager
     */
    public AvatarAnimManager getPlayerAnimManager() {
        return ((IAnimatedAvatar) getPlayer()).playerAnimLib$getAnimManager();
    }

    @Override
    public AvatarAnimationData copy() {
        return new AvatarAnimationData(getPlayer(), getVelocity(), getPartialTick());
    }
}
