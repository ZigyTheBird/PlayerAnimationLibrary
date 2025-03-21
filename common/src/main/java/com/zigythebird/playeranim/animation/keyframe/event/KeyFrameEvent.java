/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranim.animation.keyframe.event;

import com.zigythebird.playeranim.animation.AnimationController;
import com.zigythebird.playeranim.animation.AnimationData;
import com.zigythebird.playeranim.animation.keyframe.Keyframe;
import com.zigythebird.playeranim.animation.keyframe.event.data.KeyFrameData;
import net.minecraft.client.player.AbstractClientPlayer;

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
	private final AbstractClientPlayer animatable;
	private final double animationTick;
	private final AnimationController controller;
	private final E eventKeyFrame;
	private final AnimationData animationData;

	public KeyFrameEvent(AbstractClientPlayer animatable, double animationTick, AnimationController controller, E eventKeyFrame, AnimationData animationData) {
		this.animatable = animatable;
		this.animationTick = animationTick;
		this.controller = controller;
		this.eventKeyFrame = eventKeyFrame;
		this.animationData = animationData;
	}

	/**
	 * Gets the amount of ticks that have passed in either the current transition or
	 * animation, depending on the controller's AnimationData.
	 */
	public double getAnimationTick() {
		return animationTick;
	}

	/**
	 * Gets the player being rendered
	 */
	public AbstractClientPlayer getAnimatable() {
		return animatable;
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
