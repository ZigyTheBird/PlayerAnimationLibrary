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
 * Particle {@link Keyframe} instruction holder
 */
public class ParticleKeyframeData extends KeyFrameData {
	private final String effect;
	private final String locator;
	private final String script;

	public ParticleKeyframeData(float startTick, String effect, String locator, String script) {
		super(startTick);

		this.script = script;
		this.locator = locator;
		this.effect = effect;
	}

	/**
	 * Gets the effect id given by the {@link Keyframe} instruction from the {@code animation.json}
	 */
	public String getEffect() {
		return this.effect;
	}

	/**
	 * Gets the locator string given by the {@link Keyframe} instruction from the {@code animation.json}
	 */
	public String getLocator() {
		return this.locator;
	}

	/**
	 * Gets the script string given by the {@link Keyframe} instruction from the {@code animation.json}
	 */
	public String script() {
		return this.script;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getStartTick(), effect, locator, script);
	}
}
