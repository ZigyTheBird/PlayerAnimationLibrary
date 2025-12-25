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

import com.zigythebird.playeranimcore.PlayerAnimLib;
import com.zigythebird.playeranimcore.data.DataTicket;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

//TODO Move to the data folder next MC breaking change
public class AnimationData {
	public static final DataTicket<Float> PARTIAL_TICK = DataTicket.create("partial_tick", Float.class);
	public static final DataTicket<Float> VELOCITY = DataTicket.create("velocity", Float.class);
	public static final DataTicket<Boolean> IS_FIRST_PERSON_PASS = DataTicket.create("first_person_pass", Boolean.class);

	private final Map<DataTicket<?>, Object> data;

	public AnimationData() {
		this.data = new Reference2ObjectOpenHashMap<>();
	}

	AnimationData(Map<DataTicket<?>, Object> data) {
		this.data = data;
	}

	/**
	 * Gets the fractional value of the current game tick that has passed in rendering
	 */
	public float getPartialTick() {
		return this.getOrDefaultData(PARTIAL_TICK, 0f);
	}

	public float getVelocity() {
		return this.getOrDefaultData(VELOCITY, 0f);
	}

	public boolean isFirstPersonPass() {
		return this.getOrDefaultData(IS_FIRST_PERSON_PASS, false);
	}

	public boolean isFirstPersonPass() {
		return this.isFirstPersonPass;
	}

	/**
	 * Helper to determine if the entity is moving.
	 */
	public boolean isMoving() {
		return this.getVelocity() > 0.015F;
	}

	/**
	 * The less strict counterpart of the method above.
	 */
	public boolean isMovingLenient() {
		return this.getVelocity() > 1.0E-6F;
	}

	public void setVelocity(float velocity) {
		this.addData(VELOCITY, velocity);
	}

	public void setPartialTick(float partialTick) {
		this.addData(PARTIAL_TICK, partialTick);
	}

	/**
	 * Add data to the RenderState
	 * @param dataTicket The DataTicket identifying the data
	 * @param data The associated data
	 */
	public <D> void addData(DataTicket<D> dataTicket, @Nullable D data) {
		this.data.put(dataTicket, data);
	}

	/**
	 * @return Whether the RenderState has data associated with the given {@link DataTicket}
	 */
	public boolean hasData(DataTicket<?> dataTicket) {
		return this.data.containsKey(dataTicket);
	}

	/**
	 * Get previously set data on the RenderState by its associated {@link DataTicket}.
	 * <p>
	 * Note that you should <b><u>NOT</u></b> be attempting to retrieve data you don't know exists.<br>
	 * Use {@link #hasData(DataTicket)} if unsure
	 *
	 * @param dataTicket The DataTicket associated with the data
	 * @return The data contained on this RenderState, null if the data is set to null, or an exception if the data doesn't exist
	 */
	public @Nullable <D> D getData(DataTicket<D> dataTicket) {
		Object data = this.data.get(dataTicket);

		if (data == null && !hasData(dataTicket))
			throw new IllegalArgumentException("Attempted to retrieve data from AnimationData that does not exist. Check your code!");

		try {
			return (D)data;
		}
		catch (ClassCastException ex) {
			PlayerAnimLib.LOGGER.error("Attempted to retrieve incorrectly typed data from AnimationData. Possibly a mod or DataTicket conflict? Expected: {}, found data type {}", dataTicket, data.getClass().getName(), ex);

			throw ex;
		}
	}

	/**
	 * Get previously set data by its associated {@link DataTicket},
	 * or a default value if the data does not exist
	 *
	 * @param dataTicket The DataTicket associated with the data
	 * @param defaultValue The fallback value if no data has been set for the given DataTicket
	 * @return The data contained on this RenderState, null if the data is set to null, or {@code defaultValue} if not present
	 */
	public <D> D getOrDefaultData(DataTicket<D> dataTicket, @Nullable D defaultValue) {
		Object data = this.data.get(dataTicket);

		if (data == null && !hasData(dataTicket))
			return defaultValue;

		try {
			return (D)data;
		}
		catch (ClassCastException ex) {
			PlayerAnimLib.LOGGER.error("Attempted to retrieve incorrectly typed data from AnimationData. Possibly a mod or DataTicket conflict? Expected: {}, found data type {}", dataTicket, data.getClass().getName(), ex);

			return defaultValue;
		}
	}

	public AnimationData copy() {
		return new AnimationData(new Reference2ObjectOpenHashMap<>(this.data));
	}
}
