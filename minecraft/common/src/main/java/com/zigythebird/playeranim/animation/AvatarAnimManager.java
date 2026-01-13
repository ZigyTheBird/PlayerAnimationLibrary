package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.accessors.IAvatarAnimationState;
import com.zigythebird.playeranim.util.RenderUtil;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.layered.AnimationStack;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.jetbrains.annotations.ApiStatus;

/**
 * The animation data collection for a given player instance
 * <p>
 * Generally speaking, a single working-instance of a player will have a single instance of {@code PlayerAnimManager} associated with it
 */
public class AvatarAnimManager extends AnimationStack {
	private float lastUpdateTime;

	public AvatarAnimManager() {}

	public float getLastUpdateTime() {
		return this.lastUpdateTime;
	}

	@ApiStatus.Internal
	public void updatedAt(float updateTime) {
		this.lastUpdateTime = updateTime;
	}

	public void updatePart(ModelPart part, PlayerAnimBone bone) {
		PartPose initialPose = part.getInitialPose();
		bone = this.get3DTransform(bone);
		RenderUtil.translatePartToBone(part, bone, initialPose);
	}

	@ApiStatus.Internal
	public void handleAnimations(IAvatarAnimationState state) {
		if (state instanceof AvatarRenderState avatarRenderState) {
			float currentFrameTime = avatarRenderState.ageInTicks;

			if (currentFrameTime == this.getLastUpdateTime())
				return;

			AnimationData animationData = state.playerAnimLib$getAnimData();
			// I have to do this due to floating-point error nonsense
			animationData.setPartialTick(currentFrameTime - (int)currentFrameTime);

			if (!Minecraft.getInstance().isPaused()) {
				for (int i = 0; i < (int)currentFrameTime - (int)this.getLastUpdateTime(); i++)
					this.tick(animationData.copy());

				this.updatedAt(currentFrameTime);
			}

			this.setupAnimation(animationData);
		}
	}

	protected void setupAnimation(AnimationData state) {
		this.getLayers().removeIf(pair -> pair.right() == null || pair.right().canRemove());
		for (Pair<Integer, IAnimation> pair : this.getLayers()) {
			IAnimation animation = pair.right();

			if (animation.isActive())
				animation.setupAnim(state.copy());
		}
	}
}
