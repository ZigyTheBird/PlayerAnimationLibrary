/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranimcore.animation.keyframe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A record of a deserialized animation for a given bone
 * <p>
 * Responsible for holding the various {@link Keyframe Keyframes} for the bone's animation transformations
 *
 * @param rotationKeyFrames The deserialized rotation {@code Keyframe} stack
 * @param positionKeyFrames The deserialized position {@code Keyframe} stack
 * @param scaleKeyFrames The deserialized scale {@code Keyframe} stack
 * @param bendKeyFrames The deserialized bend {@code Keyframe} stack
 */
public record BoneAnimation(KeyframeStack rotationKeyFrames,
							KeyframeStack positionKeyFrames,
							KeyframeStack scaleKeyFrames,
							List<Keyframe> bendKeyFrames) {

	public BoneAnimation() {
		this(new KeyframeStack(), new KeyframeStack(), new KeyframeStack(), new ArrayList<>());
	}

	public boolean hasKeyframes() {
		return rotationKeyFrames().hasKeyframes() || positionKeyFrames().hasKeyframes() ||
				scaleKeyFrames().hasKeyframes() || !bendKeyFrames.isEmpty();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof BoneAnimation that)) return false;
        return Objects.equals(scaleKeyFrames, that.scaleKeyFrames) && Objects.equals(bendKeyFrames, that.bendKeyFrames) && Objects.equals(rotationKeyFrames, that.rotationKeyFrames) && Objects.equals(positionKeyFrames, that.positionKeyFrames);
	}

	@Override
	public int hashCode() {
		return Objects.hash(rotationKeyFrames, positionKeyFrames, scaleKeyFrames, bendKeyFrames);
	}
}
