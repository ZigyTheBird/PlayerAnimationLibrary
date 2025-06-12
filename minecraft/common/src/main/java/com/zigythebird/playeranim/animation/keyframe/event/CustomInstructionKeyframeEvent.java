/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranim.animation.keyframe.event;

import com.zigythebird.playeranim.animation.AnimationController;
import com.zigythebird.playeranim.animation.AnimationData;
import com.zigythebird.playeranim.animation.keyframe.event.data.CustomInstructionKeyframeData;
import net.minecraft.client.player.AbstractClientPlayer;

/**
 * The {@link KeyFrameEvent} specific to the {@link AnimationController#setCustomInstructionKeyframeHandler(AnimationController.CustomKeyframeHandler)}
 * <p>
 * Called when a custom instruction keyframe is encountered
 */
public class CustomInstructionKeyframeEvent extends KeyFrameEvent<CustomInstructionKeyframeData> {
	public CustomInstructionKeyframeEvent(AbstractClientPlayer player, float animationTick, AnimationController controller,
										  CustomInstructionKeyframeData customInstructionKeyframeData, AnimationData animationData) {
		super(player, animationTick, controller, customInstructionKeyframeData, animationData);
	}

	/**
	 * Get the {@link CustomInstructionKeyframeData} relevant to this event call
	 */
	@Override
	public CustomInstructionKeyframeData getKeyframeData() {
		return super.getKeyframeData();
	}
}
