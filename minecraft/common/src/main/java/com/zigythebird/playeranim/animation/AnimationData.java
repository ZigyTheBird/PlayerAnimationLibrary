package com.zigythebird.playeranim.animation;

import net.minecraft.client.player.AbstractClientPlayer;

public class AnimationData {
	private final AbstractClientPlayer player;
	private float partialTick;
	private float velocity;
	private float animationTick;

	public AnimationData(AbstractClientPlayer player, float partialTick, float velocity) {
		this.player = player;
		this.partialTick = partialTick;
		this.velocity = velocity;
	}

	public AnimationData(AbstractClientPlayer player, float partialTick, float velocity, float animationTick) {
		this.player = player;
		this.partialTick = partialTick;
		this.velocity = velocity;
		this.animationTick = animationTick;
	}

	/**
	 * Gets the current player being animated
	 */
	public AbstractClientPlayer getPlayer() {
		return this.player;
	}

	/**
	 * Gets the current player animation manager
	 */
	public PlayerAnimManager getPlayerAnimManager() {
		return getPlayer().playerAnimLib$getAnimManager();
	}

	/**
	 * Gets the number of ticks that have passed in either the current transition or
	 * animation, depending on the controller's AnimationData.
	 */
	public float getAnimationTick() {
		return this.animationTick;
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

	public void setAnimationTick(float animationTick) {
		this.animationTick = animationTick;
	}

	public void setPartialTick(float partialTick) {
		this.partialTick = partialTick;
	}

	public void setVelocity(float velocity) {
		this.velocity = velocity;
	}

	public AnimationData copy() {
		return new AnimationData(player, partialTick, velocity, animationTick);
	}
}
