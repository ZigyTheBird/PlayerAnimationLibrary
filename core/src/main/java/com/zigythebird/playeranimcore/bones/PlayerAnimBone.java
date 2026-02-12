package com.zigythebird.playeranimcore.bones;

import com.zigythebird.playeranimcore.math.ExtendedVector3f;
import org.joml.Vector3f;

/**
 * This is the object that is directly modified by animations to handle movement
 */
public class PlayerAnimBone {
	public final String name;
	public final Vector3f pivot;

	public final ExtendedVector3f position = new ExtendedVector3f();
	public final ExtendedVector3f rotation = new ExtendedVector3f();
	public final ExtendedVector3f scale = new ExtendedVector3f(1F);

	public PlayerAnimBone(String name, Vector3f pivot) {
		this.name = name;
		this.pivot = pivot;
	}

	public PlayerAnimBone(PlayerAnimBone bone) {
		this(bone.getName(), new Vector3f(bone.pivot)); // copy
		copyOtherBone(bone);
	}

	public String getName() {
		return this.name;
	}

	public Vector3f getPivot() {
		return this.pivot;
	}

	public void setToInitialPose() {
		this.position.set(0, 0, 0);
		this.rotation.set(0, 0, 0);
		this.scale.set(1, 1, 1);
	}

	public PlayerAnimBone scale(float value) {
		this.position.mul(value);
		this.rotation.mul(value);
		this.scale.mul(value);

		return this;
	}

	public PlayerAnimBone add(PlayerAnimBone bone) {
		this.position.add(bone.position);
		this.rotation.add(bone.rotation);
		this.scale.add(bone.scale);
		return this;
	}

	public PlayerAnimBone applyOtherBone(PlayerAnimBone bone) {
		this.position.add(bone.position);
		this.rotation.add(bone.rotation);
		this.scale.mul(bone.scale);
		return this;
	}

	public PlayerAnimBone copyOtherBone(PlayerAnimBone bone) {
		this.position.set(bone.position);
		this.rotation.set(bone.rotation);
		this.scale.set(bone.scale);
		return this;
	}

	public PlayerAnimBone copyOtherBoneIfNotDisabled(PlayerAnimBone bone) {
		this.position.copyOtherIfNotDisabled(bone.position);
		this.rotation.copyOtherIfNotDisabled(bone.rotation);
		this.scale.copyOtherIfNotDisabled(bone.scale);
		return this;
	}

	public void setEnabled(boolean enabled) {
		this.position.setEnabled(enabled);
		this.rotation.setEnabled(enabled);
		this.scale.setEnabled(enabled);
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
		return getName().hashCode();
	}
}
