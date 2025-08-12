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
