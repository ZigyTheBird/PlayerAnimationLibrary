package com.zigythebird.playeranimcore.math;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranimcore.animation.keyframe.Keyframe;
import com.zigythebird.playeranimcore.animation.keyframe.KeyframeStack;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.enums.Axis;
import com.zigythebird.playeranimcore.enums.TransformType;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;

import java.util.List;

public class ExtendedVector3f extends Vector3f {
    public Float xTransitionLength = null;
    public Float yTransitionLength = null;
    public Float zTransitionLength = null;

    public boolean xEnabled = true;
    public boolean yEnabled = true;
    public boolean zEnabled = true;

    public ExtendedVector3f() {
        // no-op
    }

    public ExtendedVector3f(float d) {
        super(d);
    }

    public ExtendedVector3f(float x, float y, float z) {
        super(x, y, z);
    }

    public ExtendedVector3f copyOtherIfNotDisabled(ExtendedVector3f vec) {
        if (vec.xEnabled) this.x = vec.x;
        if (vec.yEnabled) this.y = vec.y;
        if (vec.zEnabled) this.z = vec.z;
        return this;
    }

    @ApiStatus.Internal
    public void beginOrEndTickLerp(String name, ExtendedVector3f bone, float animTime, Animation animation, TransformType type) {
        if (bone.xEnabled) this.x = beginOrEndTickLerp(name, this.x, bone.x, bone.xTransitionLength, animTime, animation, type, Axis.X);
        if (bone.yEnabled) this.y = beginOrEndTickLerp(name, this.y, bone.y, bone.yTransitionLength, animTime, animation, type, Axis.Y);
        if (bone.zEnabled) this.z = beginOrEndTickLerp(name, this.z, bone.z, bone.zTransitionLength, animTime, animation, type, Axis.Z);
    }

    private static float beginOrEndTickLerp(String name, float startValue, float endValue, Float transitionLength, float animTime, Animation animation, TransformType type, Axis axis) {
        EasingType easingType = EasingType.EASE_IN_OUT_SINE;
        if (animation != null) {
            float temp = startValue;
            startValue = endValue;
            endValue = temp;

            if (transitionLength == null) transitionLength = animation.length() - (float) animation.data().getRaw(ExtraAnimationData.END_TICK_KEY);

            if (animation.data().has(ExtraAnimationData.EASING_BEFORE_KEY) && !(boolean) animation.data().getRaw(ExtraAnimationData.EASING_BEFORE_KEY)) {
                BoneAnimation boneAnimation = animation.getBone(name);
                KeyframeStack keyframeStack = boneAnimation == null ? null : switch (type) {
                    case BEND -> {
                        List<Keyframe> bendKeyFrames = boneAnimation.bendKeyFrames();
                        if (!bendKeyFrames.isEmpty()) easingType = bendKeyFrames.getLast().easingType();
                        yield null;
                    }
                    case ROTATION -> boneAnimation.rotationKeyFrames();
                    case SCALE -> boneAnimation.scaleKeyFrames();
                    case POSITION -> boneAnimation.positionKeyFrames();
                };
                if (keyframeStack != null) {
                    List<Keyframe> keyFrames = keyframeStack.getKeyFramesForAxis(axis);
                    if (!keyFrames.isEmpty()) easingType = keyFrames.getLast().easingType();
                }
            }
            if (easingType == EasingType.BEZIER || easingType == EasingType.BEZIER_AFTER || easingType == EasingType.CATMULLROM)
                easingType = EasingType.EASE_IN_OUT_SINE;
        }
        if (transitionLength == null) return endValue;
        return easingType.apply(startValue, endValue, animTime / transitionLength);
    }

    public void setEnabled(boolean enabled) {
        this.xEnabled = enabled;
        this.yEnabled = enabled;
        this.zEnabled = enabled;
    }

    public void setXTransitionLength(Float xTransitionLength) {
        this.xTransitionLength = xTransitionLength;
    }

    public void setYTransitionLength(Float yTransitionLength) {
        this.yTransitionLength = yTransitionLength;
    }

    public void setZTransitionLength(Float zTransitionLength) {
        this.zTransitionLength = zTransitionLength;
    }
}
