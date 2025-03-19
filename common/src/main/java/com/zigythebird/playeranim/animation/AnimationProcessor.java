package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.animation.layered.IAnimation;
import com.zigythebird.playeranim.cache.PlayerAnimBone;
import com.zigythebird.playeranim.cache.PlayerAnimCache;
import com.zigythebird.playeranim.math.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

/**
 * DO NOT TOUCH THIS UNLESS YOU REALLY KNOW WHAT YOU'RE DOING.
 */
@ApiStatus.Internal
public class AnimationProcessor {
	private final Map<String, PlayerAnimBone> bones = new Object2ObjectOpenHashMap<>();
	protected Map<String, BoneSnapshot> boneSnapshots;

	protected double animTime;
	private double lastGameTickTime;
	private long lastRenderedInstance = -1;
	private final AbstractClientPlayer player;

	/**
	 * Each AnimationProcessor must be bound to a player
	 * @param player The player to whom this processor is bound
	 */
	public AnimationProcessor(AbstractClientPlayer player) {
		this.player = player;

		//Todo: Make an event where you can add custom body parts here
		this.registerPlayerAnimBone("body");
		this.registerPlayerAnimBone("right_arm");
		this.registerPlayerAnimBone("left_arm");
		this.registerPlayerAnimBone("right_leg");
		this.registerPlayerAnimBone("left_leg");
		this.registerPlayerAnimBone("head");
		this.registerPlayerAnimBone("torso");
		this.registerPlayerAnimBone("right_item");
		this.registerPlayerAnimBone("left_item");
		this.registerPlayerAnimBone("cape");
		this.registerPlayerAnimBone("elytra");
	}

	/**
	 * This method is called once per render frame for each player being rendered
	 * <p>
	 * It is an internal method for automated animation parsing.
	 */
	public void handleAnimations(float partialTick, boolean fullTick) {
		Vec3 velocity = player.getDeltaMovement();
		float avgVelocity = (float)((Math.abs(velocity.x) + Math.abs(velocity.z)) / 2f);
		AnimationState animationState = new AnimationState(player, partialTick, avgVelocity >= 0.015F);

		Minecraft mc = Minecraft.getInstance();
		PlayerAnimManager animatableManager = player.playerAnimLib$getAnimManager();
		double currentTick = player.tickCount;

		if (animatableManager.getFirstTickTime() == -1)
			animatableManager.startedAt(currentTick + partialTick);

		double currentFrameTime = currentTick + partialTick;
		boolean isReRender = !animatableManager.isFirstTick() && currentFrameTime == animatableManager.getLastUpdateTime();

		if (isReRender && player.getId() == this.lastRenderedInstance)
			return;

		if (!mc.isPaused()) {
			animatableManager.updatedAt(currentFrameTime);

			double lastUpdateTime = animatableManager.getLastUpdateTime();
			this.animTime += lastUpdateTime - this.lastGameTickTime;
			this.lastGameTickTime = lastUpdateTime;
		}

		animationState.animationTick = this.animTime;
		this.lastRenderedInstance = player.getId();

		if (fullTick) player.playerAnimLib$getAnimManager().tick(animationState.copy());

		if (!this.getRegisteredBones().isEmpty())
			this.tickAnimation(animatableManager, this.animTime, animationState);
	}

	/**
	 * Build an animation queue for the given {@link RawAnimation}
	 *
	 * @param rawAnimation The raw animation to be compiled
	 * @return A queue of animations and loop types to play
	 */
	public Queue<QueuedAnimation> buildAnimationQueue(RawAnimation rawAnimation) {
		LinkedList<QueuedAnimation> animations = new LinkedList<>();
		boolean error = false;

		for (RawAnimation.Stage stage : rawAnimation.getAnimationStages()) {
			Animation animation = null;

			if (stage.animationID() == RawAnimation.Stage.WAIT) { // This is intentional. Do not change this or T̶s̶l̶a̶t̶ I will be unhappy
				animation = Animation.generateWaitAnimation(stage.additionalTicks());
			}
			else {
				try {
					animation = PlayerAnimCache.getAnimation(stage.animationID());
				}
				catch (RuntimeException ex) {
					ModInit.LOGGER.error("Unable to find animation: " + stage.animationID() + " for " + player.getClass().getSimpleName(), ex);

					error = true;
				}
			}

			if (animation != null)
				animations.add(new QueuedAnimation(animation, stage.loopType()));
		}

		return error ? null : animations;
	}

	/**
	 * Tick and apply transformations to the model based on the current state of the {@link AnimationController}
	 *
	 * @param playerAnimManager		The PlayerAnimManager instance being used for this animation processor
	 * @param animTime              The internal tick counter kept by the {@link PlayerAnimManager} for this player
	 * @param state                 An {@link AnimationState} instance applied to this render frame
	 */
	public void tickAnimation(PlayerAnimManager playerAnimManager, double animTime, AnimationState state) {
		boneSnapshots = updateBoneSnapshots(playerAnimManager.getBoneSnapshotCollection());

		for (PlayerAnimBone entry : this.bones.values()) {
			entry.parent = null;
		}

		for (Pair<Integer, IAnimation> pair : playerAnimManager.getLayers()) {
			IAnimation animation = pair.getRight();

			animation.setupAnim(state.copy());

			for (PlayerAnimBone entry : this.bones.values()) {
				animation.get3DTransform(entry);
			}
		}

		//Todo: Maybe allow mod developers to set this somewhere.
		//Also maybe this should be 0 instead of 1 by default.
		double resetTickLength = 1;

		for (PlayerAnimBone bone : getRegisteredBones()) {
			if (!bone.hasRotationChanged()) {
				BoneSnapshot initialSnapshot = bone.getInitialSnapshot();
				BoneSnapshot saveSnapshot = boneSnapshots.get(bone.getName());

				if (saveSnapshot.isRotAnimInProgress())
					saveSnapshot.stopRotAnim(animTime);

				double percentageReset = Math.min((animTime - saveSnapshot.getLastResetRotationTick()) / resetTickLength, 1);

				bone.setRotX((float)Mth.lerp(percentageReset, saveSnapshot.getRotX(), initialSnapshot.getRotX()));
				bone.setRotY((float)Mth.lerp(percentageReset, saveSnapshot.getRotY(), initialSnapshot.getRotY()));
				bone.setRotZ((float)Mth.lerp(percentageReset, saveSnapshot.getRotZ(), initialSnapshot.getRotZ()));

				if (percentageReset >= 1)
					saveSnapshot.updateRotation(bone.getRotX(), bone.getRotY(), bone.getRotZ());
			}

			if (!bone.hasPositionChanged()) {
				BoneSnapshot initialSnapshot = bone.getInitialSnapshot();
				BoneSnapshot saveSnapshot = boneSnapshots.get(bone.getName());

				if (saveSnapshot.isPosAnimInProgress())
					saveSnapshot.stopPosAnim(animTime);

				double percentageReset = Math.min((animTime - saveSnapshot.getLastResetPositionTick()) / resetTickLength, 1);

				bone.setPosX((float)Mth.lerp(percentageReset, saveSnapshot.getOffsetX(), initialSnapshot.getOffsetX()));
				bone.setPosY((float)Mth.lerp(percentageReset, saveSnapshot.getOffsetY(), initialSnapshot.getOffsetY()));
				bone.setPosZ((float)Mth.lerp(percentageReset, saveSnapshot.getOffsetZ(), initialSnapshot.getOffsetZ()));

				if (percentageReset >= 1)
					saveSnapshot.updateOffset(bone.getPosX(), bone.getPosY(), bone.getPosZ());
			}

			if (!bone.hasScaleChanged()) {
				BoneSnapshot initialSnapshot = bone.getInitialSnapshot();
				BoneSnapshot saveSnapshot = boneSnapshots.get(bone.getName());

				if (saveSnapshot.isScaleAnimInProgress())
					saveSnapshot.stopScaleAnim(animTime);

				double percentageReset = Math.min((animTime - saveSnapshot.getLastResetScaleTick()) / resetTickLength, 1);

				bone.setScaleX((float)Mth.lerp(percentageReset, saveSnapshot.getScaleX(), initialSnapshot.getScaleX()));
				bone.setScaleY((float)Mth.lerp(percentageReset, saveSnapshot.getScaleY(), initialSnapshot.getScaleY()));
				bone.setScaleZ((float)Mth.lerp(percentageReset, saveSnapshot.getScaleZ(), initialSnapshot.getScaleZ()));

				if (percentageReset >= 1)
					saveSnapshot.updateScale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
			}

			if (!bone.hasBendChanged()) {
				BoneSnapshot initialSnapshot = bone.getInitialSnapshot();
				BoneSnapshot saveSnapshot = boneSnapshots.get(bone.getName());

				if (saveSnapshot.isBendAnimInProgress())
					saveSnapshot.stopBendAnim(animTime);

				double percentageReset = Math.min((animTime - saveSnapshot.getLastResetBendTick()) / resetTickLength, 1);

				bone.setBendAxis((float)Mth.lerp(percentageReset, saveSnapshot.getBendAxis(), initialSnapshot.getBendAxis()));
				bone.setBend((float)Mth.lerp(percentageReset, saveSnapshot.getBend(), initialSnapshot.getBend()));

				if (percentageReset >= 1)
					saveSnapshot.updateBend(bone.getBendAxis(), bone.getBend());
			}
		}

		resetBoneTransformationMarkers();
		playerAnimManager.finishFirstTick();
	}

	/**
	 * Reset the transformation markers applied to each {@link PlayerAnimBone} ready for the next render frame
	 */
	private void resetBoneTransformationMarkers() {
		getRegisteredBones().forEach(PlayerAnimBone::resetStateChanges);
	}

	/**
	 * Create new bone {@link BoneSnapshot} based on the bone's initial snapshot for the currently registered {@link PlayerAnimBone PlayerAnimBones},
	 * filtered by the bones already present in the master snapshots map
	 *
	 * @param snapshots The master bone snapshots map from the related {@link PlayerAnimManager}
	 * @return The input snapshots map, for easy assignment
	 */
	private Map<String, BoneSnapshot> updateBoneSnapshots(Map<String, BoneSnapshot> snapshots) {
		for (PlayerAnimBone bone : getRegisteredBones()) {
			if (!snapshots.containsKey(bone.getName()))
				snapshots.put(bone.getName(), new BoneSnapshot(bone.getInitialSnapshot()));
		}

		return snapshots;
	}

	/**
	 * Gets a bone by name
	 *
	 * @param boneName The bone name
	 * @return the bone
	 */
	public PlayerAnimBone getBone(String boneName) {
		return this.bones.get(boneName);
	}

	private void registerPlayerAnimBone(String name) {
		registerPlayerAnimBone(new PlayerAnimBone(name));
	}

	/**
	 * Adds the given bone to the bones list for this processor
	 * <p>
	 * This is normally handled automatically by the mod
	 * <p>
	 * Failure to properly register a bone will break things.
	 */
	private void registerPlayerAnimBone(PlayerAnimBone bone) {
		bone.saveInitialSnapshot();
		this.bones.put(bone.getName(), bone);
	}

	public AbstractClientPlayer getPlayer() {
		return this.player;
	}

	/**
	 * Get an iterable collection of the {@link PlayerAnimBone PlayerAnimBones} currently registered to the processor
	 */
	public Collection<PlayerAnimBone> getRegisteredBones() {
		return this.bones.values();
	}

	/**
	 * {@link Animation} and {@link Animation.LoopType} override pair,
	 * used to define a playable animation stage for a player
	 */
	public record QueuedAnimation(Animation animation, Animation.LoopType loopType) {}
}
