package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.dataticket.DataTicket;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.Map;

/**
 * Animation state handler for end-users
 * <p>
 * This is where users would set their selected animation to play,
 * stop the controller, or any number of other animation-related actions.
 */
public class AnimationState {
	private final AbstractClientPlayer player;
	private final float partialTick;
	private final boolean isMoving;
	private final Map<DataTicket<?>, Object> extraData = new Object2ObjectOpenHashMap<>();
	public float animationTick;

	public AnimationState(AbstractClientPlayer player, float partialTick, boolean isMoving) {
		this.player = player;
		this.partialTick = partialTick;
		this.isMoving = isMoving;
	}

	/**
	 * Gets the amount of ticks that have passed in either the current transition or
	 * animation, depending on the controller's AnimationState.
	 */
	public float getAnimationTick() {
		return this.animationTick;
	}

	/**
	 * Gets the current player being animated
	 */
	public AbstractClientPlayer getPlayer() {
		return this.player;
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

	/**
	 * Gets the optional additional data map for the state
	 *
	 * @see DataTicket
	 */
	public Map<DataTicket<?>, ?> getExtraData() {
		return this.extraData;
	}

	/**
	 * Get a data value saved to this animation state by the ticket for that data
	 *
	 * @see DataTicket
	 * @param dataTicket The {@link DataTicket} for the data to retrieve
	 * @return The cached data for the given {@code DataTicket}, or null if not saved
	 */
	public <D> D getData(DataTicket<D> dataTicket) {
		return dataTicket.getData(this.extraData);
	}

	/**
	 * Save a data value for the given {@link DataTicket} in the additional data map
	 *
	 * @param dataTicket The {@code DataTicket} for the data value
	 * @param data The data value
	 */
	public <D> void setData(DataTicket<D> dataTicket, D data) {
		this.extraData.put(dataTicket, data);
	}
}
