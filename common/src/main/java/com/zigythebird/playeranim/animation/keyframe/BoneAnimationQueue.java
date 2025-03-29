package com.zigythebird.playeranim.animation.keyframe;

import com.zigythebird.playeranim.animation.AnimationController;
import com.zigythebird.playeranim.bones.BoneSnapshot;
import com.zigythebird.playeranim.bones.PlayerAnimBone;

/**
 * A bone pseudo-stack for bone animation positions, scales, and rotations
 * <p>
 * Animation points are calculated then pushed onto their respective queues to be used for transformations in rendering
 */
public record BoneAnimationQueue(PlayerAnimBone bone, AnimationPointQueue rotationXQueue, AnimationPointQueue rotationYQueue,
								 AnimationPointQueue rotationZQueue, AnimationPointQueue positionXQueue, AnimationPointQueue positionYQueue,
								 AnimationPointQueue positionZQueue, AnimationPointQueue scaleXQueue, AnimationPointQueue scaleYQueue,
								 AnimationPointQueue scaleZQueue, AnimationPointQueue bendAxisQueue, AnimationPointQueue bendQueue) {
	public BoneAnimationQueue(PlayerAnimBone bone) {
		this(bone, new AnimationPointQueue(), new AnimationPointQueue(), new AnimationPointQueue(),
				new AnimationPointQueue(), new AnimationPointQueue(), new AnimationPointQueue(),
				new AnimationPointQueue(), new AnimationPointQueue(), new AnimationPointQueue(),
				new AnimationPointQueue(), new AnimationPointQueue());
	}

	/**
	 * Add a new {@link AnimationPoint} to the {@link BoneAnimationQueue#positionXQueue}
	 *
	 * @param keyFrame The {@code Nullable} Keyframe relevant to the animation point
	 * @param lerpedTick The lerped time (current tick + partial tick) that the point starts at
	 * @param transitionLength The length of the transition (based on the {@link AnimationController})
	 * @param startValue The value of the point at the start of its transition
	 * @param endValue The value of the point at the end of its transition
	 */
	public void addPosXPoint(Keyframe keyFrame, double lerpedTick, double transitionLength, double startValue, double endValue) {
		this.positionXQueue.add(new AnimationPoint(keyFrame, lerpedTick, transitionLength, startValue, endValue));
	}

	/**
	 * Add a new {@link AnimationPoint} to the {@link BoneAnimationQueue#positionYQueue}
	 *
	 * @param keyFrame The {@code Nullable} Keyframe relevant to the animation point
	 * @param lerpedTick The lerped time (current tick + partial tick) that the point starts at
	 * @param transitionLength The length of the transition (based on the {@link AnimationController})
	 * @param startValue The value of the point at the start of its transition
	 * @param endValue The value of the point at the end of its transition
	 */
	public void addPosYPoint(Keyframe keyFrame, double lerpedTick, double transitionLength, double startValue, double endValue) {
		this.positionYQueue.add(new AnimationPoint(keyFrame, lerpedTick, transitionLength, startValue, endValue));
	}

	/**
	 * Add a new {@link AnimationPoint} to the {@link BoneAnimationQueue#positionZQueue}
	 *
	 * @param keyFrame The {@code Nullable} Keyframe relevant to the animation point
	 * @param lerpedTick The lerped time (current tick + partial tick) that the point starts at
	 * @param transitionLength The length of the transition (based on the {@link AnimationController})
	 * @param startValue The value of the point at the start of its transition
	 * @param endValue The value of the point at the end of its transition
	 */
	public void addPosZPoint(Keyframe keyFrame, double lerpedTick, double transitionLength, double startValue, double endValue) {
		this.positionZQueue.add(new AnimationPoint(keyFrame, lerpedTick, transitionLength, startValue, endValue));
	}

	/**
	 * Add a new X, Y, and Z position {@link AnimationPoint} to their respective queues
	 *
	 * @param keyFrame The {@code Nullable} Keyframe relevant to the animation point
	 * @param lerpedTick The lerped time (current tick + partial tick) that the point starts at
	 * @param transitionLength The length of the transition (base on the {@link AnimationController}
	 * @param startSnapshot The {@link BoneSnapshot} that serves as the starting positions relevant to the keyframe provided
	 * @param nextXPoint The X {@code AnimationPoint} that is next in the queue, to serve as the end value of the new point
	 * @param nextYPoint The Y {@code AnimationPoint} that is next in the queue, to serve as the end value of the new point
	 * @param nextZPoint The Z {@code AnimationPoint} that is next in the queue, to serve as the end value of the new point
	 */
	public void addNextPosition(Keyframe keyFrame, double lerpedTick, double transitionLength, BoneSnapshot startSnapshot, AnimationPoint nextXPoint, AnimationPoint nextYPoint, AnimationPoint nextZPoint) {
		if (nextXPoint != null)
			addPosXPoint(keyFrame, lerpedTick, transitionLength, startSnapshot.getOffsetX(), nextXPoint.animationStartValue());
		if (nextYPoint != null)
			addPosYPoint(keyFrame, lerpedTick, transitionLength, startSnapshot.getOffsetY(), nextYPoint.animationStartValue());
		if (nextZPoint != null)
			addPosZPoint(keyFrame, lerpedTick, transitionLength, startSnapshot.getOffsetZ(), nextZPoint.animationStartValue());
	}

	/**
	 * Add a new {@link AnimationPoint} to the {@link BoneAnimationQueue#scaleXQueue}
	 *
	 * @param keyFrame The {@code Nullable} Keyframe relevant to the animation point
	 * @param lerpedTick The lerped time (current tick + partial tick) that the point starts at
	 * @param transitionLength The length of the transition (based on the {@link AnimationController})
	 * @param startValue The value of the point at the start of its transition
	 * @param endValue The value of the point at the end of its transition
	 */
	public void addScaleXPoint(Keyframe keyFrame, double lerpedTick, double transitionLength, double startValue, double endValue) {
		this.scaleXQueue.add(new AnimationPoint(keyFrame, lerpedTick, transitionLength, startValue, endValue));
	}

	/**
	 * Add a new {@link AnimationPoint} to the {@link BoneAnimationQueue#scaleYQueue}
	 *
	 * @param keyFrame The {@code Nullable} Keyframe relevant to the animation point
	 * @param lerpedTick The lerped time (current tick + partial tick) that the point starts at
	 * @param transitionLength The length of the transition (based on the {@link AnimationController})
	 * @param startValue The value of the point at the start of its transition
	 * @param endValue The value of the point at the end of its transition
	 */
	public void addScaleYPoint(Keyframe keyFrame, double lerpedTick, double transitionLength, double startValue, double endValue) {
		this.scaleYQueue.add(new AnimationPoint(keyFrame, lerpedTick, transitionLength, startValue, endValue));
	}

	/**
	 * Add a new {@link AnimationPoint} to the {@link BoneAnimationQueue#scaleZQueue}
	 *
	 * @param keyFrame The {@code Nullable} Keyframe relevant to the animation point
	 * @param lerpedTick The lerped time (current tick + partial tick) that the point starts at
	 * @param transitionLength The length of the transition (based on the {@link AnimationController})
	 * @param startValue The value of the point at the start of its transition
	 * @param endValue The value of the point at the end of its transition
	 */
	public void addScaleZPoint(Keyframe keyFrame, double lerpedTick, double transitionLength, double startValue, double endValue) {
		this.scaleZQueue.add(new AnimationPoint(keyFrame, lerpedTick, transitionLength, startValue, endValue));
	}

	/**
	 * Add a new X, Y, and Z scale {@link AnimationPoint} to their respective queues
	 *
	 * @param keyFrame The {@code Nullable} Keyframe relevant to the animation point
	 * @param lerpedTick The lerped time (current tick + partial tick) that the point starts at
	 * @param transitionLength The length of the transition (base on the {@link AnimationController}
	 * @param startSnapshot The {@link BoneSnapshot} that serves as the starting scales relevant to the keyframe provided
	 * @param nextXPoint The X {@code AnimationPoint} that is next in the queue, to serve as the end value of the new point
	 * @param nextYPoint The Y {@code AnimationPoint} that is next in the queue, to serve as the end value of the new point
	 * @param nextZPoint The Z {@code AnimationPoint} that is next in the queue, to serve as the end value of the new point
	 */
	public void addNextScale(Keyframe keyFrame, double lerpedTick, double transitionLength, BoneSnapshot startSnapshot, AnimationPoint nextXPoint, AnimationPoint nextYPoint, AnimationPoint nextZPoint) {
		if (nextXPoint != null)
			addScaleXPoint(keyFrame, lerpedTick, transitionLength, startSnapshot.getScaleX(), nextXPoint.animationStartValue());
		if (nextYPoint != null)
			addScaleYPoint(keyFrame, lerpedTick, transitionLength, startSnapshot.getScaleY(), nextYPoint.animationStartValue());
		if (nextZPoint != null)
			addScaleZPoint(keyFrame, lerpedTick, transitionLength, startSnapshot.getScaleZ(), nextZPoint.animationStartValue());
	}

	/**
	 * Add a new {@link AnimationPoint} to the {@link BoneAnimationQueue#rotationXQueue}
	 *
	 * @param keyFrame The {@code Nullable} Keyframe relevant to the animation point
	 * @param lerpedTick The lerped time (current tick + partial tick) that the point starts at
	 * @param transitionLength The length of the transition (based on the {@link AnimationController})
	 * @param startValue The value of the point at the start of its transition
	 * @param endValue The value of the point at the end of its transition
	 */
	public void addRotationXPoint(Keyframe keyFrame, double lerpedTick, double transitionLength, double startValue, double endValue) {
		this.rotationXQueue.add(new AnimationPoint(keyFrame, lerpedTick, transitionLength, startValue, endValue));
	}

	/**
	 * Add a new {@link AnimationPoint} to the {@link BoneAnimationQueue#rotationYQueue}
	 *
	 * @param keyFrame The {@code Nullable} Keyframe relevant to the animation point
	 * @param lerpedTick The lerped time (current tick + partial tick) that the point starts at
	 * @param transitionLength The length of the transition (based on the {@link AnimationController})
	 * @param startValue The value of the point at the start of its transition
	 * @param endValue The value of the point at the end of its transition
	 */
	public void addRotationYPoint(Keyframe keyFrame, double lerpedTick, double transitionLength, double startValue, double endValue) {
		this.rotationYQueue.add(new AnimationPoint(keyFrame, lerpedTick, transitionLength, startValue, endValue));
	}

	/**
	 * Add a new {@link AnimationPoint} to the {@link BoneAnimationQueue#rotationZQueue}
	 *
	 * @param keyFrame The {@code Nullable} Keyframe relevant to the animation point
	 * @param lerpedTick The lerped time (current tick + partial tick) that the point starts at
	 * @param transitionLength The length of the transition (based on the {@link AnimationController})
	 * @param startValue The value of the point at the start of its transition
	 * @param endValue The value of the point at the end of its transition
	 */
	public void addRotationZPoint(Keyframe keyFrame, double lerpedTick, double transitionLength, double startValue, double endValue) {
		this.rotationZQueue.add(new AnimationPoint(keyFrame, lerpedTick, transitionLength, startValue, endValue));
	}

	/**
	 * Add a new X, Y, and Z rotation {@link AnimationPoint} to their respective queues
	 *
	 * @param keyFrame The {@code Nullable} Keyframe relevant to the animation point
	 * @param lerpedTick The lerped time (current tick + partial tick) that the point starts at
	 * @param transitionLength The length of the transition (base on the {@link AnimationController}
	 * @param startSnapshot The {@link BoneSnapshot} that serves as the starting rotations relevant to the keyframe provided
	 * @param nextXPoint The X {@code AnimationPoint} that is next in the queue, to serve as the end value of the new point
	 * @param nextYPoint The Y {@code AnimationPoint} that is next in the queue, to serve as the end value of the new point
	 * @param nextZPoint The Z {@code AnimationPoint} that is next in the queue, to serve as the end value of the new point
	 */
	public void addNextRotation(Keyframe keyFrame, double lerpedTick, double transitionLength, BoneSnapshot startSnapshot, AnimationPoint nextXPoint, AnimationPoint nextYPoint, AnimationPoint nextZPoint) {
		if (nextXPoint != null)
			addRotationXPoint(keyFrame, lerpedTick, transitionLength, startSnapshot.getRotX(), nextXPoint.animationStartValue());
		if (nextYPoint != null)
			addRotationYPoint(keyFrame, lerpedTick, transitionLength, startSnapshot.getRotY(), nextYPoint.animationStartValue());
		if (nextZPoint != null)
			addRotationZPoint(keyFrame, lerpedTick, transitionLength, startSnapshot.getRotZ(), nextZPoint.animationStartValue());
	}

	/**
	 * Add a new {@link AnimationPoint} to the {@link BoneAnimationQueue#bendAxisQueue}
	 *
	 * @param keyFrame The {@code Nullable} Keyframe relevant to the animation point
	 * @param lerpedTick The lerped time (current tick + partial tick) that the point starts at
	 * @param transitionLength The length of the transition (based on the {@link AnimationController})
	 * @param startValue The value of the point at the start of its transition
	 * @param endValue The value of the point at the end of its transition
	 */
	public void addBendAxisPoint(Keyframe keyFrame, double lerpedTick, double transitionLength, double startValue, double endValue) {
		this.bendAxisQueue.add(new AnimationPoint(keyFrame, lerpedTick, transitionLength, startValue, endValue));
	}

	/**
	 * Add a new {@link AnimationPoint} to the {@link BoneAnimationQueue#bendQueue}
	 *
	 * @param keyFrame The {@code Nullable} Keyframe relevant to the animation point
	 * @param lerpedTick The lerped time (current tick + partial tick) that the point starts at
	 * @param transitionLength The length of the transition (based on the {@link AnimationController})
	 * @param startValue The value of the point at the start of its transition
	 * @param endValue The value of the point at the end of its transition
	 */
	public void addBendPoint(Keyframe keyFrame, double lerpedTick, double transitionLength, double startValue, double endValue) {
		this.bendQueue.add(new AnimationPoint(keyFrame, lerpedTick, transitionLength, startValue, endValue));
	}

	/**
	 * Add a bend and it's axis {@link AnimationPoint} to their respective queues
	 *
	 * @param keyFrame The {@code Nullable} Keyframe relevant to the animation point
	 * @param lerpedTick The lerped time (current tick + partial tick) that the point starts at
	 * @param transitionLength The length of the transition (base on the {@link AnimationController}
	 * @param startSnapshot The {@link BoneSnapshot} that serves as the starting rotations relevant to the keyframe provided
	 * @param nextBendAxis The bend axis {@code AnimationPoint} that is next in the queue, to serve as the end value of the new point
	 * @param nextBend The bend {@code AnimationPoint} that is next in the queue, to serve as the end value of the new point
	 */
	public void addNextBend(Keyframe keyFrame, double lerpedTick, double transitionLength, BoneSnapshot startSnapshot, AnimationPoint nextBendAxis, AnimationPoint nextBend) {
		if (nextBendAxis != null)
			addBendAxisPoint(keyFrame, lerpedTick, transitionLength, startSnapshot.getBendAxis(), nextBendAxis.animationStartValue());
		if (nextBend != null)
			addBendPoint(keyFrame, lerpedTick, transitionLength, startSnapshot.getBend(), nextBend.animationStartValue());
	}

	/**
	 * Add an X, Y, and Z position {@link AnimationPoint} to their respective queues
	 *
	 * @param xPoint The x position {@code AnimationPoint} to add
	 * @param yPoint The y position {@code AnimationPoint} to add
	 * @param zPoint The z position {@code AnimationPoint} to add
	 */
	public void addPositions(AnimationPoint xPoint, AnimationPoint yPoint, AnimationPoint zPoint) {
		this.positionXQueue.add(xPoint);
		this.positionYQueue.add(yPoint);
		this.positionZQueue.add(zPoint);
	}

	/**
	 * Add an X, Y, and Z scale {@link AnimationPoint} to their respective queues
	 *
	 * @param xPoint The x scale {@code AnimationPoint} to add
	 * @param yPoint The y scale {@code AnimationPoint} to add
	 * @param zPoint The z scale {@code AnimationPoint} to add
	 */
	public void addScales(AnimationPoint xPoint, AnimationPoint yPoint, AnimationPoint zPoint) {
		this.scaleXQueue.add(xPoint);
		this.scaleYQueue.add(yPoint);
		this.scaleZQueue.add(zPoint);
	}

	/**
	 * Add an X, Y, and Z rotation {@link AnimationPoint} to their respective queues
	 *
	 * @param xPoint The x rotation {@code AnimationPoint} to add
	 * @param yPoint The y rotation {@code AnimationPoint} to add
	 * @param zPoint The z rotation {@code AnimationPoint} to add
	 */
	public void addRotations(AnimationPoint xPoint, AnimationPoint yPoint, AnimationPoint zPoint) {
		this.rotationXQueue.add(xPoint);
		this.rotationYQueue.add(yPoint);
		this.rotationZQueue.add(zPoint);
	}

	/**
	 * Add a bend and it's axis {@link AnimationPoint} to their respective queues
	 *
	 * @param bendAxisPoint The bend axis {@code AnimationPoint} to add
	 * @param bendPoint The bend {@code AnimationPoint} to add
	 */
	public void addBends(AnimationPoint bendAxisPoint, AnimationPoint bendPoint) {
		this.bendAxisQueue.add(bendAxisPoint);
		this.bendQueue.add(bendPoint);
	}
}
