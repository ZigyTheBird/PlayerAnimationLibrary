package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.cache.PlayerAnimBone;
import com.zigythebird.playeranim.math.Pair;
import com.zigythebird.playeranim.math.Vec3f;

/**
 * Transformations applied to the bone are monitored by the {@link AnimationProcessor}
 * in the course of animations, and stored here for monitoring.
 */
public class BoneSnapshot {
	public final PlayerAnimBone bone;

	private float scaleX;
	private float scaleY;
	private float scaleZ;

	private float offsetPosX;
	private float offsetPosY;
	private float offsetPosZ;

	private float rotX;
	private float rotY;
	private float rotZ;

	private float bendAxis;
	private float bend;

	private double lastResetRotationTick = 0;
	private double lastResetPositionTick = 0;
	private double lastResetScaleTick = 0;
	private double lastResetBendTick = 0;

	protected boolean rotAnimInProgress = true;
	protected boolean posAnimInProgress = true;
	protected boolean scaleAnimInProgress = true;
	protected boolean bendAnimInProgress = true;

	public BoneSnapshot() {
		this.bone = null;
		setToInitialPose();
	}

	public BoneSnapshot(PlayerAnimBone bone) {
		this.rotX = bone.getRotX();
		this.rotY = bone.getRotY();
		this.rotZ = bone.getRotZ();

		this.offsetPosX = bone.getPosX();
		this.offsetPosY = bone.getPosY();
		this.offsetPosZ = bone.getPosZ();

		this.scaleX = bone.getScaleX();
		this.scaleY = bone.getScaleY();
		this.scaleZ = bone.getScaleZ();

		this.bendAxis = bone.getBendAxis();
		this.bend = bone.getBend();

		this.bone = bone;
	}

	public BoneSnapshot(BoneSnapshot bone) {
		this.bone = bone.getBone();

		this.rotX = bone.getRotX();
		this.rotY = bone.getRotY();
		this.rotZ = bone.getRotZ();

		this.offsetPosX = bone.getOffsetX();
		this.offsetPosY = bone.getOffsetY();
		this.offsetPosZ = bone.getOffsetZ();

		this.scaleX = bone.getScaleX();
		this.scaleY = bone.getScaleY();
		this.scaleZ = bone.getScaleZ();

		this.bendAxis = bone.getBendAxis();
		this.bend = bone.getBend();
	}

	public PlayerAnimBone getBone() {return this.bone;}

	public float getScaleX() {
		return this.scaleX;
	}

	public float getScaleY() {
		return this.scaleY;
	}

	public float getScaleZ() {
		return this.scaleZ;
	}

	public float getOffsetX() {
		return this.offsetPosX;
	}

	public float getOffsetY() {
		return this.offsetPosY;
	}

	public float getOffsetZ() {
		return this.offsetPosZ;
	}

	public float getRotX() {
		return this.rotX;
	}

	public float getRotY() {
		return this.rotY;
	}

	public float getRotZ() {
		return this.rotZ;
	}

	public float getBendAxis() {
		return this.bendAxis;
	}

	public float getBend() {
		return this.bend;
	}

	public double getLastResetRotationTick() {
		return this.lastResetRotationTick;
	}

	public double getLastResetPositionTick() {
		return this.lastResetPositionTick;
	}

	public double getLastResetScaleTick() {
		return this.lastResetScaleTick;
	}

	public double getLastResetBendTick() {
		return this.lastResetBendTick;
	}

	public boolean isRotAnimInProgress() {
		return this.rotAnimInProgress;
	}

	public boolean isPosAnimInProgress() {
		return this.posAnimInProgress;
	}

	public boolean isScaleAnimInProgress() {
		return this.scaleAnimInProgress;
	}

	public boolean isBendAnimInProgress() {
		return this.bendAnimInProgress;
	}

	/**
	 * Update the scale state of this snapshot
	 */
	public void updateScale(float scaleX, float scaleY, float scaleZ) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.scaleZ = scaleZ;
	}

	/**
	 * Update the offset state of this snapshot
	 */
	public void updateOffset(float offsetX, float offsetY, float offsetZ) {
		this.offsetPosX = offsetX;
		this.offsetPosY = offsetY;
		this.offsetPosZ = offsetZ;
	}

	/**
	 * Update the rotation state of this snapshot
	 */
	public void updateRotation(float rotX, float rotY, float rotZ) {
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
	}

	public void updateBend(float bendAxis, float bend) {
		this.bendAxis = bendAxis;
		this.bend = bend;
	}

	public void updateBend(Pair<Float, Float> bend) {
		updateBend(bend.getLeft(), bend.getRight());
	}

	public void startPosAnim() {
		this.posAnimInProgress = true;
	}

	public void stopPosAnim(double tick) {
		this.posAnimInProgress = false;
		this.lastResetPositionTick = tick;
	}

	public void startRotAnim() {
		this.rotAnimInProgress = true;
	}

	public void stopRotAnim(double tick) {
		this.rotAnimInProgress = false;
		this.lastResetRotationTick = tick;
	}

	public void startScaleAnim() {
		this.scaleAnimInProgress = true;
	}

	public void stopScaleAnim(double tick) {
		this.scaleAnimInProgress = false;
		this.lastResetScaleTick = tick;
	}

	public void startBendAnim() {
		this.bendAnimInProgress = true;
	}

	public void stopBendAnim(double tick) {
		this.bendAnimInProgress = false;
		this.lastResetBendTick = tick;
	}

	public Vec3f getTransformFromType(TransformType type) {
		switch (type) {
			case POSITION -> {
				return new Vec3f(offsetPosX, offsetPosY, offsetPosZ);
			}
			case ROTATION -> {
				return new Vec3f(rotX, rotY, rotZ);
			}
            case SCALE -> {
				return new Vec3f(scaleX, scaleY, scaleZ);
			}
			case BEND -> {
				return new Vec3f(bendAxis, bend, 0);
			}
		}
		return null;
	}

	public void setToInitialPose() {
		this.rotX = 0;
		this.rotY = 0;
		this.rotZ = 0;

		this.offsetPosX = 0;
		this.offsetPosY = 0;
		this.offsetPosZ = 0;

		this.scaleX = 1;
		this.scaleY = 1;
		this.scaleZ = 1;

		this.bendAxis = 0;
		this.bend = 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || getClass() != obj.getClass())
			return false;

		return hashCode() == obj.hashCode();
	}

	@Override
	public int hashCode() {
		return this.bone.getName().hashCode();
	}
}
