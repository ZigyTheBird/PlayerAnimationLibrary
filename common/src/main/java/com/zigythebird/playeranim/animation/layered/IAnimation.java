package com.zigythebird.playeranim.animation.layered;

import com.zigythebird.playeranim.animation.AnimationState;
import com.zigythebird.playeranim.animation.State;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranim.cache.PlayerAnimBone;
import org.jetbrains.annotations.NotNull;

public interface IAnimation {
    FirstPersonConfiguration DEFAULT_FIRST_PERSON_CONFIG = new FirstPersonConfiguration();

    /**
     * Animation tick, on lag-free client 20 [tick/sec]
     * You can get the animations time from other places, but it will be invoked when the animation is ACTIVE
     */
    default void tick(AnimationState state) {}

    /**
     * Called before rendering a character
     * @param state Current animation state which can be used to get the player and the current partial tick.
     */
    default void setupAnim(AnimationState state) {}

    /**
     * Is the animation currently active?
     * The tick method will only be invoked when the animation is active
     */
    boolean isActive();

    /**
     * Transform a bone to match the current animation.
     * @param bone the bone being currently animated.
     */
    default void get3DTransform(@NotNull PlayerAnimBone bone) {}

    /**
     * Keep in mind that modifiers can't affect the first-person mode, at least not by default.
     */
    @NotNull
    default FirstPersonMode getFirstPersonMode() {
        return FirstPersonMode.NONE;
    }

    /**
     * Keep in mind that modifiers can't affect the first-person configuration, at least not by default.
     */
    @NotNull
    default FirstPersonConfiguration getFirstPersonConfiguration() {
        return DEFAULT_FIRST_PERSON_CONFIG;
    }
}
