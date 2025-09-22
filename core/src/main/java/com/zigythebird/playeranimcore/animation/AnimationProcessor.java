/*
 * MIT License
 *
 * Copyright (c) 2024 GeckoLib
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

package com.zigythebird.playeranimcore.animation;

import com.zigythebird.playeranimcore.animation.layered.AnimationStack;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public abstract class AnimationProcessor {
	/**
	 * Each AnimationProcessor must be bound to a player
	 */
	public AnimationProcessor() {}

	/**
	 * This method is called once per render frame for each player being rendered
	 * <p>
	 * It is an internal method for automated animation parsing.
	 */
	public abstract void handleAnimations(float partialTick, boolean fullTick);

	/**
	 * Tick and apply transformations to the model based on the current state of the {@link AnimationController}
	 *
	 * @param playerAnimManager		The PlayerAnimManager instance being used for this animation processor
	 * @param state                 An {@link AnimationData} instance applied to this render frame
	 */
	public void tickAnimation(AnimationStack playerAnimManager, AnimationData state) {
		playerAnimManager.getLayers().removeIf(pair -> pair.right() == null || pair.right().canRemove());
		for (Pair<Integer, IAnimation> pair : playerAnimManager.getLayers()) {
			IAnimation animation = pair.right();

			if (animation.isActive())
				animation.setupAnim(state.copy());
		}
	}

    /**
     * {@link Animation} and {@link Animation.LoopType} override pair,
     * used to define a playable animation stage for a player
     * TODO move to standalone class
     */
    public record QueuedAnimation(Animation animation, Animation.LoopType loopType) {}
}
