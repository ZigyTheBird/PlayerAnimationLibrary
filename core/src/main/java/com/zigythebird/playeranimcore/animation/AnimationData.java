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

package com.zigythebird.playeranimcore.animation;

public class AnimationData {
	private float velocity;
	private float partialTick;

	public AnimationData(float velocity, float partialTick) {
		this.velocity = velocity;
		this.partialTick = partialTick;
	}

	/**
	 * Gets the fractional value of the current game tick that has passed in rendering
	 */
	public float getPartialTick() {
		return this.partialTick;
	}

	/**
	 * The player's velocity.
	 */
	public float getVelocity() {
		return this.velocity;
	}

	/**
	 * Helper to determine if the player is moving.
	 */
	public boolean isMoving() {
		return this.velocity > 0.015F;
	}

	/**
	 * The less strict counterpart of the method above.
	 */
	public boolean isMovingLenient() {
		return this.velocity > 1.0E-6F;
	}

	public void setVelocity(float velocity) {
		this.velocity = velocity;
	}

	public void setPartialTick(float partialTick) {
		this.partialTick = partialTick;
	}

	public AnimationData copy() {
		return new AnimationData(velocity, partialTick);
	}
}
