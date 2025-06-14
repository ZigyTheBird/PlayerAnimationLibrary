package com.zigythebird.playeranimcore.math;

import org.joml.Vector3f;
/**
 * Some casts to {@link float} to make my life easier.
 */
public class MathHelper {
    public static final Vector3f ZERO = new Vector3f();
    public static final float PI = (float) Math.PI;

    public static float pow(float a, float b) {
        return (float) Math.pow(a, b);
    }

    public static float cos(float a) {
        return (float) Math.cos(a);
    }

    public static float sqrt(float a) {
        return (float) Math.sqrt(a);
    }

    public static float toRadians(float value) {
        return value * 0.01745329251f;
    }

    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }
}
