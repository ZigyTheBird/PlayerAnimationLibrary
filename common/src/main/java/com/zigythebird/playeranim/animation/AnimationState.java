package com.zigythebird.playeranim.animation;

import net.minecraft.client.player.AbstractClientPlayer;

/**
 * Animation state handler for end-users
 * <p>
 * This is where users would set their selected animation to play,
 * stop the controller, or any number of other animation-related actions.
 */
public class AnimationState {
	private final AbstractClientPlayer player;
	private float partialTick;
	private boolean isMoving;
	public double animationTick;

	public AnimationState(AbstractClientPlayer player, float partialTick, boolean isMoving) {
		this.player = player;
		this.partialTick = partialTick;
		this.isMoving = isMoving;
	}

	public AnimationState(AbstractClientPlayer player, float partialTick, boolean isMoving, double animationTick) {
		this.player = player;
		this.partialTick = partialTick;
		this.isMoving = isMoving;
		this.animationTick = animationTick;
	}

	/**
	 * Gets the amount of ticks that have passed in either the current transition or
	 * animation, depending on the controller's AnimationState.
	 */
	public double getAnimationTick() {
		return this.animationTick;
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
	 * Gets the fractional value of the current game tick that has passed in rendering
	 */
	public float getPartialTick() {
		return this.partialTick;
	}

	/**
	 * Gets whether the current player is considered to be moving for animation purposes
	 * <p>
	 * Note that this is a best-case approximation of movement, and your needs may vary.
	 */
	public boolean isMoving() {
		return this.isMoving;
	}

	public void setIsMoving(boolean isMoving) {
		this.isMoving = isMoving;
	}

	public void setPartialTick(float partialTick) {
		this.partialTick = partialTick;
	}

	public AnimationState copy() {
		return new AnimationState(player, partialTick, isMoving, animationTick);
	}
}
