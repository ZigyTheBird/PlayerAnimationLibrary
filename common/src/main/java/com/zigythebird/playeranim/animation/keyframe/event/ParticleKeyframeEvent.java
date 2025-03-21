package com.zigythebird.playeranim.animation.keyframe.event;

import com.zigythebird.playeranim.animation.AnimationController;
import com.zigythebird.playeranim.animation.AnimationData;
import com.zigythebird.playeranim.animation.keyframe.event.data.ParticleKeyframeData;
import net.minecraft.client.player.AbstractClientPlayer;

/**
 * The {@link KeyFrameEvent} specific to the {@link AnimationController#particleKeyframeHandler}
 * <p>
 * Called when a particle instruction keyframe is encountered
 */
public class ParticleKeyframeEvent extends KeyFrameEvent<ParticleKeyframeData> {
	public ParticleKeyframeEvent(AbstractClientPlayer player, double animationTick, AnimationController controller,
								 ParticleKeyframeData particleKeyFrameData, AnimationData animationData) {
		super(player, animationTick, controller, particleKeyFrameData, animationData);
	}

	/**
	 * Get the {@link ParticleKeyframeData} relevant to this event call
	 */
	@Override
	public ParticleKeyframeData getKeyframeData() {
		return super.getKeyframeData();
	}
}
