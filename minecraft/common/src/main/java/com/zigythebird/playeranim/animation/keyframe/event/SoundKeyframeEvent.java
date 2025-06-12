/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranim.animation.keyframe.event;

import com.zigythebird.playeranim.animation.AnimationController;
import com.zigythebird.playeranim.animation.AnimationData;
import com.zigythebird.playeranim.animation.keyframe.event.data.SoundKeyframeData;
import net.minecraft.client.player.AbstractClientPlayer;

/**
 * The {@link KeyFrameEvent} specific to the {@link AnimationController#setSoundKeyframeHandler(AnimationController.SoundKeyframeHandler)}
 * <p>
 * Called when a sound instruction keyframe is encountered
 */
public class SoundKeyframeEvent extends KeyFrameEvent<SoundKeyframeData> {
	public SoundKeyframeEvent(AbstractClientPlayer player, float animationTick, AnimationController controller,
							  SoundKeyframeData keyFrameData, AnimationData animationData) {
		super(player, animationTick, controller, keyFrameData, animationData);
	}

	@Override
	public SoundKeyframeData getKeyframeData() {
		return super.getKeyframeData();
	}
}
