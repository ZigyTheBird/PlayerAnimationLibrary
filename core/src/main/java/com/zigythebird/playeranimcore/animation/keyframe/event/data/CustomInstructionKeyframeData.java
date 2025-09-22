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

package com.zigythebird.playeranimcore.animation.keyframe.event.data;

import com.zigythebird.playeranimcore.animation.keyframe.Keyframe;

import java.util.Objects;

/**
 * Custom instruction {@link Keyframe} instruction holder
 */
public class CustomInstructionKeyframeData extends KeyFrameData {
	private final String instructions;

	public CustomInstructionKeyframeData(float startTick, String instructions) {
		super(startTick);

		this.instructions = instructions;
	}

	/**
	 * Gets the instructions string given by the {@link Keyframe} instruction from the {@code animation.json}
	 */
	public String getInstructions() {
		return this.instructions;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getStartTick(), instructions);
	}
}
