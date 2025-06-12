/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranim.animation.keyframe;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

/**
 * Stores a triplet of {@link Keyframe Keyframes} in an ordered stack
 */
public record KeyframeStack(List<Keyframe> xKeyframes, List<Keyframe> yKeyframes, List<Keyframe> zKeyframes) {
	public KeyframeStack() {
		this(new ObjectArrayList<>(), new ObjectArrayList<>(), new ObjectArrayList<>());
	}

	public static KeyframeStack from(KeyframeStack otherStack) {
		return new KeyframeStack(otherStack.xKeyframes, otherStack.yKeyframes, otherStack.zKeyframes);
	}

	public float getLastKeyframeTime() {
		return Math.max(getLastXAxisKeyframeTime(),
				Math.max(getLastYAxisKeyframeTime(), getLastZAxisKeyframeTime()));
	}

	public float getLastXAxisKeyframeTime() {
		float xTime = 0;

		for (Keyframe frame : xKeyframes()) {
			xTime += frame.length();
		}

		return xTime;
	}

	public float getLastYAxisKeyframeTime() {
		float yTime = 0;

		for (Keyframe frame : yKeyframes()) {
			yTime += frame.length();
		}

		return yTime;
	}

	public float getLastZAxisKeyframeTime() {
		float zTime = 0;

		for (Keyframe frame : zKeyframes()) {
			zTime += frame.length();
		}

		return zTime;
	}

	public boolean hasKeyframes() {
		return !xKeyframes().isEmpty() || !yKeyframes().isEmpty() || !zKeyframes().isEmpty();
	}
}
