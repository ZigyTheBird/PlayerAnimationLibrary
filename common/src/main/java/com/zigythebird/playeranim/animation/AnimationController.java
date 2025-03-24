package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.animation.keyframe.*;
import com.zigythebird.playeranim.animation.keyframe.event.CustomInstructionKeyframeEvent;
import com.zigythebird.playeranim.animation.keyframe.event.ParticleKeyframeEvent;
import com.zigythebird.playeranim.animation.keyframe.event.SoundKeyframeEvent;
import com.zigythebird.playeranim.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranim.animation.keyframe.event.data.KeyFrameData;
import com.zigythebird.playeranim.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranim.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranim.animation.layered.AnimationContainer;
import com.zigythebird.playeranim.animation.layered.IAnimation;
import com.zigythebird.playeranim.animation.layered.modifier.AbstractModifier;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranim.cache.PlayerAnimBone;
import com.zigythebird.playeranim.molang.MolangLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.MochaEngine;

import java.util.*;
import java.util.function.Function;

/**
 * The actual controller that handles the playing and usage of animations, including their various keyframes and instruction markers
 * <p>
 * Each controller can only play a single animation at a time - for example you may have one controller to animate walking,
 * one to control attacks, one to control size, etc.
 */
public class AnimationController implements IAnimation {
	protected final AbstractClientPlayer player;
	protected final ResourceLocation id;
	protected final AnimationStateHandler stateHandler;
	protected final Map<String, BoneAnimationQueue> boneAnimationQueues = new Object2ObjectOpenHashMap<>();
	protected final Map<String, BoneSnapshot> boneSnapshots = new Object2ObjectOpenHashMap<>();
	protected final Map<String, PlayerAnimBone> bones = new Object2ObjectOpenHashMap<>();
	protected Queue<AnimationProcessor.QueuedAnimation> animationQueue = new LinkedList<>();
	protected final MochaEngine<AnimationController> molangRuntime;

	protected boolean isJustStarting = false;
	protected boolean needsAnimationReload = false;
	protected boolean shouldResetTick = false;
	private boolean justStopped = true;
	protected boolean justStartedTransition = false;

	//Todo: Make default implementations of these.
	//For example a default sound keyframe handler that can play note block sounds + existing MC sounds.
	//Check out the AutoPlayingSoundKeyframeHandler class
	protected SoundKeyframeHandler soundKeyframeHandler = null;
	protected ParticleKeyframeHandler particleKeyframeHandler = null;
	protected CustomKeyframeHandler customKeyframeHandler = null;

	protected RawAnimation triggeredAnimation = null;
	protected boolean handlingTriggeredAnimations = false;

	protected double transitionLength;
	protected RawAnimation currentRawAnimation;
	protected AnimationProcessor.QueuedAnimation currentAnimation;
	protected double animTime;
	protected State animationState = State.STOPPED;
	protected double tickOffset;
	protected double lastPollTime = -1;
	protected Function<AbstractClientPlayer, Double> animationSpeedModifier = animatable -> 1d;
	protected Function<AbstractClientPlayer, EasingType> overrideEasingTypeFunction = animatable -> null;
	private final Set<KeyFrameData> executedKeyFrames = new ObjectOpenHashSet<>();
	protected AnimationData animationData;
	
	protected Function<AbstractClientPlayer, FirstPersonMode> firstPersonMode = null;
	protected Function<AbstractClientPlayer, FirstPersonConfiguration> firstPersonConfiguration = null;
	private final List<AbstractModifier> modifiers = new ArrayList<>();

	private final InternalAnimationAccessor internalAnimationAccessor = new InternalAnimationAccessor(this);

	/**
	 * Instantiates a new {@code AnimationController}
	 * <p>
	 * This constructor assumes a 0-tick transition length between animations
	 *
	 * @param player The object that will be animated by this controller
	 * @param id The name of the controller - should represent what animations it handles
	 * @param animationHandler The {@link AnimationStateHandler} animation state handler responsible for deciding which animations to play
	 */
	public AnimationController(AbstractClientPlayer player, ResourceLocation id, AnimationStateHandler animationHandler) {
		this(player, id, 0, animationHandler);
	}

	/**
	 * Instantiates a new {@code AnimationController}
	 *
	 * @param player The object that will be animated by this controller
	 * @param id The name of the controller - should represent what animations it handles
	 * @param transitionTickTime The amount of time (in <b>ticks</b>) that the controller should take to transition between animations.
	 *                              Lerping is automatically applied where possible
	 * @param animationHandler The {@link AnimationStateHandler} animation state handler responsible for deciding which animations to play
	 */
	public AnimationController(AbstractClientPlayer player, ResourceLocation id, int transitionTickTime, AnimationStateHandler animationHandler) {
		this.player = player;
		this.id = id;
		this.transitionLength = transitionTickTime;
		this.stateHandler = animationHandler;
		this.molangRuntime = MolangLoader.createNewEngine(this);

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
	 * Applies the given {@link SoundKeyframeHandler} to this controller, for handling {@link SoundKeyframeEvent sound keyframe instructions}
	 *
	 * @return this
	 */
	public AnimationController setSoundKeyframeHandler(SoundKeyframeHandler soundHandler) {
		this.soundKeyframeHandler = soundHandler;

		return this;
	}

	/**
	 * Applies the given {@link ParticleKeyframeHandler} to this controller, for handling {@link ParticleKeyframeEvent particle keyframe instructions}
	 *
	 * @return this
	 */
	public AnimationController setParticleKeyframeHandler(ParticleKeyframeHandler particleHandler) {
		this.particleKeyframeHandler = particleHandler;

		return this;
	}

	/**
	 * Applies the given {@link CustomKeyframeHandler} to this controller, for handling {@link CustomInstructionKeyframeEvent sound keyframe instructions}
	 *
	 * @return this
	 */
	public AnimationController setCustomInstructionKeyframeHandler(CustomKeyframeHandler customInstructionHandler) {
		this.customKeyframeHandler = customInstructionHandler;

		return this;
	}

	/**
	 * Applies the given modifier function to this controller, for handling the speed that the controller should play its animations at
	 * <p>
	 * An output value of 1 is considered neutral, with 2 playing an animation twice as fast, 0.5 playing half as fast, etc
	 *
	 * @param speedModFunction The function to apply to this controller to handle animation speed
	 * @return this
	 */
	public AnimationController setAnimationSpeedHandler(Function<AbstractClientPlayer, Double> speedModFunction) {
		this.animationSpeedModifier = speedModFunction;

		return this;
	}

	/**
	 * Applies the given modifier value to this controller, for handlign the speed that the controller hsould play its animations at
	 * <p>
	 * A value of 1 is considered neutral, with 2 playing an animation twice as fast, 0.5 playing half as fast, etc
	 *
	 * @param speed The speed modifier to apply to this controller to handle animation speed.
	 * @return this
	 */
	public AnimationController setAnimationSpeed(double speed) {
		return setAnimationSpeedHandler(animatable -> speed);
	}

	/**
	 * Sets the controller's {@link EasingType} override for animations
	 * <p>
	 * By default, the controller will use whatever {@code EasingType} was defined in the animation json
	 *
	 * @param easingTypeFunction The new {@code EasingType} to use
	 * @return this
	 */
	public AnimationController setOverrideEasingType(EasingType easingTypeFunction) {
		return setOverrideEasingTypeFunction(animatable -> easingTypeFunction);
	}

	/**
	 * Sets the controller's {@link EasingType} override function for animations
	 * <p>
	 * By default, the controller will use whatever {@code EasingType} was defined in the animation json
	 *
	 * @param easingType The new {@code EasingType} to use
	 * @return this
	 */
	public AnimationController setOverrideEasingTypeFunction(Function<AbstractClientPlayer, EasingType> easingType) {
		this.overrideEasingTypeFunction = easingType;

		return this;
	}

	/**
	 * Tells the AnimationController that you want to receive the {@link AnimationController.AnimationStateHandler} while a triggered animation is playing
	 * <p>
	 * This has no effect if no triggered animation has been registered, or one isn't currently playing
	 * <p>
	 * If a triggered animation is playing, it can be checked in your AnimationStateHandler via {@link #isPlayingTriggeredAnimation()}
	 */
	public AnimationController receiveTriggeredAnimations() {
		this.handlingTriggeredAnimations = true;

		return this;
	}

	public ResourceLocation getId() {
		return this.id;
	}

	/**
	 * Gets the currently loaded {@link Animation}, if present
	 * <p>
	 * An animation returned here does not guarantee it is currently playing, just that it is the currently loaded animation for this controller
	 */
	@Nullable
	public AnimationProcessor.QueuedAnimation getCurrentAnimation() {
		return this.currentAnimation;
	}

	/**
	 * Gets the currently playing {@link RawAnimation triggered animation}, if present
	 */
	@Nullable
	public RawAnimation getTriggeredAnimation() {
		return this.triggeredAnimation;
	}

	/**
	 * Returns the current state of this controller.
	 */
	public @NotNull State getAnimationState() {
		return this.animationState;
	}

	@Override
	public boolean isActive() {
		return this.animationState.isActive();
	}

	public AnimationData getAnimationData() {
		return animationData;
	}

	public AbstractClientPlayer getPlayer() {
		return this.player;
	}

	/**
	 * Gets the currently loaded animation's {@link BoneAnimationQueue BoneAnimationQueues}.
	 */
	public Map<String, BoneAnimationQueue> getBoneAnimationQueues() {
		return this.boneAnimationQueues;
	}

	/**
	 * Gets the current animation speed modifier
	 * <p>
	 * This modifier defines the relative speed in which animations will be played based on the current state of the game
	 *
	 * @return The computed current animation speed modifier
	 */
	public double getAnimationSpeed() {
		return this.animationSpeedModifier.apply(this.player);
	}

	/**
	 * Marks the controller as needing to reset its animation and state the next time {@link #setAnimation(RawAnimation)} is called
	 * <p>
	 * Use this if you have a {@link RawAnimation} with multiple stages and you want it to start again from the first stage, or if you want to reset the currently playing animation to the start
	 */
	public void forceAnimationReset() {
		this.needsAnimationReload = true;
	}

	/**
	 * Tells the controller to stop all animations until told otherwise
	 * <p>
	 * Calling this will prevent the controller from continuing to play the currently loaded animation until
	 * either {@link #forceAnimationReset()} is called, or
	 * {@link #setAnimation(RawAnimation)} is called with a different animation
	 */
	public void stop() {
		this.animationState = State.STOPPED;
	}

	/**
	 * Overrides the animation transition time for the controller
	 */
	public AnimationController transitionLength(int ticks) {
		this.transitionLength = ticks;

		return this;
	}

	/**
	 * Checks whether the last animation that was playing on this controller has finished or not
	 * <p>
	 * This will return true if the controller has had an animation set previously, and it has finished playing
	 * and isn't going to loop or proceed to another animation
	 *
	 * @return Whether the previous animation finished or not
	 */
	public boolean hasAnimationFinished() {
		return this.currentRawAnimation != null && this.animationState == State.STOPPED;
	}

	/**
	 * Returns the currently cached {@link RawAnimation}
	 * <p>
	 * This animation may or may not still be playing, but it is the last one to be set in {@link #setAnimation}
	 */
	public RawAnimation getCurrentRawAnimation() {
		return this.currentRawAnimation;
	}

	/**
	 * Used for custom handling if {@link #receiveTriggeredAnimations()} was marked
	 */
	public boolean isPlayingTriggeredAnimation() {
		return this.triggeredAnimation != null && !hasAnimationFinished();
	}

	/**
	 * Sets the currently loaded animation to the one provided
	 * <p>
	 * This method may be safely called every render frame, as passing the same builder that is already loaded will do nothing
	 * <p>
	 * Pass null to this method to tell the controller to stop
	 * <p>
	 * If {@link #forceAnimationReset()} has been called prior to this, the controller will reload the animation regardless of whether it matches the currently loaded one or not
	 */
	public void setAnimation(RawAnimation rawAnimation) {
		if (rawAnimation == null || rawAnimation.getAnimationStages().isEmpty()) {
			stop();

			return;
		}

		if (this.needsAnimationReload || !rawAnimation.equals(this.currentRawAnimation)) {
			Queue<AnimationProcessor.QueuedAnimation> animations = this.player.playerAnimLib$getAnimProcessor().buildAnimationQueue(rawAnimation);

			if (animations != null) {
				this.animationQueue = animations;
				this.currentRawAnimation = rawAnimation;
				this.shouldResetTick = true;
				this.animationState = State.TRANSITIONING;
				this.justStartedTransition = true;
				this.needsAnimationReload = false;

				return;
			}

			stop();
		}
	}

	public void tryTriggerAnimation(RawAnimation anim) {
		if (anim == null)
			return;

		stop();
		this.triggeredAnimation = anim;

		if (this.animationState == State.STOPPED) {
			this.animationState = State.TRANSITIONING;
			this.shouldResetTick = true;
			this.justStartedTransition = true;
		}
	}

	/**
	 * Stops and removes a previously triggered animation, effectively ending it immediately.
	 *
	 * @return true if a triggered animation was stopped
	 */
	protected boolean stopTriggeredAnimation() {
		if (this.triggeredAnimation == null)
			return false;

		if (this.currentRawAnimation == this.triggeredAnimation) {
			this.currentAnimation = null;
			this.currentRawAnimation = null;
		}

		this.triggeredAnimation = null;
		this.needsAnimationReload = true;

		return true;
	}

	/**
	 * Handle a given AnimationData, alongside the current triggered animation if applicable
	 */
	protected PlayState handleAnimationState(AnimationData state) {
		if (this.triggeredAnimation != null) {
			if (this.currentRawAnimation != this.triggeredAnimation)
				this.currentAnimation = null;

			setAnimation(this.triggeredAnimation);

			if (!hasAnimationFinished() && (!this.handlingTriggeredAnimations || this.stateHandler.handle(state) == PlayState.CONTINUE))
				return PlayState.CONTINUE;

			this.triggeredAnimation = null;
			this.needsAnimationReload = true;
		}

		return this.stateHandler.handle(state);
	}

	/**
	 * This method is called every frame in order to populate the animation point
	 * queues, and process animation state logic
	 *
	 * @param state                 The animation test state
	 * @param bones                 The registered {@link PlayerAnimBone bones} for this model
	 * @param snapshots             The {@link BoneSnapshot} map
	 * @param seekTime              The current tick + partial tick
	 * @param crashWhenCantFindBone Whether to hard-fail when a bone can't be found, or to continue with the remaining bones
	 */
	public void process(AnimationData state, Map<String, PlayerAnimBone> bones, Map<String, BoneSnapshot> snapshots, final double seekTime, boolean crashWhenCantFindBone) {
		double adjustedTick = adjustTick(seekTime);
		boolean doneTransitioning = hasBeginTick() ? (double) this.currentAnimation.animation().data().get("beginTick") <= adjustedTick : this.transitionLength <= adjustedTick;

		if (animationState == State.TRANSITIONING && doneTransitioning) {
			this.shouldResetTick = true;
			this.animationState = State.RUNNING;
			adjustedTick = adjustTick(seekTime);
		}

		PlayState playState = handleAnimationState(state);

		if (playState == PlayState.STOP || (this.currentAnimation == null && this.animationQueue.isEmpty())) {
			this.animationState = State.STOPPED;
			this.justStopped = true;

			return;
		}

		createInitialQueues(bones.values());

		if (this.justStartedTransition && (this.shouldResetTick || this.justStopped)) {
			this.justStopped = false;
			adjustedTick = adjustTick(seekTime);

			if (this.currentAnimation == null)
				this.animationState = State.TRANSITIONING;
		}
		else if (this.currentAnimation == null) {
			this.shouldResetTick = true;
			this.animationState = State.TRANSITIONING;
			this.justStartedTransition = true;
			this.needsAnimationReload = false;
			adjustedTick = adjustTick(seekTime);
		}
		else if (this.animationState != State.TRANSITIONING) {
			this.animationState = State.RUNNING;
		}

		if (getAnimationState() == State.RUNNING) {
			addBonesToQueue(this.currentAnimation.animation().bones().values());
			processCurrentAnimation(adjustedTick, seekTime, crashWhenCantFindBone, state);
		}
		else if (this.animationState == State.TRANSITIONING) {
			if (this.lastPollTime != seekTime && (adjustedTick == 0 || this.isJustStarting)) {
				this.justStartedTransition = false;
				this.lastPollTime = seekTime;
				this.currentAnimation = this.animationQueue.poll();

				resetEventKeyFrames();

				if (this.currentAnimation == null)
					return;

				saveSnapshotsForAnimation(this.currentAnimation, snapshots);
			}

			if (this.currentAnimation != null) {
				this.animTime = 0;
				addBonesToQueue(this.currentAnimation.animation().bones().values());

				for (BoneAnimation boneAnimation : this.currentAnimation.animation().boneAnimations()) {
					BoneAnimationQueue boneAnimationQueue = this.boneAnimationQueues.get(boneAnimation.boneName());
					BoneSnapshot boneSnapshot = this.boneSnapshots.get(boneAnimation.boneName());
					PlayerAnimBone bone = bones.get(boneAnimation.boneName());

					if (boneSnapshot == null)
						continue;

					if (bone == null) {
						if (crashWhenCantFindBone)
							throw new RuntimeException("Could not find bone: " + boneAnimation.boneName());

						continue;
					}

					KeyframeStack<Keyframe> rotationKeyFrames = boneAnimation.rotationKeyFrames();
					KeyframeStack<Keyframe> positionKeyFrames = boneAnimation.positionKeyFrames();
					KeyframeStack<Keyframe> scaleKeyFrames = boneAnimation.scaleKeyFrames();
					KeyframeStack<Keyframe> bendKeyFrames = boneAnimation.bendKeyFrames();

					if (rotationKeyFrames.hasKeyframes()) {
						boneAnimationQueue.addNextRotation(null, adjustedTick, this.transitionLength, boneSnapshot, bone.getInitialSnapshot(),
								getAnimationPointAtTick(rotationKeyFrames.xKeyframes(), 0, TransformType.ROTATION, Axis.X),
								getAnimationPointAtTick(rotationKeyFrames.yKeyframes(), 0, TransformType.ROTATION, Axis.Y),
								getAnimationPointAtTick(rotationKeyFrames.zKeyframes(), 0, TransformType.ROTATION, Axis.Z));
					}

					if (positionKeyFrames.hasKeyframes()) {
						boneAnimationQueue.addNextPosition(null, adjustedTick, this.transitionLength, boneSnapshot,
								getAnimationPointAtTick(positionKeyFrames.xKeyframes(), 0, TransformType.POSITION, Axis.X),
								getAnimationPointAtTick(positionKeyFrames.yKeyframes(), 0, TransformType.POSITION, Axis.Y),
								getAnimationPointAtTick(positionKeyFrames.zKeyframes(), 0, TransformType.POSITION, Axis.Z));
					}

					if (scaleKeyFrames.hasKeyframes()) {
						boneAnimationQueue.addNextScale(null, adjustedTick, this.transitionLength, boneSnapshot,
								getAnimationPointAtTick(scaleKeyFrames.xKeyframes(), 0, TransformType.SCALE, Axis.X),
								getAnimationPointAtTick(scaleKeyFrames.yKeyframes(), 0, TransformType.SCALE, Axis.Y),
								getAnimationPointAtTick(scaleKeyFrames.zKeyframes(), 0, TransformType.SCALE, Axis.Z));
					}

					if (bendKeyFrames.hasKeyframes()) {
						boneAnimationQueue.addNextBend(null, adjustedTick, this.transitionLength, boneSnapshot, bone.getInitialSnapshot(),
								getAnimationPointAtTick(scaleKeyFrames.xKeyframes(), 0, TransformType.BEND, Axis.X),
								getAnimationPointAtTick(scaleKeyFrames.yKeyframes(), 0, TransformType.BEND, Axis.Y));
					}
				}
			}
		}
	}

	/**
	 * Handle the current animation's state modifications and translations
	 *
	 * @param adjustedTick The controller-adjusted tick for animation purposes
	 * @param seekTime The lerped tick (current tick + partial tick)
	 * @param crashWhenCantFindBone Whether the controller should throw an exception when unable to find the required bone, or continue with the remaining bones
	 */
	private void processCurrentAnimation(double adjustedTick, double seekTime, boolean crashWhenCantFindBone, AnimationData animationData) {
		if (adjustedTick >= this.currentAnimation.animation().length() || (this.currentAnimation.animation().data().has("endTick") &&
				adjustedTick >= (double)this.currentAnimation.animation().data().get("endTick"))) {
			if (this.currentAnimation.loopType().shouldPlayAgain(this.player, this, this.currentAnimation.animation())) {
				if (this.animationState != State.PAUSED) {
					this.shouldResetTick = true;

					adjustedTick = adjustTick(seekTime) + this.currentAnimation.animation().loopType().restartFromTick(player, this, this.currentAnimation.animation());
					resetEventKeyFrames();
				}
			}
			else {
				AnimationProcessor.QueuedAnimation nextAnimation = this.animationQueue.peek();

				resetEventKeyFrames();

				if (nextAnimation == null) {
					this.animationState = State.STOPPED;

					return;
				}
				else {
					this.animationState = State.TRANSITIONING;
					this.shouldResetTick = true;
					adjustedTick = adjustTick(seekTime);
					this.currentAnimation = this.animationQueue.poll();
				}
			}
		}

		final double finalAdjustedTick = adjustedTick;

		this.animTime = (float) (finalAdjustedTick / 20d);

		for (BoneAnimation boneAnimation : this.currentAnimation.animation().boneAnimations()) {
			BoneAnimationQueue boneAnimationQueue = this.boneAnimationQueues.get(boneAnimation.boneName());

			if (boneAnimationQueue == null) {
				if (crashWhenCantFindBone)
					throw new RuntimeException("Could not find bone: " + boneAnimation.boneName());

				continue;
			}

			KeyframeStack<Keyframe> rotationKeyFrames = boneAnimation.rotationKeyFrames();
			KeyframeStack<Keyframe> positionKeyFrames = boneAnimation.positionKeyFrames();
			KeyframeStack<Keyframe> scaleKeyFrames = boneAnimation.scaleKeyFrames();
			KeyframeStack<Keyframe> bendKeyFrames = boneAnimation.bendKeyFrames();

			if (rotationKeyFrames.hasKeyframes()) {
				boneAnimationQueue.addRotations(
						getAnimationPointAtTick(rotationKeyFrames.xKeyframes(), adjustedTick, TransformType.ROTATION, Axis.X),
						getAnimationPointAtTick(rotationKeyFrames.yKeyframes(), adjustedTick, TransformType.ROTATION, Axis.Y),
						getAnimationPointAtTick(rotationKeyFrames.zKeyframes(), adjustedTick, TransformType.ROTATION, Axis.Z));
			}

			if (positionKeyFrames.hasKeyframes()) {
				boneAnimationQueue.addPositions(
						getAnimationPointAtTick(positionKeyFrames.xKeyframes(), adjustedTick, TransformType.POSITION, Axis.X),
						getAnimationPointAtTick(positionKeyFrames.yKeyframes(), adjustedTick, TransformType.POSITION, Axis.Y),
						getAnimationPointAtTick(positionKeyFrames.zKeyframes(), adjustedTick, TransformType.POSITION, Axis.Z));
			}

			if (scaleKeyFrames.hasKeyframes()) {
				boneAnimationQueue.addScales(
						getAnimationPointAtTick(scaleKeyFrames.xKeyframes(), adjustedTick, TransformType.SCALE, Axis.X),
						getAnimationPointAtTick(scaleKeyFrames.yKeyframes(), adjustedTick, TransformType.SCALE, Axis.Y),
						getAnimationPointAtTick(scaleKeyFrames.zKeyframes(), adjustedTick, TransformType.SCALE, Axis.Z));
			}

			if (bendKeyFrames.hasKeyframes()) {
				boneAnimationQueue.addBends(
						getAnimationPointAtTick(bendKeyFrames.xKeyframes(), adjustedTick, TransformType.BEND, Axis.X),
						getAnimationPointAtTick(bendKeyFrames.yKeyframes(), adjustedTick, TransformType.BEND, Axis.Y));
			}
		}

		adjustedTick += hasBeginTick() ? (double) this.currentAnimation.animation().data().get("beginTick") : this.transitionLength;

		for (SoundKeyframeData keyframeData : this.currentAnimation.animation().keyFrames().sounds()) {
			if (adjustedTick >= keyframeData.getStartTick() && this.executedKeyFrames.add(keyframeData)) {
				if (this.soundKeyframeHandler == null) {
					ModInit.LOGGER.warn("Sound Keyframe found for " + this.player.getClass().getSimpleName() + " -> " + getId() + ", but no keyframe handler registered");

					break;
				}

				this.soundKeyframeHandler.handle(new SoundKeyframeEvent(this.player, adjustedTick, this, keyframeData, animationData));
			}
		}

		for (ParticleKeyframeData keyframeData : this.currentAnimation.animation().keyFrames().particles()) {
			if (adjustedTick >= keyframeData.getStartTick() && this.executedKeyFrames.add(keyframeData)) {
				if (this.particleKeyframeHandler == null) {
					ModInit.LOGGER.warn("Particle Keyframe found for " + this.player.getClass().getSimpleName() + " -> " + getId() + ", but no keyframe handler registered");

					break;
				}

				this.particleKeyframeHandler.handle(new ParticleKeyframeEvent(this.player, adjustedTick, this, keyframeData, animationData));
			}
		}

		for (CustomInstructionKeyframeData keyframeData : this.currentAnimation.animation().keyFrames().customInstructions()) {
			if (adjustedTick >= keyframeData.getStartTick() && this.executedKeyFrames.add(keyframeData)) {
				if (this.customKeyframeHandler == null) {
					ModInit.LOGGER.warn("Custom Instruction Keyframe found for " + this.player.getClass().getSimpleName() + " -> " + getId() + ", but no keyframe handler registered");

					break;
				}

				this.customKeyframeHandler.handle(new CustomInstructionKeyframeEvent(this.player, adjustedTick, this, keyframeData, animationData));
			}
		}

		if (this.transitionLength == 0 && this.shouldResetTick && this.animationState == State.TRANSITIONING)
			this.currentAnimation = this.animationQueue.poll();
	}

	/**
	 * Prepare the {@link BoneAnimationQueue} map for the current render frame
	 *
	 * @param boneList The bone list from the {@link AnimationProcessor}
	 */
	private void createInitialQueues(Collection<PlayerAnimBone> boneList) {
		this.boneAnimationQueues.clear();

		addBonesToQueue(boneList);
	}

	private void addBonesToQueue(Collection<PlayerAnimBone> boneList) {
		for (PlayerAnimBone bone : boneList) {
			this.boneAnimationQueues.put(bone.getName(), new BoneAnimationQueue(bone));
		}
	}

	/**
	 * Cache the relevant {@link BoneSnapshot BoneSnapshots} for the current {@link AnimationProcessor.QueuedAnimation}
	 * for animation lerping
	 *
	 * @param animation The {@code QueuedAnimation} to filter {@code BoneSnapshots} for
	 * @param snapshots The master snapshot collection to pull filter from
	 */
	private void saveSnapshotsForAnimation(AnimationProcessor.QueuedAnimation animation, Map<String, BoneSnapshot> snapshots) {
		for (BoneSnapshot snapshot : snapshots.values()) {
			if (animation.animation().boneAnimations() != null) {
				for (BoneAnimation boneAnimation : animation.animation().boneAnimations()) {
					if (boneAnimation.boneName().equals(snapshot.getBone().getName())) {
						this.boneSnapshots.put(boneAnimation.boneName(), new BoneSnapshot(snapshot));

						break;
					}
				}
			}
		}
	}

	/**
	 * Adjust a tick value depending on the controller's current state and speed modifier
	 * <p>
	 * Is used when starting a new animation, transitioning, and a few other key areas
	 *
	 * @param tick The currently used tick value
	 * @return 0 if {@link #shouldResetTick} is set to false, or a {@link #animationSpeedModifier} modified value otherwise
	 */
	protected double adjustTick(double tick) {
		if (!this.shouldResetTick)
			return this.animationSpeedModifier.apply(this.player) * Math.max(tick - this.tickOffset, 0);

		if (getAnimationState() != State.STOPPED)
			this.tickOffset = tick;

		this.shouldResetTick = false;

		return 0;
	}

	public double getAnimTime() {
		return this.animTime;
	}

	public boolean hasBeginTick() {
		return this.currentAnimation != null && this.currentAnimation.animation().data().has("beginTick");
	}

	/**
	 * Convert a {@link KeyframeLocation} to an {@link AnimationPoint}
	 */
	private AnimationPoint getAnimationPointAtTick(List<Keyframe> frames, double tick, TransformType type, Axis axis) {
		KeyframeLocation<Keyframe> location = getCurrentKeyFrameLocation(frames, tick);
		Keyframe currentFrame = location.keyframe();
		double startValue;
		double endValue;

		try {
			startValue = this.molangRuntime.eval(currentFrame.startValue());
			endValue = this.molangRuntime.eval(currentFrame.endValue());
		} catch (Throwable e) {
			ModInit.LOGGER.error("Failed to parse molangs!", e);
			startValue = endValue = type == TransformType.SCALE ? 1 : 0;
		}

		if (type == TransformType.ROTATION) {
			if (!(MolangLoader.isConstant(currentFrame.startValue()))) {
				startValue = Math.toRadians(startValue);
			}

			if (!(MolangLoader.isConstant(currentFrame.endValue()))) {
				endValue = Math.toRadians(endValue);
			}
		}

		return new AnimationPoint(currentFrame, location.startTick(), currentFrame.length(), startValue, endValue);
	}

	/**
	 * Returns the {@link Keyframe} relevant to the current tick time
	 *
	 * @param frames The list of {@code KeyFrames} to filter through
	 * @param ageInTicks The current tick time
	 * @return A new {@code KeyFrameLocation} containing the current {@code KeyFrame} and the tick time used to find it
	 */
	private KeyframeLocation<Keyframe> getCurrentKeyFrameLocation(List<Keyframe> frames, double ageInTicks) {
		if (frames.isEmpty()) {
			return new KeyframeLocation<>(new Keyframe(0), 0);
		}

		double totalFrameTime = 0;

		for (Keyframe frame : frames) {
			totalFrameTime += frame.length();

			if (totalFrameTime > ageInTicks)
				return new KeyframeLocation<>(frame, (ageInTicks - (totalFrameTime - frame.length())));
		}

		return new KeyframeLocation<>(frames.getLast(), ageInTicks);
	}

	/**
	 * Clear the {@link KeyFrameData} cache in preparation for the next animation
	 */
	private void resetEventKeyFrames() {
		this.executedKeyFrames.clear();
	}
	
	public void get3DTransformRaw(@NotNull PlayerAnimBone bone) {
		if (bones.containsKey(bone.getName())) {
			bone.copyOtherBone(bones.get(bone.getName()));
			bone.parent = null;
		}
		if (this.currentAnimation != null) {
			Map<String, String> parents = this.currentAnimation.animation().parents();
			if (parents.containsKey(bone.getName()))
				bone.parent = this.currentAnimation.animation().bones().get(parents.get(bone.getName()));
		}
	}

	@Override
	public void get3DTransform(@NotNull PlayerAnimBone bone) {
		if (!modifiers.isEmpty()) {
			modifiers.get(0).get3DTransform(bone);
		}
		else get3DTransformRaw(bone);
	}

	@Override
	public @NotNull FirstPersonMode getFirstPersonMode() {
		if (firstPersonMode != null) return firstPersonMode.apply(player);
		return FirstPersonMode.NONE;
	}

	@Override
	public @NotNull FirstPersonConfiguration getFirstPersonConfiguration() {
		if (firstPersonConfiguration != null) return firstPersonConfiguration.apply(player);
		return IAnimation.DEFAULT_FIRST_PERSON_CONFIG;
	}
	
	public void setFirstPersonMode(FirstPersonMode mode) {
		firstPersonMode = (player) -> mode;
	}
	
	public void setFirstPersonModeHandler(Function<AbstractClientPlayer, FirstPersonMode> modeHandler) {
		firstPersonMode = modeHandler;
	}

	public void setFirstPersonConfiguration(FirstPersonConfiguration config) {
		firstPersonConfiguration = (player) -> config;
	}

	public void setFirstPersonConfigurationHandler(Function<AbstractClientPlayer, FirstPersonConfiguration> configHandler) {
		firstPersonConfiguration = configHandler;
	}

	@Override
	public void tick(AnimationData state) {
		for (int i = 0; i < modifiers.size(); i++) {
			if (modifiers.get(i).canRemove()) {
				removeModifier(i--);
			}
		}
		if (modifiers.size() > 0) {
			modifiers.get(0).tick(state);
		}
	}

	public void addModifier(@NotNull AbstractModifier modifier, int idx) {
		modifier.setHost(this);
		modifiers.add(idx, modifier);
		linkModifiers();
	}

	@Override
	public void setupAnim(AnimationData state) {
		this.animationData = state;
		if (!modifiers.isEmpty()) {
			modifiers.get(0).setupAnim(state);
		}
		else internalSetupAnim(state);
	}

	protected void internalSetupAnim(AnimationData state) {
		this.isJustStarting = state.getPlayerAnimManager().isFirstTick();
        Map<String, BoneSnapshot> boneSnapshots = new HashMap<>(state.getPlayer().playerAnimLib$getAnimProcessor().boneSnapshots);
		if (this.currentAnimation != null) {
			for (PlayerAnimBone bone : this.currentAnimation.animation().bones().values()) {
				boneSnapshots.put(bone.getName(), new BoneSnapshot(bone.getInitialSnapshot()));
			}
		}

		this.process(state, this.bones, boneSnapshots, state.getPlayer().playerAnimLib$getAnimProcessor().animTime, false);

		for (BoneAnimationQueue boneAnimation : this.getBoneAnimationQueues().values()) {
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
			EasingType easingType = this.overrideEasingTypeFunction.apply(player);

			if (rotXPoint != null && rotYPoint != null && rotZPoint != null) {
				bone.setRotX((float) EasingType.lerpWithOverride(this.molangRuntime, rotXPoint, easingType) + initialSnapshot.getRotX());
				bone.setRotY((float) EasingType.lerpWithOverride(this.molangRuntime, rotYPoint, easingType) + initialSnapshot.getRotY());
				bone.setRotZ((float) EasingType.lerpWithOverride(this.molangRuntime, rotZPoint, easingType) + initialSnapshot.getRotZ());
				snapshot.updateRotation(bone.getRotX(), bone.getRotY(), bone.getRotZ());
				snapshot.startRotAnim();
				bone.markRotationAsChanged();
			}

			if (posXPoint != null && posYPoint != null && posZPoint != null) {
				bone.setPosX((float) EasingType.lerpWithOverride(this.molangRuntime, posXPoint, easingType));
				bone.setPosY((float) EasingType.lerpWithOverride(this.molangRuntime, posYPoint, easingType));
				bone.setPosZ((float) EasingType.lerpWithOverride(this.molangRuntime, posZPoint, easingType));
				snapshot.updateOffset(bone.getPosX(), bone.getPosY(), bone.getPosZ());
				snapshot.startPosAnim();
				bone.markPositionAsChanged();
			}

			if (scaleXPoint != null && scaleYPoint != null && scaleZPoint != null) {
				bone.setScaleX((float) EasingType.lerpWithOverride(this.molangRuntime, scaleXPoint, easingType));
				bone.setScaleY((float) EasingType.lerpWithOverride(this.molangRuntime, scaleYPoint, easingType));
				bone.setScaleZ((float) EasingType.lerpWithOverride(this.molangRuntime, scaleZPoint, easingType));
				snapshot.updateScale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
				snapshot.startScaleAnim();
				bone.markScaleAsChanged();
			}

			if (bendAxisPoint != null && bendPoint != null) {
				bone.setBendAxis((float) EasingType.lerpWithOverride(this.molangRuntime, bendAxisPoint, easingType));
				bone.setBend((float) EasingType.lerpWithOverride(this.molangRuntime, bendPoint, easingType));
				snapshot.updateBend(bone.getBendAxis(), bone.getBend());
				snapshot.startBendAnim();
				bone.markBendAsChanged();
			}
		}
	}

	public void addModifierBefore(@NotNull AbstractModifier modifier) {
		this.addModifier(modifier, 0);
	}

	public void addModifierLast(@NotNull AbstractModifier modifier) {
		this.addModifier(modifier, modifiers.size());
	}

	public void removeModifier(int idx) {
		modifiers.remove(idx);
		linkModifiers();
	}

	protected void linkModifiers() {
		Iterator<AbstractModifier> modifierIterator = modifiers.iterator();
		if (modifierIterator.hasNext()) {
			AbstractModifier tmp = modifierIterator.next();
			while (modifierIterator.hasNext()) {
				AbstractModifier tmp2 = modifierIterator.next();
				tmp.setAnim(tmp2);
				tmp = tmp2;
			}
			tmp.setAnim(internalAnimationAccessor);
		}
	}

	private void registerPlayerAnimBone(String name) {
		registerPlayerAnimBone(new PlayerAnimBone(name));
	}

	/**
	 * Adds the given bone to the bones list for this controller
	 * <p>
	 * This is normally handled automatically by the mod
	 */
	private void registerPlayerAnimBone(PlayerAnimBone bone) {
		bone.saveInitialSnapshot();
		this.bones.put(bone.getName(), bone);
	}

	/**
	 * Every render frame, the {@code AnimationController} will call this handler for <u>each</u> animatable that is being rendered
	 * <p>
	 * This handler defines which animation should be currently playing, and returning a {@link PlayState} to tell the controller what to do next
	 * <p>
	 * Example Usage:
	 * <pre>{@code
	 * AnimationFrameHandler myIdleWalkHandler = state -> {
	 *	if (state.isMoving()) {
	 *		state.getController().setAnimation(myWalkAnimation);
	 *	}
	 *	else {
	 *		state.getController().setAnimation(myIdleAnimation);
	 *	}
	 *
	 *	return PlayState.CONTINUE;
	 *};}</pre>
	 */
	@FunctionalInterface
	public interface AnimationStateHandler {
		/**
		 * The handling method, called each frame
		 * <p>
		 * Return {@link PlayState#CONTINUE} to tell the controller to continue animating,
		 * or return {@link PlayState#STOP} to tell it to stop playing all animations and wait for the next {@link PlayState#CONTINUE} return.
		 */
		PlayState handle(AnimationData state);
	}

	/**
	 * A handler for when a predefined sound keyframe is hit
	 * <p>
	 * When the keyframe is encountered, the {@link SoundKeyframeHandler#handle(SoundKeyframeEvent)} method will be called.
	 * Play the sound(s) of your choice at this time.
	 */
	@FunctionalInterface
	public interface SoundKeyframeHandler {
		void handle(SoundKeyframeEvent event);
	}

	/**
	 * A handler for when a predefined particle keyframe is hit
	 * <p>
	 * When the keyframe is encountered, the {@link ParticleKeyframeHandler#handle(ParticleKeyframeEvent)} method will be called.
	 * Spawn the particles/effects of your choice at this time.
	 */
	@FunctionalInterface
	public interface ParticleKeyframeHandler {
		void handle(ParticleKeyframeEvent event);
	}

	/**
	 * A handler for pre-defined custom instruction keyframes
	 * <p>
	 * When the keyframe is encountered, the {@link CustomKeyframeHandler#handle(CustomInstructionKeyframeEvent)} method will be called.
	 * You can then take whatever action you want at this point.
	 */
	@FunctionalInterface
	public interface CustomKeyframeHandler {
		void handle(CustomInstructionKeyframeEvent event);
	}

	private static class InternalAnimationAccessor extends AnimationContainer<AnimationController> {
		private InternalAnimationAccessor(AnimationController controller) {
			super(controller);
		}

		@Override
		public void setupAnim(AnimationData state) {
			this.anim.internalSetupAnim(state);
		}

		@Override
		public void get3DTransform(@NotNull PlayerAnimBone bone) {
			this.anim.get3DTransformRaw(bone);
		}
	}
}
