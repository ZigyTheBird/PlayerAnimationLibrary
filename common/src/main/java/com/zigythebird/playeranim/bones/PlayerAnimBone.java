package com.zigythebird.playeranim.bones;

import com.zigythebird.playeranim.animation.EasingType;
import com.zigythebird.playeranim.math.Pair;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.Objects;

/**
 * Mutable bone object representing a set of cubes, as well as child bones
 * <p>
 * This is the object that is directly modified by animations to handle movement
 */
@SuppressWarnings("LombokSetterMayBeUsed")
public class PlayerAnimBone {
	private final Vec3 pivot;
	private final String name;

	public PlayerAnimBone parent;

	protected float scaleX = 1;
	protected float scaleY = 1;
	protected float scaleZ = 1;

	protected float positionX;
	protected float positionY;
	protected float positionZ;

	protected float rotX;
	protected float rotY;
	protected float rotZ;

	protected float bendAxis;
	protected float bend;

	private boolean positionChanged = false;
	private boolean rotationChanged = false;
	private boolean scaleChanged = false;
	private boolean bendChanged = false;

	public PlayerAnimBone(String name) {
		this(name, null);
	}

	public PlayerAnimBone(String name, Vec3 pivot) {
		this(null, name, pivot);
	}

	public PlayerAnimBone(PlayerAnimBone parent, String name, Vec3 pivot) {
		this.parent = parent;
		this.name = name;
		this.pivot = pivot;
	}

	public String getName() {
		return this.name;
	}

	public PlayerAnimBone getParent() {
		return this.parent;
	}

	public Vec3 getPivot() {return this.pivot;}

	public float getRotX() {
		return this.rotX;
	}

	public float getRotY() {
		return this.rotY;
	}

	public float getRotZ() {
		return this.rotZ;
	}

	public float getPosX() {
		return this.positionX;
	}

	public float getPosY() {
		return this.positionY;
	}

	public float getPosZ() {
		return this.positionZ;
	}

	public float getScaleX() {
		return this.scaleX;
	}

	public float getScaleY() {
		return this.scaleY;
	}

	public float getScaleZ() {
		return this.scaleZ;
	}

	public float getBendAxis() {
		return this.bendAxis;
	}

	public float getBend() {
		return this.bend;
	}

	public void setRotX(float value) {
		if (Float.isNaN(value)) return;
		this.rotX = value;

		markRotationAsChanged();
	}

	public void setRotY(float value) {
		if (Float.isNaN(value)) return;
		this.rotY = value;

		markRotationAsChanged();
	}

	public void setRotZ(float value) {
		if (Float.isNaN(value)) return;
		this.rotZ = value;

		markRotationAsChanged();
	}

	public void updateRotation(float xRot, float yRot, float zRot) {
		setRotX(xRot);
		setRotY(yRot);
		setRotZ(zRot);
	}

	public void setPosX(float value) {
		if (Float.isNaN(value)) return;
		this.positionX = value;

		markPositionAsChanged();
	}

	public void setPosY(float value) {
		if (Float.isNaN(value)) return;
		this.positionY = value;

		markPositionAsChanged();
	}

	public void setPosZ(float value) {
		if (Float.isNaN(value)) return;
		this.positionZ = value;

		markPositionAsChanged();
	}

	public void updatePosition(float posX, float posY, float posZ) {
		setPosX(posX);
		setPosY(posY);
		setPosZ(posZ);
	}

	public void setScaleX(float value) {
		if (Float.isNaN(value)) return;
		this.scaleX = value;

		markScaleAsChanged();
	}

	public void setScaleY(float value) {
		if (Float.isNaN(value)) return;
		this.scaleY = value;

		markScaleAsChanged();
	}

	public void setScaleZ(float value) {
		if (Float.isNaN(value)) return;
		this.scaleZ = value;

		markScaleAsChanged();
	}

	public void updateScale(float scaleX, float scaleY, float scaleZ) {
		setScaleX(scaleX);
		setScaleY(scaleY);
		setScaleZ(scaleZ);
	}

	public void setBendAxis(float value) {
		this.bendAxis = value;

		this.markBendAsChanged();
	}

	public void setBend(float value) {
		this.bend = value;

		this.markBendAsChanged();
	}

	public void updateBend(float bendAxis, float bend) {
		setBendAxis(bendAxis);
		setBend(bend);
	}

	public void updateBend(Pair<Float, Float> bend) {
		setBendAxis(bend.getLeft());
		setBend(bend.getRight());
	}

	public void markScaleAsChanged() {
		this.scaleChanged = true;
	}

	public void markBendAsChanged() {
		this.bendChanged = true;
	}

	public void markRotationAsChanged() {
		this.rotationChanged = true;
	}

	public void markPositionAsChanged() {
		this.positionChanged = true;
	}

	public boolean hasScaleChanged() {
		return this.scaleChanged;
	}

	public boolean hasBendChanged() {
		return this.bendChanged;
	}

	public boolean hasRotationChanged() {
		return this.rotationChanged;
	}

	public boolean hasPositionChanged() {
		return this.positionChanged;
	}

	public void resetStateChanges() {
		this.scaleChanged = false;
		this.rotationChanged = false;
		this.positionChanged = false;
		this.bendChanged = false;
	}
	
	public void setToInitialPose() {
		this.positionX = 0;
		this.positionY = 0;
		this.positionZ = 0;

		this.rotX = 0;
		this.rotY = 0;
		this.rotZ = 0;

		this.scaleX = 1;
		this.scaleY = 1;
		this.scaleZ = 1;

		this.bendAxis = 0;
		this.bend = 0;
	}

	public Vector3d getPositionVector() {
		return new Vector3d(getPosX(), getPosY(), getPosZ());
	}

	public Vector3d getRotationVector() {
		return new Vector3d(getRotX(), getRotY(), getRotZ());
	}

	public Vector3d getScaleVector() {
		return new Vector3d(getScaleX(), getScaleY(), getScaleZ());
	}

	public void addRotationOffsetFromBone(PlayerAnimBone source) {
		setRotX(getRotX() + source.getRotX());
		setRotY(getRotY() + source.getRotY());
		setRotZ(getRotZ() + source.getRotZ());
	}
	
	public PlayerAnimBone scale(float value) {
		this.positionX *= value;
		this.positionY *= value;
		this.positionZ *= value;

		this.rotX *= value;
		this.rotY *= value;
		this.rotZ *= value;

		this.scaleX *= value;
		this.scaleY *= value;
		this.scaleZ *= value;

		this.bendAxis *= value;
		this.bend *= value;

		return this;
	}

	public PlayerAnimBone add(PlayerAnimBone bone) {
		this.positionX += bone.positionX;
		this.positionY += bone.positionY;
		this.positionZ += bone.positionZ;

		this.rotX += bone.rotX;
		this.rotY += bone.rotY;
		this.rotZ += bone.rotZ;

		this.scaleX += bone.scaleX;
		this.scaleY += bone.scaleY;
		this.scaleZ += bone.scaleZ;

		this.bendAxis += bone.bendAxis;
		this.bend += bone.bend;

		return this;
	}

	public void copyOtherBone(PlayerAnimBone bone) {
		if (!Float.isNaN(bone.positionX))
			this.positionX = bone.positionX;
		if (!Float.isNaN(bone.positionY))
			this.positionY = bone.positionY;
		if (!Float.isNaN(bone.positionZ))
			this.positionZ = bone.positionZ;

		if (!Float.isNaN(bone.rotX))
			this.rotX = bone.rotX;
		if (!Float.isNaN(bone.rotY))
			this.rotY = bone.rotY;
		if (!Float.isNaN(bone.rotZ))
			this.rotZ = bone.rotZ;

		if (!Float.isNaN(bone.scaleX))
			this.scaleX = bone.scaleX;
		if (!Float.isNaN(bone.scaleY))
			this.scaleY = bone.scaleY;
		if (!Float.isNaN(bone.scaleZ))
			this.scaleZ = bone.scaleZ;

		if (!Float.isNaN(bone.bendAxis))
			this.bendAxis = bone.bendAxis;
		if (!Float.isNaN(bone.bend))
			this.bend = bone.bend;
	}

	public void beginTickLerp(AdvancedPlayerAnimBone bone, float animTime) {
		this.positionX = applyBeginTickLerp(positionX, bone.positionX, bone.positionXBeginTransitionLength, animTime);
		this.positionY = applyBeginTickLerp(positionY, bone.positionY, bone.positionYBeginTransitionLength, animTime);
		this.positionZ = applyBeginTickLerp(positionZ, bone.positionZ, bone.positionZBeginTransitionLength, animTime);

		this.rotX = applyBeginTickLerp(rotX, bone.rotX, bone.rotXBeginTransitionLength, animTime);
		this.rotY = applyBeginTickLerp(rotY, bone.rotY, bone.rotYBeginTransitionLength, animTime);
		this.rotZ = applyBeginTickLerp(rotZ, bone.rotZ, bone.rotZBeginTransitionLength, animTime);

		this.scaleX = applyBeginTickLerp(scaleX, bone.scaleX, bone.scaleXBeginTransitionLength, animTime);
		this.scaleY = applyBeginTickLerp(scaleY, bone.scaleY, bone.scaleYBeginTransitionLength, animTime);
		this.scaleZ = applyBeginTickLerp(scaleZ, bone.scaleZ, bone.scaleZBeginTransitionLength, animTime);

		this.bendAxis = applyBeginTickLerp(bendAxis, bone.bendAxis, bone.bendAxisBeginTransitionLength, animTime);
		this.bend = applyBeginTickLerp(bend, bone.bend, bone.bendBeginTransitionLength, animTime);
	}
	
	private float applyBeginTickLerp(float startValue, float endValue, Float transitionLength, float animTime) {
		if (!Float.isNaN(endValue)) {
			if (transitionLength != null)
				return (float) EasingType.EASE_IN_OUT_SINE.apply(startValue, endValue, animTime / transitionLength);
			return endValue;
		}
		return startValue;
	}

	public void copySnapshot(BoneSnapshot snapshot) {
		this.positionX = snapshot.getOffsetX();
		this.positionY = snapshot.getOffsetY();
		this.positionZ = snapshot.getOffsetZ();

		this.rotX = snapshot.getRotX();
		this.rotY = snapshot.getRotY();
		this.rotZ = snapshot.getRotZ();

		this.scaleX = snapshot.getScaleX();
		this.scaleY = snapshot.getScaleY();
		this.scaleZ = snapshot.getScaleZ();

		this.bend = snapshot.getBend();
		this.bendAxis = snapshot.getBendAxis();
	}

	public void copyVanillaPart(ModelPart part) {
		PartPose initialPose = part.getInitialPose();

		this.positionX = part.x - initialPose.x();
		this.positionY = part.y - initialPose.y();
		this.positionZ = part.z - initialPose.z();

		this.rotX = part.zRot;
		this.rotY = part.yRot;
		this.rotZ = part.zRot;

		this.scaleX = part.xScale;
		this.scaleY = part.yScale;
		this.scaleZ = part.zScale;

		this.bendAxis = 0;
		this.bend = 0;
	}

	public BoneSnapshot saveSnapshot() {
		return new BoneSnapshot(this);
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || getClass() != obj.getClass())
			return false;

		return hashCode() == obj.hashCode();
	}

	public int hashCode() {
		return Objects.hash(getName(), (getParent() != null ? getParent().getName() : 0));
	}
}
