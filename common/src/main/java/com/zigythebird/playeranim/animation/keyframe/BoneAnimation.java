/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranim.animation.keyframe;

/**
 * A record of a deserialized animation for a given bone
 * <p>
 * Responsible for holding the various {@link Keyframe Keyframes} for the bone's animation transformations
 *
 * @param boneName The name of the bone as listed in the {@code animation.json}
 * @param rotationKeyFrames The deserialized rotation {@code Keyframe} stack
 * @param positionKeyFrames The deserialized position {@code Keyframe} stack
 * @param scaleKeyFrames The deserialized scale {@code Keyframe} stack
 * @param bendKeyFrames The deserialized bend {@code Keyframe} stack
 */
public record BoneAnimation(String boneName,
							KeyframeStack<Keyframe> rotationKeyFrames,
							KeyframeStack<Keyframe> positionKeyFrames,
							KeyframeStack<Keyframe> scaleKeyFrames,
							KeyframeStack<Keyframe> bendKeyFrames) {
}
