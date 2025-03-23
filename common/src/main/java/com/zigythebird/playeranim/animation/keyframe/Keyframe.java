/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranim.animation.keyframe;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import com.zigythebird.playeranim.animation.EasingType;
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
public record Keyframe(int tick, double length, List<Expression> startValue, List<Expression> endValue, EasingType easingType, List<List<Expression>> easingArgs) {
	public Keyframe(int tick, double length, List<Expression> startValue, List<Expression> endValue) {
		this(tick, length, startValue, endValue, EasingType.LINEAR);
	}

	public Keyframe(int tick, double length, List<Expression> startValue, List<Expression> endValue, EasingType easingType) {
		this(tick, length, startValue, endValue, easingType, new ObjectArrayList<>(0));
	}

	public Keyframe(int tick, double length) {
		this(tick, length, Collections.emptyList(), Collections.emptyList());
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
