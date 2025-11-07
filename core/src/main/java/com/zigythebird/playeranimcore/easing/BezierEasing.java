package com.zigythebird.playeranimcore.easing;

import com.zigythebird.playeranimcore.animation.keyframe.AnimationPoint;
import com.zigythebird.playeranimcore.math.MathHelper;
import com.zigythebird.playeranimcore.math.ModVector2d;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.runtime.standard.MochaMath;

import java.util.ArrayList;
import java.util.List;

abstract class BezierEasing implements EasingTypeTransformer {
    @Override
    public Float2FloatFunction buildTransformer(@Nullable Float value) {
        return EasingType.easeIn(EasingType::linear);
    }

    abstract boolean isEasingBefore();

    @Override
    public float apply(MochaEngine<?> env, AnimationPoint animationPoint, @Nullable Float easingValue, float lerpValue) {
        List<List<Expression>> easingArgs = animationPoint.easingArgs();
        if (easingArgs.isEmpty())
            return MathHelper.lerp(buildTransformer(easingValue).apply(lerpValue), animationPoint.animationStartValue(), animationPoint.animationEndValue());

        float rightValue = isEasingBefore() ? 0 : env.eval(easingArgs.getFirst());
        float rightTime = isEasingBefore() ? 0.1f : env.eval(easingArgs.get(1));
        float leftValue = isEasingBefore() ? env.eval(easingArgs.getFirst()) : 0;
        float leftTime = isEasingBefore() ? env.eval(easingArgs.get(1)) : -0.1f;

        if (easingArgs.size() > 3) {
            rightValue = env.eval(easingArgs.get(2));
            rightTime = env.eval(easingArgs.get(3));
        }

        leftValue = (float) Math.toRadians(leftValue);
        rightValue = (float) Math.toRadians(rightValue);

        float gapTime = animationPoint.transitionLength()/20;

        float time_handle_before = Math.clamp(rightTime, 0, gapTime);
        float time_handle_after  = Math.clamp(leftTime, -gapTime, 0);

        CubicBezierCurve curve = new CubicBezierCurve(
                new ModVector2d(0, animationPoint.animationStartValue()),
                new ModVector2d(time_handle_before, animationPoint.animationStartValue() + rightValue),
                new ModVector2d(time_handle_after + gapTime, animationPoint.animationEndValue() + leftValue),
                new ModVector2d(gapTime, animationPoint.animationEndValue()));
        float time = gapTime * lerpValue;

        List<ModVector2d> points = curve.getPoints(200);
        ModVector2d closest  = new ModVector2d();
        float closest_diff = Float.POSITIVE_INFINITY;
        for (ModVector2d point : points) {
            float diff = Math.abs(point.x - time);
            if (diff < closest_diff) {
                closest_diff = diff;
                closest.set(point);
            }
        }
        ModVector2d second_closest = new ModVector2d();
        closest_diff = Float.POSITIVE_INFINITY;
        for (ModVector2d point : points) {
            if (point == closest) continue;
            float diff = Math.abs(point.x - time);
            if (diff < closest_diff) {
                closest_diff = diff;
                second_closest.set(closest);
                second_closest.set(point);
            }
        }
        return MathHelper.lerp(Math.clamp(MathHelper.lerp(time, closest.x, second_closest.x), 0, 1), closest.y, second_closest.y);
    }
}

class BezierEasingBefore extends BezierEasing {
    @Override
    boolean isEasingBefore() {
        return true;
    }
}

class BezierEasingAfter extends BezierEasing {
    @Override
    boolean isEasingBefore() {
        return false;
    }
}

class CubicBezierCurve {
    private ModVector2d v0;
    private ModVector2d v1;
    private ModVector2d v2;
    private ModVector2d v3;

    public CubicBezierCurve(ModVector2d v0, ModVector2d v1, ModVector2d v2, ModVector2d v3) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public ModVector2d getPoint(float t) {
        return getPoint(t, new ModVector2d());
    }

    public ModVector2d getPoint(float t, ModVector2d target) {
        if (target == null) {
            target = new ModVector2d();
        }

        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;

        target.x = uuu * v0.x + 3 * uu * t * v1.x + 3 * u * tt * v2.x + ttt * v3.x;
        target.y = uuu * v0.y + 3 * uu * t * v1.y + 3 * u * tt * v2.y + ttt * v3.y;

        return target;
    }

    public List<ModVector2d> getPoints(int divisions) {
        List<ModVector2d> points = new ArrayList<>();

        for (int i = 0; i <= divisions; i++) {
            points.add(getPoint((float) i / divisions));
        }

        return points;
    }
}
