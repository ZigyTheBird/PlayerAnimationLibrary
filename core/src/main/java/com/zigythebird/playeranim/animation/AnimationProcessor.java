package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.animation.layered.AnimationStack;
import com.zigythebird.playeranim.animation.layered.IAnimation;
import com.zigythebird.playeranim.bones.PlayerAnimBone;
import com.zigythebird.playeranim.enums.AnimationStage;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * DO NOT TOUCH THIS UNLESS YOU REALLY KNOW WHAT YOU'RE DOING.
 */
@ApiStatus.Internal
public abstract class AnimationProcessor {
	private final Map<String, PlayerAnimBone> bones = new Object2ObjectOpenHashMap<>();

	protected float animTime;
	protected float lastGameTickTime;
	protected long lastRenderedInstance = -1;

	/**
	 * Each AnimationProcessor must be bound to a player
	 */
	public AnimationProcessor() {
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
	public abstract void handleAnimations(float partialTick, boolean fullTick);

	/**
	 * Build an animation queue for the given {@link RawAnimation}
	 *
	 * @param rawAnimation The raw animation to be compiled
	 * @return A queue of animations and loop types to play
	 */
	public Queue<QueuedAnimation> buildAnimationQueue(RawAnimation rawAnimation) {
		LinkedList<QueuedAnimation> animations = new LinkedList<>();
		for (RawAnimation.Stage stage : rawAnimation.getAnimationStages()) {
			Animation animation;
			if (stage.stage() == AnimationStage.WAIT) { // This is intentional. Do not change this or T̶s̶l̶a̶t̶ I will be unhappy
				animation = Animation.generateWaitAnimation(stage.additionalTicks());
			} else {
				animation = stage.animation();
			}

			if (animation != null) animations.add(new QueuedAnimation(animation, stage.loopType()));
		}
		return animations;
	}

	/**
	 * Tick and apply transformations to the model based on the current state of the {@link AnimationController}
	 *
	 * @param playerAnimManager		The PlayerAnimManager instance being used for this animation processor
	 * @param state                 An {@link AnimationData} instance applied to this render frame
	 */
	public void tickAnimation(AnimationStack playerAnimManager, AnimationData state) {
		for (PlayerAnimBone entry : this.bones.values()) {
			entry.parent = null;
		}

		playerAnimManager.getLayers().removeIf(pair -> pair.right() == null || pair.right().canRemove());
		for (Pair<Integer, IAnimation> pair : playerAnimManager.getLayers()) {
			IAnimation animation = pair.right();

			if (animation.isActive())
				animation.setupAnim(state.copy());
		}

		// TODO playerAnimManager.finishFirstTick();
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
