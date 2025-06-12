package com.zigythebird.playeranim.animation.keyframe.event.data;

import com.zigythebird.playeranim.animation.keyframe.Keyframe;

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
