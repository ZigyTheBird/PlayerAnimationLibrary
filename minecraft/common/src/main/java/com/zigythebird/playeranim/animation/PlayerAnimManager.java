package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranimcore.animation.layered.AnimationStack;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import com.zigythebird.playeranim.util.RenderUtil;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.player.AbstractClientPlayer;
import org.jetbrains.annotations.ApiStatus;

/**
 * The animation data collection for a given player instance
 * <p>
 * Generally speaking, a single working-instance of a player will have a single instance of {@code PlayerAnimManager} associated with it
 */
public class PlayerAnimManager extends AnimationStack {
	private final AbstractClientPlayer player;

	private float lastUpdateTime;
	private boolean isFirstTick = true;
	private float firstTickTime = -1;
	private float tickDelta;

	public PlayerAnimManager(AbstractClientPlayer player) {
		this.player = player;
	}

	public float getLastUpdateTime() {
		return this.lastUpdateTime;
	}

	public void updatedAt(float updateTime) {
		this.lastUpdateTime = updateTime;
	}

	public float getFirstTickTime() {
		return this.firstTickTime;
	}

	public void startedAt(float time) {
		this.firstTickTime = time;
	}

	public boolean isFirstTick() {
		return this.isFirstTick;
	}

	protected void finishFirstTick() {
		this.isFirstTick = false;
	}

	public float getTickDelta() {
		return this.tickDelta;
	}

	/**
	 * If you touch this, you're a horrible person.
	 */
	@ApiStatus.Internal
	public void setTickDelta(float tickDelta) {
		this.tickDelta = tickDelta;
	}

	public AbstractClientPlayer getPlayer() {
		return player;
	}

	public void updatePart(String partName, ModelPart part, AnimationProcessor processor) {
		PlayerAnimBone bone = processor.getBone(partName);
		PartPose initialPose = part.getInitialPose();
		bone = this.get3DTransform(bone);
		RenderUtil.translatePartToBone(part, bone, initialPose);
	}
}
