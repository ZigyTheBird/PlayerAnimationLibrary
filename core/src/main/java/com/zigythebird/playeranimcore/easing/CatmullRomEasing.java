package com.zigythebird.playeranimcore.easing;

import com.zigythebird.playeranimcore.animation.keyframe.AnimationPoint;
import com.zigythebird.playeranimcore.math.MathHelper;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.parser.ast.Expression;

import java.util.List;

/**
 * Custom EasingType implementation required for special-handling of spline-based interpolation
 */
public class CatmullRomEasing implements EasingTypeTransformer {
    /**
     * Generates a value from a given Catmull-Rom spline range with Centripetal parameterization (alpha=0.5)
     * <p>
     * Per standard implementation, this generates a spline curve over control points p1-p2, with p0 and p3
     * acting as curve anchors.<br>
     * We then apply the delta to determine the point on the generated spline to return.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Centripetal_Catmull%E2%80%93Rom_spline">Wikipedia</a>
     */
    public static float getPointOnSpline(float delta, float p0, float p1, float p2, float p3) {
        return 0.5f * (2f * p1 + (p2 - p0) * delta +
                (2f * p0 - 5f * p1 + 4f * p2 - p3) * delta * delta +
                (3f * p1 - p0 - 3f * p2 + p3) * delta * delta * delta);
    }

    @Override
    public Float2FloatFunction buildTransformer(Float value) {
        return EasingType.easeIn(EasingType::linear);
    }

    @Override
    public float apply(MochaEngine<?> env, AnimationPoint animationPoint, @Nullable Float easingValue, float lerpValue)  {
        if (animationPoint.currentTick() >= animationPoint.transitionLength())
            return animationPoint.animationEndValue();

        List<List<Expression>> easingArgs = animationPoint.easingArgs();

        if (easingArgs.size() < 2)
            return MathHelper.lerp(buildTransformer(easingValue).apply(lerpValue), animationPoint.animationStartValue(), animationPoint.animationEndValue());

        return getPointOnSpline(lerpValue, env.eval(easingArgs.get(0)), animationPoint.animationStartValue(), animationPoint.animationEndValue(), env.eval(easingArgs.get(1)));
    }
}
