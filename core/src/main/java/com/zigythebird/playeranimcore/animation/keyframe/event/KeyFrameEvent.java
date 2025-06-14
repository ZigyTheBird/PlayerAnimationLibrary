/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranimcore.animation.keyframe.event;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.Keyframe;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.KeyFrameData;

/**
 * The base class for {@link Keyframe} events
 * <p>
 * These will be passed to one of the controllers in {@link AnimationController} when encountered during animation
 *
 * @see CustomInstructionKeyframeEvent
 * @see ParticleKeyframeEvent
 * @see SoundKeyframeEvent
 */
public abstract class KeyFrameEvent<E extends KeyFrameData> {
	private final float animationTick;
	private final AnimationController controller;
	private final E eventKeyFrame;
	private final AnimationData animationData;

	public KeyFrameEvent(float animationTick, AnimationController controller, E eventKeyFrame, AnimationData animationData) {
		this.animationTick = animationTick;
		this.controller = controller;
		this.eventKeyFrame = eventKeyFrame;
		this.animationData = animationData;
	}

	/**
	 * Gets the amount of ticks that have passed in either the current transition or
	 * animation, depending on the controller's AnimationData.
	 */
	public float getAnimationTick() {
		return animationTick;
	}

	/**
	 * Gets the {@link AnimationController} responsible for the currently playing animation
	 */
	public AnimationController getController() {
		return controller;
	}

	/**
	 * Returns the {@link KeyFrameData} relevant to the encountered {@link Keyframe}
	 */
	public E getKeyframeData() {
		return this.eventKeyFrame;
	}

	/**
	 * Returns the {@link AnimationData} for the current render pass
	 */
	public AnimationData getAnimationState() {
		return this.animationData;
	}
}
