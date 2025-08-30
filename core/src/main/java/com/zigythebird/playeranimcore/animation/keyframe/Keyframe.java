/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranimcore.animation.keyframe;

import com.zigythebird.playeranimcore.easing.EasingType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import team.unnamed.mocha.parser.ast.Expression;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Animation keyframe data
 *
 * @param length The length (in ticks) the keyframe lasts for
 * @param startValue The value to start the keyframe's transformation with
 * @param endValue The value to end the keyframe's transformation with
 * @param easingType The {@code EasingType} to use for transformations
 * @param easingArgs The arguments to provide to the easing calculation
 */
public record Keyframe(float length, List<Expression> startValue, List<Expression> endValue, EasingType easingType, List<List<Expression>> easingArgs) {
	public Keyframe(float length, List<Expression> startValue, List<Expression> endValue) {
		this(length, startValue, endValue, EasingType.LINEAR);
	}

	public Keyframe(float length, List<Expression> startValue, List<Expression> endValue, EasingType easingType) {
		this(length, startValue, endValue, easingType, new ObjectArrayList<>(0));
	}

	public Keyframe(float length) {
		this(length, Collections.emptyList(), Collections.emptyList());
	}

	public static float getLastKeyframeTime(List<Keyframe> list) {
		return (float) list.stream().mapToDouble(Keyframe::length).sum();
	}

	public static Keyframe getKeyframeAtTime(List<Keyframe> list, float tick) {
		float totalFrameTime = 0;

		for (Keyframe keyframe : list) {
			totalFrameTime += keyframe.length;
			if (totalFrameTime >= tick) return keyframe;
		}

		return list.getLast();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.length, this.startValue, this.endValue, this.easingType.id, this.easingArgs);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Keyframe keyframe)) return false;
        return Float.compare(length, keyframe.length) == 0 && easingType.id == keyframe.easingType.id && Objects.equals(endValue, keyframe.endValue) && Objects.equals(startValue, keyframe.startValue) && Objects.equals(easingArgs, keyframe.easingArgs);
	}
}
