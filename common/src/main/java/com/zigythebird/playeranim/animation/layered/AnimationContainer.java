package com.zigythebird.playeranim.animation.layered;

import com.zigythebird.playeranim.animation.AnimationState;
import com.zigythebird.playeranim.animation.State;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranim.cache.PlayerAnimBone;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A container to make swapping animation object easier
 * It will clone the behaviour of the held animation
 * <p>
 * you can put endless AnimationContainer into each other
 * @param <T> Nullable animation
 */
public class AnimationContainer<T extends IAnimation> implements IAnimation {
    @Nullable
    protected T anim;

    public AnimationContainer(@Nullable T anim) {
        this.anim = anim;
    }

    public AnimationContainer() {
        this.anim = null;
    }

    public void setAnim(@Nullable T newAnim) {
        this.anim = newAnim;
    }

    public @Nullable T getAnim() {
        return this.anim;
    }

    @Override
    public @NotNull State getAnimationState() {
        return anim != null ? anim.getAnimationState() : State.STOPPED;
    }

    @Override
    public void tick(AnimationState state) {
        if (anim != null) anim.tick(state);
    }

    @Override
    public void get3DTransform(@NotNull PlayerAnimBone bone) {
        if (anim != null) anim.get3DTransform(bone);
    }

    @Override
    public void setupAnim(AnimationState state) {
        if (this.anim != null) this.anim.setupAnim(state);
    }

    @Override
    public @NotNull FirstPersonMode getFirstPersonMode() {
        return anim != null ? anim.getFirstPersonMode() : FirstPersonMode.NONE;
    }

    @Override
    public @NotNull FirstPersonConfiguration getFirstPersonConfiguration() {
        return anim != null ? anim.getFirstPersonConfiguration() : IAnimation.super.getFirstPersonConfiguration();
    }
}
