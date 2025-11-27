package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.accessors.IAnimatedAvatar;
import com.zigythebird.playeranimcore.animation.AnimationData;
import net.minecraft.world.entity.Avatar;

public class AvatarAnimationData extends AnimationData {
    private final Avatar avatar;

    public AvatarAnimationData(Avatar avatar, float velocity, float partialTick) {
        super(velocity, partialTick);
        this.avatar = avatar;
    }

    /**
     * Gets the current avatar being animated
     */
    public Avatar getAvatar() {
        return this.avatar;
    }

    /**
     * Gets the current avatar animation manager
     */
    public AvatarAnimManager getAnimManager() {
        return ((IAnimatedAvatar) getAvatar()).playerAnimLib$getAnimManager();
    }

    @Override
    public AvatarAnimationData copy() {
        return new AvatarAnimationData(getAvatar(), getVelocity(), getPartialTick());
    }
}
