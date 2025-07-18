package com.zigythebird.playeranimcore.animation;

import com.zigythebird.playeranimcore.animation.keyframe.*;
import com.zigythebird.playeranimcore.animation.keyframe.event.CustomKeyFrameEvents;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.KeyFrameData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranimcore.animation.layered.AnimationContainer;
import com.zigythebird.playeranimcore.animation.layered.AnimationSnapshot;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractModifier;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.bones.AdvancedBoneSnapshot;
import com.zigythebird.playeranimcore.bones.AdvancedPlayerAnimBone;
import com.zigythebird.playeranimcore.bones.PivotBone;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.enums.AnimationFormat;
import com.zigythebird.playeranimcore.enums.PlayState;
import com.zigythebird.playeranimcore.enums.State;
import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.event.EventResult;
import com.zigythebird.playeranimcore.math.ModMatrix4f;
import com.zigythebird.playeranimcore.math.ModVector4f;
import com.zigythebird.playeranimcore.math.Vec3f;
import com.zigythebird.playeranimcore.molang.MolangLoader;
import com.zigythebird.playeranimcore.util.MatrixUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.MochaEngine;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The actual controller that handles the playing and usage of animations, including their various keyframes and instruction markers
 * <p>
 * Each controller can only play a single animation at a time - for example, you may have one controller to animate walking,
 * one to control attacks, one to control size, etc.
 */
public abstract class AnimationController implements IAnimation {
	public static KeyframeLocation<Keyframe> EMPTY_KEYFRAME_LOCATION = new KeyframeLocation<>(new Keyframe(0), 0, 0);
	
	protected final AnimationStateHandler stateHandler;
	protected final LinkedHashMap<String, BoneAnimationQueue> boneAnimationQueues = new LinkedHashMap<>();
	protected final Map<String, AdvancedPlayerAnimBone> bones = new Object2ObjectOpenHashMap<>();
	protected final Map<String, PlayerAnimBone> activeBones = new Object2ObjectOpenHashMap<>();
	protected final Map<String, PivotBone> pivotBones = new Object2ObjectOpenHashMap<>();
	protected Queue<AnimationProcessor.QueuedAnimation> animationQueue = new LinkedList<>();
	protected final MochaEngine<AnimationController> molangRuntime;

	protected boolean isJustStarting = false;
	protected boolean needsAnimationReload = false;
	protected boolean shouldResetTick = false;

	protected CustomKeyFrameEvents.CustomKeyFrameHandler<SoundKeyframeData> soundKeyframeHandler = null;
	protected CustomKeyFrameEvents.CustomKeyFrameHandler<ParticleKeyframeData> particleKeyframeHandler = null;
	protected CustomKeyFrameEvents.CustomKeyFrameHandler<CustomInstructionKeyframeData> customKeyframeHandler = null;

	protected RawAnimation triggeredAnimation = null;
	protected boolean handlingTriggeredAnimations = false;

	protected RawAnimation currentRawAnimation;
	protected AnimationProcessor.QueuedAnimation currentAnimation;
	protected float animTime;
	protected State animationState = State.STOPPED;
	protected float tickOffset;
	protected float startAnimFrom;
	protected Consumer<Function<String, AdvancedPlayerAnimBone>> postAnimationSetupConsumer = function -> {};
	protected Function<AnimationController, Float> animationSpeedModifier = controller -> 1F;
	protected Function<AnimationController, EasingType> overrideEasingTypeFunction = controller -> null;
	private final Set<KeyFrameData> executedKeyFrames = new ObjectOpenHashSet<>();
	protected AnimationData animationData;

	protected Function<AnimationController, FirstPersonMode> firstPersonMode = null;
	protected Function<AnimationController, FirstPersonConfiguration> firstPersonConfiguration = null;
	private final List<AbstractModifier> modifiers = new ArrayList<>();

	private final InternalAnimationAccessor internalAnimationAccessor = new InternalAnimationAccessor(this);

	/**
	 * Instantiates a new {@code AnimationController}
	 *
	 * @param animationHandler The {@link AnimationStateHandler} animation state handler responsible for deciding which animations to play
	 */
	public AnimationController(AnimationStateHandler animationHandler) {
		this.stateHandler = animationHandler;
		this.molangRuntime = MolangLoader.createNewEngine(this);

		registerBones();
	}

	public abstract void registerBones();

	/**
	 * Applies the given {@link CustomKeyFrameEvents.CustomKeyFrameHandler} to this controller, for handling {@link SoundKeyframeData sound keyframe instructions}
	 */
	public AnimationController setSoundKeyframeHandler(CustomKeyFrameEvents.CustomKeyFrameHandler<SoundKeyframeData> soundHandler) {
		this.soundKeyframeHandler = soundHandler;

		return this;
	}

	/**
	 * Applies the given {@link CustomKeyFrameEvents.CustomKeyFrameHandler} to this controller, for handling {@link ParticleKeyframeData particle keyframe instructions}
	 */
	public AnimationController setParticleKeyframeHandler(CustomKeyFrameEvents.CustomKeyFrameHandler<ParticleKeyframeData> particleHandler) {
		this.particleKeyframeHandler = particleHandler;

		return this;
	}

	/**
	 * Applies the given {@link CustomKeyFrameEvents.CustomKeyFrameHandler} to this controller, for handling {@link CustomInstructionKeyframeData sound keyframe instructions}
	 */
	public AnimationController setCustomInstructionKeyframeHandler(CustomKeyFrameEvents.CustomKeyFrameHandler<CustomInstructionKeyframeData> customInstructionHandler) {
		this.customKeyframeHandler = customInstructionHandler;

		return this;
	}

	/**
	 * Gives you each bone after it has been set up for a new animation.
	 * Useful for disabling certain bone axes, or all of them if you want.
	 */
	public AnimationController setPostAnimationSetupConsumer(Consumer<Function<String, AdvancedPlayerAnimBone>> postAnimationSetupConsumer) {
		this.postAnimationSetupConsumer = postAnimationSetupConsumer;

		return this;
	}

	/**
	 * Applies the given modifier function to this controller, for handling the speed that the controller should play its animations at
	 * <p>
	 * An output value of 1 is considered neutral, with 2 playing an animation twice as fast, 0.5 playing half as fast, etc
	 */
	public AnimationController setAnimationSpeedHandler(Function<AnimationController, Float> speedModFunction) {
		this.animationSpeedModifier = speedModFunction;

		return this;
	}

	/**
	 * Applies the given modifier value to this controller, for handlign the speed that the controller hsould play its animations at
	 * <p>
	 * A value of 1 is considered neutral, with 2 playing an animation twice as fast, 0.5 playing half as fast, etc
	 */
	public AnimationController setAnimationSpeed(float speed) {
		return setAnimationSpeedHandler(animatable -> speed);
	}

	/**
	 * Sets the controller's {@link EasingType} override for animations
	 * This lets you change all the easings in an animation to your desired easing.
	 * By default, the controller will use whatever {@code EasingType} was defined in the animation json
	 */
	public AnimationController setOverrideEasingType(EasingType easingTypeFunction) {
		return setOverrideEasingTypeFunction(animatable -> easingTypeFunction);
	}

	/**
	 * Sets the controller's {@link EasingType} override function for animations
	 * This lets you change all the easings in an animation to your desired easing.
	 * By default, the controller will use whatever {@code EasingType} was defined in the animation json
	 */
	public AnimationController setOverrideEasingTypeFunction(Function<AnimationController, EasingType> easingType) {
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
	public float getAnimationSpeed() {
		return this.animationSpeedModifier.apply(this);
	}

	/**
	 * Marks the controller as needing to reset its animation and state the next time {@link #handleAnimation} is called
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
	 * either {@link #forceAnimationReset()} is called, or a different animation is triggered
	 */
	public void stop() {
		this.animationState = State.STOPPED;
		resetEventKeyFrames();
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
	 * This animation may or may not still be playing, but it is the last one to be set in {@link #handleAnimation}
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
	 * Use {@link #triggerAnimation(RawAnimation)} ()} instead if outside state handler.
	 * Sets the currently loaded animation to the one provided
	 * <p>
	 * This method may be safely called every render frame, as passing the same builder that is already loaded will do nothing
	 * <p>
	 * Pass null to this method to tell the controller to stop
	 * <p>
	 * If {@link #forceAnimationReset()} has been called prior to this, the controller will reload the animation regardless of whether it matches the currently loaded one or not
	 *
	 * @param startAnimFrom Where to start the animation from in ticks
	 */
	protected void setAnimation(RawAnimation rawAnimation, float startAnimFrom) {
		if (rawAnimation == null || rawAnimation.getAnimationStages().isEmpty()) {
			stop();

			return;
		}

		if (this.needsAnimationReload || !rawAnimation.equals(this.currentRawAnimation)) {
			Queue<AnimationProcessor.QueuedAnimation> animations = getQueuedAnimations(rawAnimation);

			if (animations != null) {
				this.animationQueue = animations;
				this.currentRawAnimation = rawAnimation;
				this.startAnimFrom = startAnimFrom;
				this.shouldResetTick = true;
				this.animationState = State.RUNNING;
				this.currentAnimation = this.animationQueue.poll();
				setupNewAnimation();
				this.needsAnimationReload = false;

				return;
			}

			stop();
		}
	}

	protected void setAnimation(RawAnimation rawAnimation) {
		setAnimation(rawAnimation, 0);
	}

	protected abstract Queue<AnimationProcessor.QueuedAnimation> getQueuedAnimations(RawAnimation rawAnimation);

	/**
	 * Main method used to set the currently playing animation.
	 * @param newAnimation The animation you want to play.
	 * @param startAnimFrom Where to start the animation from in ticks.
	 */
	public void triggerAnimation(RawAnimation newAnimation, float startAnimFrom) {
		if (newAnimation == null)
			return;

		stop();
		this.triggeredAnimation = newAnimation;

		this.needsAnimationReload = true;
		this.animationState = State.RUNNING;
		this.shouldResetTick = true;
		this.startAnimFrom = startAnimFrom;
	}

	public void triggerAnimation(RawAnimation newAnimation) {
		triggerAnimation(newAnimation, 0);
	}

	public void triggerAnimation(Animation newAnimation, float startAnimFrom) {
		triggerAnimation(RawAnimation.begin().then(newAnimation, Animation.LoopType.DEFAULT), startAnimFrom);
	}

	public void triggerAnimation(Animation newAnimation) {
		triggerAnimation(RawAnimation.begin().then(newAnimation, Animation.LoopType.DEFAULT), 0);
	}

	/**
	 * Fade out from current animation into new animation.
	 * Does not fade if there is currently no active animation
	 * @param fadeModifier Fade modifier, use {@link AbstractFadeModifier#standardFadeIn(int, EasingType)} for simple fade.
	 * @param newAnimation The animation you want to play.
	 */
	public void replaceAnimationWithFade(@NotNull AbstractFadeModifier fadeModifier, @Nullable RawAnimation newAnimation) {
		replaceAnimationWithFade(fadeModifier, newAnimation, true);
	}

	/**
	 * Fade out from current to a new animation
	 * @param fadeModifier    Fade modifier, use {@link AbstractFadeModifier#standardFadeIn(int, EasingType)} for simple fade.
	 * @param newAnimation    The animation you want to play.
	 * @param fadeFromNothing Do fade even if we go from nothing. (for KeyframeAnimation, it can be false by default)
	 */
	public void replaceAnimationWithFade(@NotNull AbstractFadeModifier fadeModifier, @Nullable RawAnimation newAnimation, boolean fadeFromNothing) {
		if (fadeFromNothing || this.isActive()) {
			if (this.isActive()) {
				Map<String, AdvancedBoneSnapshot> snapshots = new HashMap<>();
				for (PlayerAnimBone bone : activeBones.values()) {
					snapshots.put(bone.getName(), new AdvancedBoneSnapshot(bone));
				}
				fadeModifier.setTransitionAnimation(new AnimationSnapshot(snapshots));
			}
			addModifierLast(fadeModifier);
		}
		this.triggerAnimation(newAnimation);
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

	protected PlayState handleAnimation(AnimationData state) {
		if (this.triggeredAnimation != null) {
			if (this.currentRawAnimation != this.triggeredAnimation)
				this.currentAnimation = null;

			setAnimation(this.triggeredAnimation, startAnimFrom);

			if (!hasAnimationFinished() && !this.handlingTriggeredAnimations)
				return PlayState.CONTINUE;

			this.triggeredAnimation = null;
			this.needsAnimationReload = true;
		}

		return this.stateHandler.handle(this, state, (animation, startTick) -> {
			this.setAnimation(animation, startTick);
			return PlayState.CONTINUE;
		});
	}

	/**
	 * This method is called every frame in order to populate the animation point
	 * queues, and process animation state logic
	 *
	 * @param state                 The animation test state
	 * @param seekTime              The current tick + partial tick
	 */
	public void process(AnimationData state, final float seekTime) {
		float adjustedTick = adjustTick(seekTime);

		PlayState playState = handleAnimation(state);

		if (playState == PlayState.STOP || (this.currentAnimation == null && this.animationQueue.isEmpty())) {
			this.animationState = State.STOPPED;

            return;
		}

		this.boneAnimationQueues.clear();

		if (getAnimationState() == State.RUNNING) {
			processCurrentAnimation(adjustedTick, seekTime, state);
		}
	}

	/**
	 * Handle the current animation's state modifications and translations
	 *
	 * @param adjustedTick The controller-adjusted tick for animation purposes
	 * @param seekTime The lerped tick (current tick + partial tick)
	 */
	private void processCurrentAnimation(float adjustedTick, float seekTime, AnimationData animationData) {
		Animation animation = this.currentAnimation.animation();

		if (adjustedTick >= animation.length()) {
			if (this.currentAnimation.loopType().shouldPlayAgain(animation)) {
				if (this.animationState != State.PAUSED) {
					this.shouldResetTick = true;
					this.startAnimFrom = this.currentAnimation.loopType().restartFromTick(animation);
					adjustedTick = adjustTick(seekTime);
					resetEventKeyFrames();
				}
			}
			else {
				AnimationProcessor.QueuedAnimation nextAnimation = this.animationQueue.peek();

				resetEventKeyFrames();

				if (nextAnimation == null) {
					this.animationState = State.STOPPED;
					this.currentAnimation = null;
					for (AdvancedPlayerAnimBone bone : this.bones.values()) {
						bone.setToInitialPose();
					}

					return;
				}
				else {
					this.animationState = State.RUNNING;
					this.shouldResetTick = true;
					adjustedTick = adjustTick(seekTime);
					this.currentAnimation = this.animationQueue.poll();
					setupNewAnimation();
				}
			}
		}

		final float finalAdjustedTick = adjustedTick;
		this.animTime = finalAdjustedTick / 20f;

		for (Map.Entry<String, BoneAnimation> entry : animation.boneAnimations().entrySet()) {
			BoneAnimationQueue boneAnimationQueue = this.boneAnimationQueues.computeIfAbsent(entry.getKey(), (name) -> new BoneAnimationQueue(bones.containsKey(name) ? bones.get(name) : this.pivotBones.get(name)));
			AdvancedPlayerAnimBone bone = this.bones.get(entry.getKey());

			BoneAnimation boneAnimation = entry.getValue();
			KeyframeStack rotationKeyFrames = boneAnimation.rotationKeyFrames();
			KeyframeStack positionKeyFrames = boneAnimation.positionKeyFrames();
			KeyframeStack scaleKeyFrames = boneAnimation.scaleKeyFrames();
			List<Keyframe> bendKeyFrames = boneAnimation.bendKeyFrames();

			if (rotationKeyFrames.hasKeyframes()) {
				boneAnimationQueue.addRotations(
						getAnimationPointAtTick(rotationKeyFrames.xKeyframes(), adjustedTick, TransformType.ROTATION, bone != null ? bone::setRotXTransitionLength : null),
						getAnimationPointAtTick(rotationKeyFrames.yKeyframes(), adjustedTick, TransformType.ROTATION, bone != null ? bone::setRotYTransitionLength : null),
						getAnimationPointAtTick(rotationKeyFrames.zKeyframes(), adjustedTick, TransformType.ROTATION, bone != null ? bone::setRotZTransitionLength : null));
			}

			if (positionKeyFrames.hasKeyframes()) {
				boneAnimationQueue.addPositions(
						getAnimationPointAtTick(positionKeyFrames.xKeyframes(), adjustedTick, TransformType.POSITION, bone != null ? bone::setPositionXTransitionLength : null),
						getAnimationPointAtTick(positionKeyFrames.yKeyframes(), adjustedTick, TransformType.POSITION, bone != null ? bone::setPositionYTransitionLength : null),
						getAnimationPointAtTick(positionKeyFrames.zKeyframes(), adjustedTick, TransformType.POSITION, bone != null ? bone::setPositionZTransitionLength : null));
			}

			if (scaleKeyFrames.hasKeyframes()) {
				boneAnimationQueue.addScales(
						getAnimationPointAtTick(scaleKeyFrames.xKeyframes(), adjustedTick, TransformType.SCALE, bone != null ? bone::setScaleXTransitionLength : null),
						getAnimationPointAtTick(scaleKeyFrames.yKeyframes(), adjustedTick, TransformType.SCALE, bone != null ? bone::setScaleYTransitionLength : null),
						getAnimationPointAtTick(scaleKeyFrames.zKeyframes(), adjustedTick, TransformType.SCALE, bone != null ? bone::setScaleZTransitionLength : null));
			}

			if (!bendKeyFrames.isEmpty()) {
				boneAnimationQueue.addBend(getAnimationPointAtTick(bendKeyFrames, adjustedTick, TransformType.BEND, bone != null ? bone::setBendTransitionLength : null));
			}
		}

		this.boneAnimationQueues.entrySet().stream().sorted((o1, o2) -> {
			boolean isMainBone1 = this.bones.containsKey(o1.getKey());
			boolean isMainBone2 = this.bones.containsKey(o2.getKey());
			if (isMainBone1 == isMainBone2) return 0;
			if (isMainBone1) return 1;
			return -1;
		}).forEach(entry -> this.boneAnimationQueues.putLast(entry.getKey(), entry.getValue()));

		handleCustomKeyframe(
				animation.keyFrames().sounds(),
				this.soundKeyframeHandler, CustomKeyFrameEvents.SOUND_KEYFRAME_EVENT.invoker(),
				adjustedTick, animationData
		);

		handleCustomKeyframe(
				animation.keyFrames().particles(),
				this.particleKeyframeHandler, CustomKeyFrameEvents.PARTICLE_KEYFRAME_EVENT.invoker(),
				adjustedTick, animationData
		);

		handleCustomKeyframe(
				animation.keyFrames().customInstructions(),
				this.customKeyframeHandler, CustomKeyFrameEvents.CUSTOM_INSTRUCTION_KEYFRAME_EVENT.invoker(),
				adjustedTick, animationData
		);
	}

	protected  <T extends KeyFrameData> void handleCustomKeyframe(T[] keyframes, @Nullable CustomKeyFrameEvents.CustomKeyFrameHandler<T> main, CustomKeyFrameEvents.CustomKeyFrameHandler<T> event, float animationTick, AnimationData animationData) {
		for (T keyframeData : keyframes) {
			if (animationTick >= keyframeData.getStartTick() && this.executedKeyFrames.add(keyframeData)) {

				EventResult result = main == null ? EventResult.PASS : main.handle(animationTick, this, keyframeData, animationData);
				if (result == EventResult.PASS) {
					result = event.handle(animationTick, this, keyframeData, animationData);
				}

				if (result == EventResult.FAIL) {
					return;
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
	protected float adjustTick(float tick) {
		if (!this.shouldResetTick)
			return this.animationSpeedModifier.apply(this) * Math.max(tick - this.tickOffset, 0) + startAnimFrom;

		if (getAnimationState() != State.STOPPED)
			this.tickOffset = tick;

		this.shouldResetTick = false;

		return startAnimFrom;
	}

	/**
	 * Duration of the animation spent in seconds
	 */
	public float getAnimationTime() {
		return this.animTime;
	}

	/**
	 * Duration of the animation spent in ticks
	 * tick + tick delta
	 */
	public float getAnimationTicks() {
		return this.animTime * 20;
	}

	public boolean hasBeginTick() {
		return this.currentAnimation.animation().data().has(ExtraAnimationData.BEGIN_TICK_KEY);
	}

	public boolean hasEndTick() {
		Animation animation = this.currentAnimation.animation();
		return !animation.loopType().shouldPlayAgain(animation) && animation.data().has(ExtraAnimationData.END_TICK_KEY);
	}

	public boolean isDisableAxisIfNotModified() {
		return this.currentAnimation != null && this.currentAnimation.animation().data().isDisableAxisIfNotModified();
	}

	public boolean isAnimationPlayerAnimatorFormat() {
		return this.currentAnimation != null && this.currentAnimation.animation().data().isAnimationPlayerAnimatorFormat();
	}

	protected void setupNewAnimation() {
		if (currentAnimation == null) return;
		resetEventKeyFrames();
		for (AdvancedPlayerAnimBone bone : bones.values()) {
			bone.setEnabled(currentAnimation.animation().getBone(bone.getName()) != null);
		}
		for (Map.Entry<String, BoneAnimation> entry : currentAnimation.animation().boneAnimations().entrySet()) {
			if (bones.containsKey(entry.getKey())) {
				AdvancedPlayerAnimBone bone = bones.get(entry.getKey());
				if (isDisableAxisIfNotModified()) {
					BoneAnimation boneAnimation = entry.getValue();
					bone.positionXEnabled = !boneAnimation.positionKeyFrames().xKeyframes().isEmpty();
					bone.positionYEnabled = !boneAnimation.positionKeyFrames().yKeyframes().isEmpty();
					bone.positionZEnabled = !boneAnimation.positionKeyFrames().zKeyframes().isEmpty();

					bone.rotXEnabled = !boneAnimation.rotationKeyFrames().xKeyframes().isEmpty();
					bone.rotYEnabled = !boneAnimation.rotationKeyFrames().yKeyframes().isEmpty();
					bone.rotZEnabled = !boneAnimation.rotationKeyFrames().zKeyframes().isEmpty();

					bone.scaleXEnabled = !boneAnimation.scaleKeyFrames().xKeyframes().isEmpty();
					bone.scaleYEnabled = !boneAnimation.scaleKeyFrames().yKeyframes().isEmpty();
					bone.scaleZEnabled = !boneAnimation.scaleKeyFrames().zKeyframes().isEmpty();

					bone.bendEnabled = !boneAnimation.bendKeyFrames().isEmpty();
					
					if (!this.isAnimationPlayerAnimatorFormat()) {
						if (bone.positionXEnabled || bone.positionYEnabled || bone.positionZEnabled)
							bone.setPositionEnabled(true);

						if (bone.rotXEnabled || bone.rotYEnabled || bone.rotZEnabled)
							bone.setRotEnabled(true);

						if (bone.scaleXEnabled || bone.scaleYEnabled || bone.scaleZEnabled)
							bone.setScaleEnabled(true);
					}
				} else bone.setEnabled(true);
			}
		}

		for (String entry : currentAnimation.animation().parents().keySet()) {
			if (this.bones.containsKey(entry)) this.bones.get(entry).setEnabled(true);
		}

		this.pivotBones.clear();
		for (Map.Entry<String, Vec3f> entry : currentAnimation.animation().bones().entrySet()) {
			this.pivotBones.put(entry.getKey(), new PivotBone(entry.getKey(), entry.getValue()));
		}

		this.postAnimationSetupConsumer.accept((name) -> bones.getOrDefault(name, null));
	}

	/**
	 * Convert a {@link KeyframeLocation} to an {@link AnimationPoint}
	 */
	private AnimationPoint getAnimationPointAtTick(List<Keyframe> frames, float tick, TransformType type, Consumer<Float> transitionLengthSetter) {
		Animation animation = this.currentAnimation.animation();
		Animation.LoopType loopType = animation.loopType();
		float endTick = animation.data().<Float>get(ExtraAnimationData.END_TICK_KEY).orElse(animation.length()-1);

		KeyframeLocation<Keyframe> location = getCurrentKeyFrameLocation(frames, tick);
		Keyframe currentFrame = location.keyframe();
		float startValue = this.molangRuntime.eval(currentFrame.startValue());
		float endValue = this.molangRuntime.eval(currentFrame.endValue());

		if (type == TransformType.ROTATION || type == TransformType.BEND) {
			if (!(MolangLoader.isConstant(currentFrame.startValue()))) {
				startValue = (float) Math.toRadians(startValue);
			}

			if (!(MolangLoader.isConstant(currentFrame.endValue()))) {
				endValue = (float) Math.toRadians(endValue);
			}
		}

		if (transitionLengthSetter != null) {
			ExtraAnimationData extraData = animation.data();
			if (hasBeginTick() && !frames.isEmpty() && currentFrame == frames.getFirst() && extraData.<Float>get(ExtraAnimationData.BEGIN_TICK_KEY).get() > tick) {
				startValue = endValue;
				transitionLengthSetter.accept(currentFrame.length());
			} else if (hasEndTick() && !frames.isEmpty() && currentFrame == frames.getLast() && endTick <= tick) {
				transitionLengthSetter.accept(animation.length() - endTick);
			} else transitionLengthSetter.accept(null);
		}

		return new AnimationPoint(currentFrame.easingType(), currentFrame.easingArgs(), location.startTick(), currentFrame.length(), startValue, endValue);
	}

	/**
	 * Returns the {@link Keyframe} relevant to the current tick time
	 *
	 * @param frames The list of {@code KeyFrames} to filter through
	 * @param ageInTicks The current tick time
	 * @return A new {@code KeyFrameLocation} containing the current {@code KeyFrame} and the tick time used to find it
	 */
	private KeyframeLocation<Keyframe> getCurrentKeyFrameLocation(List<Keyframe> frames, float ageInTicks) {
		if (frames.isEmpty())
			return EMPTY_KEYFRAME_LOCATION;

		float totalFrameTime = 0;

		for (Keyframe frame : frames) {
			totalFrameTime += frame.length();

			if (totalFrameTime > ageInTicks) {
				return new KeyframeLocation<>(frame, (ageInTicks - (totalFrameTime - frame.length())), totalFrameTime);
			}
		}

		return new KeyframeLocation<>(frames.getLast(), ageInTicks, totalFrameTime);
	}

	/**
	 * Clear the {@link KeyFrameData} cache in preparation for the next animation
	 */
	protected void resetEventKeyFrames() {
		if (!this.executedKeyFrames.isEmpty()) {
			CustomKeyFrameEvents.RESET_KEYFRAMES_EVENT.invoker().handle(this, this.executedKeyFrames);
		}
		this.executedKeyFrames.clear();
	}

	public PlayerAnimBone get3DTransformRaw(@NotNull PlayerAnimBone bone) {
		if (activeBones.containsKey(bone.getName())) {
			PlayerAnimBone bone1 = activeBones.get(bone.getName());
			if (this.currentAnimation != null && bone1 instanceof AdvancedPlayerAnimBone advancedBone) {
				ExtraAnimationData extraData = this.currentAnimation.animation().data();
				if (hasBeginTick() && extraData.<Float>get(ExtraAnimationData.BEGIN_TICK_KEY).get() > this.getAnimationTicks()) {
					bone.beginOrEndTickLerp(advancedBone, this.getAnimationTicks(), null);
				}
				else if (hasEndTick() && extraData.<Float>get(ExtraAnimationData.END_TICK_KEY).get() <= this.getAnimationTicks()) {
					bone.beginOrEndTickLerp(advancedBone, this.getAnimationTicks() - extraData.<Float>get(ExtraAnimationData.END_TICK_KEY).get(), this.currentAnimation.animation());
				}
				else bone.copyOtherBoneIfNotDisabled(bone1);
			}
			else bone.copyOtherBoneIfNotDisabled(bone1);
		}
		return bone;
	}

	@Override
	public PlayerAnimBone get3DTransform(@NotNull PlayerAnimBone bone) {
		if (!modifiers.isEmpty()) {
			return modifiers.getFirst().get3DTransform(bone);
		}
		return get3DTransformRaw(bone);
	}

	@Override
	public @NotNull FirstPersonMode getFirstPersonMode() {
		if (firstPersonMode != null) return firstPersonMode.apply(this);
		return FirstPersonMode.NONE;
	}

	@Override
	public @NotNull FirstPersonConfiguration getFirstPersonConfiguration() {
		if (firstPersonConfiguration != null) return firstPersonConfiguration.apply(this);
		return IAnimation.DEFAULT_FIRST_PERSON_CONFIG;
	}

	public void setFirstPersonMode(FirstPersonMode mode) {
		firstPersonMode = (controller) -> mode;
	}

	public void setFirstPersonModeHandler(Function<AnimationController, FirstPersonMode> modeHandler) {
		firstPersonMode = modeHandler;
	}

	public void setFirstPersonConfiguration(FirstPersonConfiguration config) {
		firstPersonConfiguration = (controller) -> config;
	}

	public void setFirstPersonConfigurationHandler(Function<AnimationController, FirstPersonConfiguration> configHandler) {
		firstPersonConfiguration = configHandler;
	}

	@Override
	public void tick(AnimationData state) {
		for (int i = 0; i < modifiers.size(); i++) {
			if (modifiers.get(i).canRemove()) {
				removeModifier(i--);
			}
		}
		if (!modifiers.isEmpty()) {
			modifiers.getFirst().tick(state);
		}
		else internalSetupAnim(state);
	}

	@Override
	public void setupAnim(AnimationData state) {
		this.animationData = state;
		if (!modifiers.isEmpty())
			modifiers.getFirst().setupAnim(state);
		else internalSetupAnim(state);
	}

	protected void internalSetupAnim(AnimationData state) {
		this.activeBones.clear();
		for (BoneAnimationQueue boneAnimation : this.getBoneAnimationQueues().values()) {
			PlayerAnimBone bone = boneAnimation.bone();
			if (bone == null) continue;
			this.activeBones.put(bone.getName(), bone);
			bone.setToInitialPose();

			AnimationPoint rotXPoint = boneAnimation.rotationXQueue().poll();
			AnimationPoint rotYPoint = boneAnimation.rotationYQueue().poll();
			AnimationPoint rotZPoint = boneAnimation.rotationZQueue().poll();
			AnimationPoint posXPoint = boneAnimation.positionXQueue().poll();
			AnimationPoint posYPoint = boneAnimation.positionYQueue().poll();
			AnimationPoint posZPoint = boneAnimation.positionZQueue().poll();
			AnimationPoint scaleXPoint = boneAnimation.scaleXQueue().poll();
			AnimationPoint scaleYPoint = boneAnimation.scaleYQueue().poll();
			AnimationPoint scaleZPoint = boneAnimation.scaleZQueue().poll();
			AnimationPoint bendPoint = boneAnimation.bendQueue().poll();
			EasingType easingType = this.overrideEasingTypeFunction.apply(this);

			if (rotXPoint != null) {
				bone.setRotX(EasingType.lerpWithOverride(this.molangRuntime, rotXPoint, easingType));
				bone.setRotY(EasingType.lerpWithOverride(this.molangRuntime, rotYPoint, easingType));
				bone.setRotZ(EasingType.lerpWithOverride(this.molangRuntime, rotZPoint, easingType));
			}

			if (posXPoint != null) {
				bone.setPosX(EasingType.lerpWithOverride(this.molangRuntime, posXPoint, easingType));
				bone.setPosY(EasingType.lerpWithOverride(this.molangRuntime, posYPoint, easingType));
				bone.setPosZ(EasingType.lerpWithOverride(this.molangRuntime, posZPoint, easingType));
			}

			if (scaleXPoint != null) {
				bone.setScaleX(EasingType.lerpWithOverride(this.molangRuntime, scaleXPoint, easingType));
				bone.setScaleY(EasingType.lerpWithOverride(this.molangRuntime, scaleYPoint, easingType));
				bone.setScaleZ(EasingType.lerpWithOverride(this.molangRuntime, scaleZPoint, easingType));
			}

			if (bendPoint != null) {
				bone.setBend(EasingType.lerpWithOverride(this.molangRuntime, bendPoint, easingType));
			}
		}

		if (this.currentAnimation == null) return;
		Map<String, String> parentsMap = this.currentAnimation.animation().parents();

        List<PlayerAnimBone> bones1 = new ArrayList<>(this.bones.values());
		for (PlayerAnimBone pivotBone : this.pivotBones.values()) {
			if (!parentsMap.containsValue(pivotBone.getName()))
				bones1.add(pivotBone);
		}

		for (PlayerAnimBone bone : bones1) {
			if (parentsMap.containsKey(bone.getName())) {
				if (!this.boneAnimationQueues.containsKey(bone.getName())) bone.setToInitialPose();
				this.activeBones.put(bone.getName(), bone);
				ModMatrix4f matrix = new ModMatrix4f();
				List<PivotBone> parents = new ArrayList<>();
				PivotBone currentParent = this.pivotBones.get(parentsMap.get(bone.getName()));
				parents.add(currentParent);
				while (parentsMap.containsKey(currentParent.getName())) {
					currentParent = this.pivotBones.get(parentsMap.get(currentParent.getName()));
					parents.addFirst(currentParent);
				}

				for (PivotBone pivotBone : parents) {
					MatrixUtil.prepMatrixForBone(matrix, pivotBone, pivotBone.getPivot());
				}

				Vec3f defaultPos = getBonePosition(bone.getName());
				ModVector4f pos = new ModVector4f(defaultPos.x(), defaultPos.y(), defaultPos.z(), 1).mul(matrix);
				bone.setPosX(-pos.x + defaultPos.x() + bone.getPosX());
				bone.setPosY(pos.y - defaultPos.y() + bone.getPosY());
				bone.setPosZ(-pos.z + defaultPos.z() + bone.getPosZ());

				Vec3f rotation = matrix.getEulerRotation();
				bone.addRot(rotation.x(), rotation.y(), rotation.z());

				bone.mulScale(matrix.getColumnScale(0), matrix.getColumnScale(1), matrix.getColumnScale(2));
			}
		}
	}

	public abstract Vec3f getBonePosition(String name);

	public AnimationController addModifier(@NotNull AbstractModifier modifier, int idx) {
		modifier.setHost(this);
		modifiers.add(idx, modifier);
		linkModifiers();
		return this;
	}

	public AnimationController addModifierBefore(@NotNull AbstractModifier modifier) {
		this.addModifier(modifier, 0);
		return this;
	}

	public AnimationController addModifierLast(@NotNull AbstractModifier modifier) {
		this.addModifier(modifier, modifiers.size());
		return this;
	}

	public AnimationController removeModifier(int idx) {
		modifiers.remove(idx);
		linkModifiers();
		return this;
	}

	public AnimationController removeAllModifiers() {
		modifiers.clear();
		return this;
	}

	public int getModifierCount() {
		return modifiers.size();
	}

	public @Nullable AbstractModifier getModifier(int idx) {
		try {
			return modifiers.get(idx);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	public boolean removeModifierIf(Predicate<? super AbstractModifier> predicate) {
		boolean success = modifiers.removeIf(predicate);
		linkModifiers();
		return success;
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

	protected void registerPlayerAnimBone(String name) {
		registerPlayerAnimBone(new AdvancedPlayerAnimBone(name));
	}

	/**
	 * Adds the given bone to the bones list for this controller
	 * <p>
	 * This is normally handled automatically by the mod
	 */
	protected void registerPlayerAnimBone(AdvancedPlayerAnimBone bone) {
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
	 *		return animationSetter.apply(myWalkAnimation);
	 *	}
	 *  return animationSetter.apply(myIdleAnimation);
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
		PlayState handle(AnimationController controller, AnimationData state, AnimationSetter animationSetter);
	}

	@FunctionalInterface
	public interface AnimationSetter {
		default PlayState setAnimation(RawAnimation animation) {
			return setAnimation(animation, 0);
		}
		PlayState setAnimation(RawAnimation animation, int startFromTick);
	}

	@SuppressWarnings("ConstantConditions")
	private static class InternalAnimationAccessor extends AnimationContainer<AnimationController> {
		private InternalAnimationAccessor(AnimationController controller) {
			super(controller);
		}

		@Override
		public void tick(AnimationData state) {
			this.anim.internalSetupAnim(state);
		}

		@Override
		public void setupAnim(AnimationData state) {
			this.anim.internalSetupAnim(state);
		}

		@Override
		public PlayerAnimBone get3DTransform(@NotNull PlayerAnimBone bone) {
			return this.anim.get3DTransformRaw(bone);
		}
	}
}
