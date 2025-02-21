/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranim.animation.keyframe.event.data;

import com.zigythebird.playeranim.animation.keyframe.Keyframe;

import java.util.Objects;

/**
 * Base class for custom {@link Keyframe} events
 *
 * @see ParticleKeyframeData
 * @see SoundKeyframeData
 */
public abstract class KeyFrameData {
	private final double startTick;

	public KeyFrameData(double startTick) {
		this.startTick = startTick;
	}

	/**
	 * Gets the start tick of the keyframe instruction
	 */
	public double getStartTick() {
		return this.startTick;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || getClass() != obj.getClass())
			return false;

		return this.hashCode() == obj.hashCode();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.startTick);
	}
}
