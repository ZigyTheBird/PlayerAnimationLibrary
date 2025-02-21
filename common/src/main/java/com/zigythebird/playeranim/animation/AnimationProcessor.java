package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.animation.keyframe.AnimationPoint;
import com.zigythebird.playeranim.animation.keyframe.BoneAnimationQueue;
import com.zigythebird.playeranim.animation.layered.IAnimation;
import com.zigythebird.playeranim.cache.PlayerAnimBone;
import com.zigythebird.playeranim.cache.PlayerAnimCache;
import com.zigythebird.playeranim.dataticket.DataTickets;
import com.zigythebird.playeranim.math.Pair;
import com.zigythebird.playeranim.math.Vec3f;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

@ApiStatus.Internal
public class AnimationProcessor {
	/**
	 * Don't touch this! I don't see why you would even want to.
	 */
	public static final AnimationProcessor INSTANCE;
	private final Map<String, PlayerAnimBone> bones = new Object2ObjectOpenHashMap<>();

	private double animTime;
	private double lastGameTickTime;
	private long lastRenderedInstance = -1;
	private AbstractClientPlayer player;

	public boolean reloadAnimations = false;

	public AnimationProcessor() {}

	public void saveInitialModelState(ModelPart head, ModelPart torso, ModelPart rightArm, ModelPart leftArm, ModelPart rightLeg, ModelPart leftLeg) {
		this.getBone("head").setInitialSnapshot(head);
		this.getBone("torso").setInitialSnapshot(torso);
		this.getBone("right_arm").setInitialSnapshot(rightArm);
		this.getBone("left_arm").setInitialSnapshot(leftArm);
		this.getBone("right_leg").setInitialSnapshot(rightLeg);
		this.getBone("left_leg").setInitialSnapshot(leftLeg);
	}

	/**
	 * This method is called once per render frame for each player being rendered
	 * <p>
	 * It is an internal method for automated animation parsing.
	 */
	public void handleAnimations(AbstractClientPlayer player, float partialTick) {
		this.player = player;
		Vec3 velocity = player.getDeltaMovement();
		float avgVelocity = (float)((Math.abs(velocity.x) + Math.abs(velocity.z)) / 2f);
		AnimationState animationState = new AnimationState(player, partialTick, avgVelocity >= 0.015F);
		animationState.setData(DataTickets.TICK, (double)player.tickCount);
		animationState.setData(DataTickets.ENTITY, player);

		Minecraft mc = Minecraft.getInstance();
		PlayerAnimManager animatableManager = ((IAnimatedPlayer)player).playerAnimLib$getAnimManager();
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

		if (!this.getRegisteredBones().isEmpty())
			this.tickAnimation(player, animatableManager, this.animTime, animationState, false);
	}

	/**
	 * Build an animation queue for the given {@link RawAnimation}
	 *
	 * @param player The player object being rendered
	 * @param rawAnimation The raw animation to be compiled
	 * @return A queue of animations and loop types to play
	 */
	public Queue<QueuedAnimation> buildAnimationQueue(AbstractClientPlayer player, RawAnimation rawAnimation) {
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
					ModInit.LOGGER.error("Unable to find animation: " + stage.animationID() + " for " + player.getClass().getSimpleName());

					error = true;
					ex.printStackTrace();
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
	 * @param player            The player object relevant to the animation being played
	 * @param playerAnimManager			The PlayerAnimManager instance being used for this animation processor
	 * @param animTime              The internal tick counter kept by the {@link PlayerAnimManager} for this player
	 * @param state                 An {@link AnimationState} instance applied to this render frame
	 * @param crashWhenCantFindBone Whether to crash if unable to find a required bone, or to continue with the remaining bones
	 */
	public void tickAnimation(AbstractClientPlayer player, PlayerAnimManager playerAnimManager, double animTime, AnimationState state, boolean crashWhenCantFindBone) {
		Map<String, BoneSnapshot> boneSnapshots = updateBoneSnapshots(playerAnimManager.getBoneSnapshotCollection());

		for (Pair<Integer, IAnimation> pair : playerAnimManager.getLayers()) {
			IAnimation animation = pair.getRight();
			AnimationController controller = animation instanceof AnimationController ? (AnimationController) animation : null;

			if (controller != null) {
				if (this.reloadAnimations) {
					controller.forceAnimationReset();
					controller.getBoneAnimationQueues().clear();
				}

				controller.isJustStarting = playerAnimManager.isFirstTick();

				controller.clearBoneSnapshots();
				controller.process(state, this.bones, boneSnapshots, animTime, crashWhenCantFindBone);

				for (BoneAnimationQueue boneAnimation : controller.getBoneAnimationQueues().values()) {
					PlayerAnimBone bone = boneAnimation.bone();
					BoneSnapshot snapshot = boneSnapshots.get(bone.getName());
					BoneSnapshot initialSnapshot = bone.getInitialSnapshot();

					AnimationPoint rotXPoint = boneAnimation.rotationXQueue().poll();
					AnimationPoint rotYPoint = boneAnimation.rotationYQueue().poll();
					AnimationPoint rotZPoint = boneAnimation.rotationZQueue().poll();
					AnimationPoint posXPoint = boneAnimation.positionXQueue().poll();
					AnimationPoint posYPoint = boneAnimation.positionYQueue().poll();
					AnimationPoint posZPoint = boneAnimation.positionZQueue().poll();
					AnimationPoint scaleXPoint = boneAnimation.scaleXQueue().poll();
					AnimationPoint scaleYPoint = boneAnimation.scaleYQueue().poll();
					AnimationPoint scaleZPoint = boneAnimation.scaleZQueue().poll();
					AnimationPoint bendAxisPoint = boneAnimation.bendAxisQueue().poll();
					AnimationPoint bendPoint = boneAnimation.bendQueue().poll();
					EasingType easingType = controller.overrideEasingTypeFunction.apply(player);

					if (rotXPoint != null && rotYPoint != null && rotZPoint != null) {
						bone.setRotX((float) EasingType.lerpWithOverride(rotXPoint, easingType) + initialSnapshot.getRotX());
						bone.setRotY((float) EasingType.lerpWithOverride(rotYPoint, easingType) + initialSnapshot.getRotY());
						bone.setRotZ((float) EasingType.lerpWithOverride(rotZPoint, easingType) + initialSnapshot.getRotZ());
						snapshot.updateRotation(bone.getRotX(), bone.getRotY(), bone.getRotZ());
						snapshot.startRotAnim();
						bone.markRotationAsChanged();
					}

					if (posXPoint != null && posYPoint != null && posZPoint != null) {
						bone.setPosX((float) EasingType.lerpWithOverride(posXPoint, easingType));
						bone.setPosY((float) EasingType.lerpWithOverride(posYPoint, easingType));
						bone.setPosZ((float) EasingType.lerpWithOverride(posZPoint, easingType));
						snapshot.updateOffset(bone.getPosX(), bone.getPosY(), bone.getPosZ());
						snapshot.startPosAnim();
						bone.markPositionAsChanged();
					}

					if (scaleXPoint != null && scaleYPoint != null && scaleZPoint != null) {
						bone.setScaleX((float) EasingType.lerpWithOverride(scaleXPoint, easingType));
						bone.setScaleY((float) EasingType.lerpWithOverride(scaleYPoint, easingType));
						bone.setScaleZ((float) EasingType.lerpWithOverride(scaleZPoint, easingType));
						snapshot.updateScale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
						snapshot.startScaleAnim();
						bone.markScaleAsChanged();
					}

					if (bendAxisPoint != null && bendPoint != null) {
						bone.setBendAxis((float) EasingType.lerpWithOverride(bendAxisPoint, easingType));
						bone.setBend((float) EasingType.lerpWithOverride(bendPoint, easingType));
						snapshot.updateBend(bone.getBendAxis(), bone.getBend());
						snapshot.startBendAnim();
						bone.markBendAsChanged();
					}

					controller.addBoneSnapshot(snapshot);
				}
			}

			float tickDelta = state.getPartialTick();
			animation.setupAnim(tickDelta);

			if (animation.shouldGet3DTransform()) {
				for (Map.Entry<String, PlayerAnimBone> entry : this.bones.entrySet()) {
					PlayerAnimBone bone = entry.getValue();
					
					Vec3f pos = new Vec3f(bone.getPosX(), bone.getPosY(), bone.getPosZ());
					pos = animation.get3DTransform(bone.getName(), TransformType.POSITION, tickDelta, pos);
					bone.updatePosition(pos.getX(), pos.getY(), pos.getZ());

					Vec3f rot = new Vec3f(bone.getRotX(), bone.getRotY(), bone.getRotZ());
					rot = animation.get3DTransform(bone.getName(), TransformType.ROTATION, tickDelta, rot);
					bone.updateRotation(rot.getX(), rot.getY(), rot.getZ());

					Vec3f scale = new Vec3f(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
					scale = animation.get3DTransform(bone.getName(), TransformType.SCALE, tickDelta, scale);
					bone.updateScale(scale.getX(), scale.getY(), scale.getZ());

					Vec3f bend = new Vec3f(bone.getBendAxis(), bone.getBend(), 0);
					bend = animation.get3DTransform(bone.getName(), TransformType.BEND, tickDelta, bend);
					bone.updateRotation(bend.getX(), bend.getY(), bend.getZ());
				}
			}
		}

		this.reloadAnimations = false;

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

				double percentageReset = Math.min((animTime - saveSnapshot.getLastResetRotationTick()) / resetTickLength, 1);

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

		for (PlayerAnimBone child : bone.getChildBones()) {
			registerPlayerAnimBone(child);
		}
	}

	public AbstractClientPlayer getLastPlayer() {
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

	static {
		AnimationProcessor animationProcessor = new AnimationProcessor();
		//Todo: Make an event where you can add custom body parts here
		PlayerAnimBone body = new PlayerAnimBone(null, "body");
		new PlayerAnimBone(body, "right_arm");
		new PlayerAnimBone(body, "left_arm");
		new PlayerAnimBone(body, "right_leg");
		new PlayerAnimBone(body, "left_leg");
		new PlayerAnimBone(body, "head");
		new PlayerAnimBone(body, "torso");
		new PlayerAnimBone(body, "right_item");
		new PlayerAnimBone(body, "left_item");
		new PlayerAnimBone(body, "cape");
		new PlayerAnimBone(body, "elytra");
		animationProcessor.registerPlayerAnimBone(body);
		INSTANCE = animationProcessor;
	}
}
