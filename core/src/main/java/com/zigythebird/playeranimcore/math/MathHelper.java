package com.zigythebird.playeranimcore.math;

import static team.unnamed.mocha.runtime.standard.MochaMath.sqrt;

/**
 * Some casts to {@link float} to make my life easier.
 */
public class MathHelper {
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

    public static float cosFromSin(float sin, float angle) {
        float cos = sqrt(1.0F - sin * sin);
        float a = angle + ((float)java.lang.Math.PI / 2F);
        float b = a - (float)((int)(a / ((float)java.lang.Math.PI * 2F))) * ((float)java.lang.Math.PI * 2F);
        if ((double)b < (double)0.0F) {
            b += ((float)java.lang.Math.PI * 2F);
        }

        return b >= (float)java.lang.Math.PI ? -cos : cos;
    }

    public static boolean absEqualsOne(float r) {
        return (Float.floatToRawIntBits(r) & Integer.MAX_VALUE) == 1065353216;
    }

    public static float safeAsin(float r) {
        return r <= -1.0F ? (-(float) Math.PI / 2F) : (float) (r >= 1.0F ? ((float) Math.PI / 2F) : Math.asin(r));
    }
    
    public static float length(float x, float y, float z, float w) {
        return (float) Math.sqrt(Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w))));
    }
}
