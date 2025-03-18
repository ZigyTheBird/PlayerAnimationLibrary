package com.zigythebird.playeranim.cache;

import com.zigythebird.playeranim.animation.BoneSnapshot;
import com.zigythebird.playeranim.math.Pair;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
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

	private BoneSnapshot initialSnapshot;

	private float scaleX = 1;
	private float scaleY = 1;
	private float scaleZ = 1;

	private float positionX;
	private float positionY;
	private float positionZ;

	private float rotX;
	private float rotY;
	private float rotZ;

	private float bendAxis;
	private float bend;

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
		this.rotX = value;

		markRotationAsChanged();
	}

	public void setRotY(float value) {
		this.rotY = value;

		markRotationAsChanged();
	}

	public void setRotZ(float value) {
		this.rotZ = value;

		markRotationAsChanged();
	}

	public void updateRotation(float xRot, float yRot, float zRot) {
		setRotX(xRot);
		setRotY(yRot);
		setRotZ(zRot);
	}

	public void setPosX(float value) {
		this.positionX = value;

		markPositionAsChanged();
	}

	public void setPosY(float value) {
		this.positionY = value;

		markPositionAsChanged();
	}

	public void setPosZ(float value) {
		this.positionZ = value;

		markPositionAsChanged();
	}

	public void updatePosition(float posX, float posY, float posZ) {
		setPosX(posX);
		setPosY(posY);
		setPosZ(posZ);
	}

	public void setScaleX(float value) {
		this.scaleX = value;

		markScaleAsChanged();
	}

	public void setScaleY(float value) {
		this.scaleY = value;

		markScaleAsChanged();
	}

	public void setScaleZ(float value) {
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

	public BoneSnapshot getInitialSnapshot() {
		return this.initialSnapshot;
	}

	public void saveInitialSnapshot() {
		if (this.initialSnapshot == null)
			this.initialSnapshot = saveSnapshot();
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
		setRotX(getRotX() + source.getRotX() - source.getInitialSnapshot().getRotX());
		setRotY(getRotY() + source.getRotY() - source.getInitialSnapshot().getRotY());
		setRotZ(getRotZ() + source.getRotZ() - source.getInitialSnapshot().getRotZ());
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
		this.positionX = bone.positionX;
		this.positionY = bone.positionY;
		this.positionZ = bone.positionZ;

		this.rotX = bone.rotX;
		this.rotY = bone.rotY;
		this.rotZ = bone.rotZ;

		this.scaleX = bone.scaleX;
		this.scaleY = bone.scaleY;
		this.scaleZ = bone.scaleZ;

		this.bendAxis = bone.bendAxis;
		this.bend = bone.bend;
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
