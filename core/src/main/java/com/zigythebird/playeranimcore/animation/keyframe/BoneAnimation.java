/*
 * MIT License
 *
 * Copyright (c) 2024 GeckoLib
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
