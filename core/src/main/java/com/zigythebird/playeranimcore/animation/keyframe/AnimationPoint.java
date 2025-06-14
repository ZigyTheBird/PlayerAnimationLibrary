/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranimcore.animation.keyframe;

import com.zigythebird.playeranimcore.animation.EasingType;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.parser.ast.Expression;

import java.util.List;

/**
 * Animation state record that holds the state of an animation at a given point
 *
 * @param easingType The easing type
 * @param easingArgs The easing arguments
 * @param currentTick The lerped tick time (current tick + partial tick) of the point
 * @param transitionLength The length of time (in ticks) that the point should take to transition
 * @param animationStartValue The start value to provide to the animation handling system
 * @param animationEndValue The end value to provide to the animation handling system
 */
public record AnimationPoint(EasingType easingType, @Nullable List<List<Expression>> easingArgs, float currentTick, float transitionLength, float animationStartValue, float animationEndValue) {
	public AnimationPoint(Keyframe keyframe, float currentTick, float transitionLength, float animationStartValue, float animationEndValue) {
		this(keyframe == null ? EasingType.LINEAR : keyframe.easingType(), keyframe == null ? null : keyframe.easingArgs(), currentTick, transitionLength, animationStartValue, animationEndValue);
	}

	@Override
	public String toString() {
		return "Tick: " + this.currentTick +
				" | Transition Length: " + this.transitionLength +
				" | Start Value: " + this.animationStartValue +
				" | End Value: " + this.animationEndValue;
	}
}
