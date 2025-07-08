package com.zigythebird.playeranimcore.animation.keyframe;

import com.zigythebird.playeranimcore.bones.PlayerAnimBone;

/**
 * A bone pseudo-stack for bone animation positions, scales, and rotations
 * <p>
 * Animation points are calculated then pushed onto their respective queues to be used for transformations in rendering
 */
public record BoneAnimationQueue(PlayerAnimBone bone, AnimationPointQueue rotationXQueue, AnimationPointQueue rotationYQueue,
								 AnimationPointQueue rotationZQueue, AnimationPointQueue positionXQueue, AnimationPointQueue positionYQueue,
								 AnimationPointQueue positionZQueue, AnimationPointQueue scaleXQueue, AnimationPointQueue scaleYQueue,
								 AnimationPointQueue scaleZQueue, AnimationPointQueue bendQueue) {
	public BoneAnimationQueue(PlayerAnimBone bone) {
		this(bone, new AnimationPointQueue(), new AnimationPointQueue(), new AnimationPointQueue(),
				new AnimationPointQueue(), new AnimationPointQueue(), new AnimationPointQueue(),
				new AnimationPointQueue(), new AnimationPointQueue(), new AnimationPointQueue(), new AnimationPointQueue());
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
	 * Add a bend {@link AnimationPoint} to the bend queue
	 *
	 * @param bendPoint The bend {@code AnimationPoint} to add
	 */
	public void addBend(AnimationPoint bendPoint) {
		this.bendQueue.add(bendPoint);
	}
}
