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
public record KeyframeStack<T extends Keyframe>(List<T> xKeyframes, List<T> yKeyframes, List<T> zKeyframes) {
	public KeyframeStack() {
		this(new ObjectArrayList<>(), new ObjectArrayList<>(), new ObjectArrayList<>());
	}

	public static <F extends Keyframe> KeyframeStack<F> from(KeyframeStack<F> otherStack) {
		return new KeyframeStack<>(otherStack.xKeyframes, otherStack.yKeyframes, otherStack.zKeyframes);
	}

	public double getLastKeyframeTime() {
		return Math.max(getLastXAxisKeyframeTime(),
				Math.max(getLastYAxisKeyframeTime(), getLastZAxisKeyframeTime()));
	}

	public double getLastXAxisKeyframeTime() {
		double xTime = 0;

		for (T frame : xKeyframes()) {
			xTime += frame.length();
		}

		return xTime;
	}

	public double getLastYAxisKeyframeTime() {
		double yTime = 0;

		for (T frame : yKeyframes()) {
			yTime += frame.length();
		}

		return yTime;
	}

	public double getLastZAxisKeyframeTime() {
		double zTime = 0;

		for (T frame : zKeyframes()) {
			zTime += frame.length();
		}

		return zTime;
	}

	public boolean hasKeyframes() {
		return !xKeyframes().isEmpty() || !yKeyframes().isEmpty() || !zKeyframes().isEmpty();
	}
}
