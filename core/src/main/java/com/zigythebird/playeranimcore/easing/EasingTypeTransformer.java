package com.zigythebird.playeranimcore.easing;

import com.zigythebird.playeranimcore.animation.keyframe.AnimationPoint;
import com.zigythebird.playeranimcore.math.MathHelper;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.value.ObjectValue;

@FunctionalInterface
public interface EasingTypeTransformer extends ObjectValue.FloatFunction3 {
    Float2FloatFunction buildTransformer(@Nullable Float value);

    default float apply(MochaEngine<?> env, AnimationPoint animationPoint) {
        Float easingVariable = null;

        if (animationPoint.easingArgs() != null && !animationPoint.easingArgs().isEmpty())
            easingVariable = env.eval(animationPoint.easingArgs().getFirst());

        return apply(env, animationPoint, easingVariable, animationPoint.currentTick() / animationPoint.transitionLength());
    }

    default float apply(MochaEngine<?> env, AnimationPoint animationPoint, @Nullable Float easingValue, float lerpValue) {
        if (lerpValue >= 1)
            return animationPoint.animationEndValue();
        if (Float.isNaN(lerpValue))
            return animationPoint.animationStartValue();

        return apply(animationPoint.animationStartValue(), animationPoint.animationEndValue(), easingValue, lerpValue);
    }

    @Override
    default float apply(float startValue, float endValue, float lerpValue) {
        return apply(startValue, endValue, null, lerpValue);
    }

    default float apply(float startValue, float endValue, @Nullable Float easingValue, float lerpValue) {
        return MathHelper.lerp(buildTransformer(easingValue).apply(lerpValue), startValue, endValue);
    }
}
