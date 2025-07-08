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

	@Override
	public int hashCode() {
		return Objects.hash(this.length, this.startValue, this.endValue, this.easingType, this.easingArgs);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || getClass() != obj.getClass())
			return false;

		return hashCode() == obj.hashCode();
	}
}
