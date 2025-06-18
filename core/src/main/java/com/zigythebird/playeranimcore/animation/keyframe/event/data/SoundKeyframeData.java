package com.zigythebird.playeranimcore.animation.keyframe.event.data;

import com.zigythebird.playeranimcore.animation.keyframe.Keyframe;

import java.util.Objects;

/**
 * Sound {@link Keyframe} instruction holder
 */
public class SoundKeyframeData extends KeyFrameData {
	private final String sound;

	public SoundKeyframeData(Float startTick, String sound) {
		super(startTick);

		this.sound = sound;
	}

	/**
	 * Gets the sound data given by the {@link Keyframe} instruction from the {@code animation.json}
	 */
	public String getSound() {
		return this.sound;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getStartTick(), this.sound);
	}
}
