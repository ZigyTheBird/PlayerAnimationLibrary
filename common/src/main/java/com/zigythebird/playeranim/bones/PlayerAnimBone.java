package com.zigythebird.playeranim.bones;

import com.zigythebird.playeranim.animation.Animation;
import com.zigythebird.playeranim.animation.EasingType;
import com.zigythebird.playeranim.animation.TransformType;
import com.zigythebird.playeranim.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranim.animation.keyframe.Keyframe;
import com.zigythebird.playeranim.animation.keyframe.KeyframeStack;
import com.zigythebird.playeranim.math.Pair;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3d;

import java.util.Arrays;
import java.util.Objects;

/**
 * This is the object that is directly modified by animations to handle movement
 */
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

	@ApiStatus.Internal
	public void beginOrEndTickLerp(AdvancedPlayerAnimBone bone, float animTime, Animation animation) {
		this.positionX = beginOrEndTickLerp(positionX, bone.positionX, bone.positionXTransitionLength, animTime, animation, TransformType.POSITION, Direction.Axis.X);
		this.positionY = beginOrEndTickLerp(positionY, bone.positionY, bone.positionYTransitionLength, animTime, animation, TransformType.POSITION, Direction.Axis.Y);
		this.positionZ = beginOrEndTickLerp(positionZ, bone.positionZ, bone.positionZTransitionLength, animTime, animation, TransformType.POSITION, Direction.Axis.Z);

		this.rotX = beginOrEndTickLerp(rotX, bone.rotX, bone.rotXTransitionLength, animTime, animation, TransformType.ROTATION, Direction.Axis.X);
		this.rotY = beginOrEndTickLerp(rotY, bone.rotY, bone.rotYTransitionLength, animTime, animation, TransformType.ROTATION, Direction.Axis.Y);
		this.rotZ = beginOrEndTickLerp(rotZ, bone.rotZ, bone.rotZTransitionLength, animTime, animation, TransformType.ROTATION, Direction.Axis.Z);

		this.scaleX = beginOrEndTickLerp(scaleX, bone.scaleX, bone.scaleXTransitionLength, animTime, animation, TransformType.SCALE, Direction.Axis.X);
		this.scaleY = beginOrEndTickLerp(scaleY, bone.scaleY, bone.scaleYTransitionLength, animTime, animation, TransformType.SCALE, Direction.Axis.Y);
		this.scaleZ = beginOrEndTickLerp(scaleZ, bone.scaleZ, bone.scaleZTransitionLength, animTime, animation, TransformType.SCALE, Direction.Axis.Z);

		this.bendAxis = beginOrEndTickLerp(bendAxis, bone.bendAxis, bone.bendAxisTransitionLength, animTime, animation, TransformType.BEND, Direction.Axis.X);
		this.bend = beginOrEndTickLerp(bend, bone.bend, bone.bendTransitionLength, animTime, animation, TransformType.BEND, Direction.Axis.Y);
	}
	
	private float beginOrEndTickLerp(float startValue, float endValue, Float transitionLength, float animTime, Animation animation, TransformType type, Direction.Axis axis) {
		if (!Float.isNaN(endValue)) {
			if (animation != null) {
				float temp = startValue;
				startValue = endValue;
				endValue = temp;
			}
			if (transitionLength != null) {
				EasingType easingType = EasingType.EASE_IN_OUT_SINE;
				try {
					if (!(boolean) animation.data().get("isEasingBefore")) {
						BoneAnimation boneAnimation = Arrays.stream(animation.boneAnimations()).filter(bone -> Objects.equals(bone.boneName(), this.getName())).findFirst().get();
						KeyframeStack<Keyframe> keyframeStack;
						switch (type) {
							case BEND -> keyframeStack = boneAnimation.bendKeyFrames();
							case ROTATION -> keyframeStack = boneAnimation.rotationKeyFrames();
							case SCALE -> keyframeStack = boneAnimation.scaleKeyFrames();
							default -> keyframeStack = boneAnimation.positionKeyFrames();
						}
						switch (axis) {
							case X -> easingType = keyframeStack.xKeyframes().getLast().easingType();
							case Y -> easingType = keyframeStack.yKeyframes().getLast().easingType();
							default -> easingType = keyframeStack.zKeyframes().getLast().easingType();
						}
					}
				} catch (Exception ignore) {}
				return (float) easingType.apply(startValue, endValue, animTime / transitionLength);
			}
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
