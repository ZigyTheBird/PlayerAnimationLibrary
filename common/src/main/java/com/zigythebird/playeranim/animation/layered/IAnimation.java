/*
 * MIT License
 *
 * Copyright (c) 2022 KosmX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.zigythebird.playeranim.animation.layered;

import com.zigythebird.playeranim.animation.AnimationData;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranim.cache.bones.PlayerAnimBone;
import org.jetbrains.annotations.NotNull;

public interface IAnimation {
    FirstPersonConfiguration DEFAULT_FIRST_PERSON_CONFIG = new FirstPersonConfiguration();

    /**
     * Animation tick, on lag-free client 20 [tick/sec]
     * You can get the animations time from other places, but it will be invoked when the animation is ACTIVE
     */
    default void tick(AnimationData state) {}

    /**
     * Called before rendering a character
     * @param state Current animation state which can be used to get the player and the current partial tick.
     */
    default void setupAnim(AnimationData state) {}

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
