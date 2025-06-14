/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranimcore.animation.keyframe.event;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.CustomInstructionKeyframeData;

/**
 * The {@link KeyFrameEvent} specific to the {@link AnimationController#setCustomInstructionKeyframeHandler(AnimationController.CustomKeyframeHandler)}
 * <p>
 * Called when a custom instruction keyframe is encountered
 */
public class CustomInstructionKeyframeEvent extends KeyFrameEvent<CustomInstructionKeyframeData> {
	public CustomInstructionKeyframeEvent(float animationTick, AnimationController controller,
										  CustomInstructionKeyframeData customInstructionKeyframeData, AnimationData animationData) {
		super(animationTick, controller, customInstructionKeyframeData, animationData);
	}

	/**
	 * Get the {@link CustomInstructionKeyframeData} relevant to this event call
	 */
	@Override
	public CustomInstructionKeyframeData getKeyframeData() {
		return super.getKeyframeData();
	}
}
