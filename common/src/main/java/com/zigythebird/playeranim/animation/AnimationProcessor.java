package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.animation.layered.IAnimation;
import com.zigythebird.playeranim.cache.bones.BoneSnapshot;
import com.zigythebird.playeranim.cache.bones.PlayerAnimBone;
import com.zigythebird.playeranim.cache.PlayerAnimCache;
import com.zigythebird.playeranim.math.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
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
		AnimationData animationData = new AnimationData(player, partialTick, (Math.abs(velocity.x) + Math.abs(velocity.z)) / 2f);

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

		animationData.setAnimationTick(this.animTime);
		this.lastRenderedInstance = player.getId();

		if (fullTick) player.playerAnimLib$getAnimManager().tick(animationData.copy());

		if (!this.getRegisteredBones().isEmpty())
			this.tickAnimation(animatableManager, animationData);
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
	 * @param state                 An {@link AnimationData} instance applied to this render frame
	 */
	public void tickAnimation(PlayerAnimManager playerAnimManager, AnimationData state) {
		boneSnapshots = updateBoneSnapshots(playerAnimManager.getBoneSnapshotCollection());

		for (PlayerAnimBone entry : this.bones.values()) {
			entry.parent = null;
		}

		for (Pair<Integer, IAnimation> pair : playerAnimManager.getLayers()) {
			IAnimation animation = pair.getRight();

			animation.setupAnim(state.copy());
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
				snapshots.put(bone.getName(), new BoneSnapshot(bone, true));
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
