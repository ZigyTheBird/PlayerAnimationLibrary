/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranimcore.animation.keyframe;

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
		return Math.max(getLastXAxisKeyframeTime(), Math.max(getLastYAxisKeyframeTime(), getLastZAxisKeyframeTime()));
	}

	public float getLastXAxisKeyframeTime() {
		return (float) xKeyframes().stream().mapToDouble(Keyframe::length).sum();
	}

	public float getLastYAxisKeyframeTime() {
		return (float) yKeyframes().stream().mapToDouble(Keyframe::length).sum();
	}

	public float getLastZAxisKeyframeTime() {
		return (float) zKeyframes().stream().mapToDouble(Keyframe::length).sum();
	}

	public boolean hasKeyframes() {
		return !xKeyframes().isEmpty() || !yKeyframes().isEmpty() || !zKeyframes().isEmpty();
	}
}
