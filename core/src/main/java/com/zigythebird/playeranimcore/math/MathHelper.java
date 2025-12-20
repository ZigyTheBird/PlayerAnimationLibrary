package com.zigythebird.playeranimcore.math;

import team.unnamed.mocha.runtime.standard.MochaMath;

/**
 * Some casts to {@link float} to make my life easier.
 */
public class MathHelper {
    public static float cos(float a) {
        return (float) Math.cos(a);
    }

    public static double cosFromSin(double sin, double angle) {
        double cos = Math.sqrt(1.0F - sin * sin);
        double a = angle + (Math.PI / 2F);
        double b = a - ((int)(a / (Math.PI * 2))) * (Math.PI * 2);
        if (b < 0) {
            b += (Math.PI * 2F);
        }

        return b >= Math.PI ? -cos : cos;
    }

    public static boolean absEqualsOne(double r) {
        return (Double.doubleToRawLongBits(r) & 0x7FFFFFFFFFFFFFFFL) == 0x3FF0000000000000L;
    }
    
    public static float length(float x, float y, float z, float w) {
        return MochaMath.sqrt(Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w))));
    }

    public static double length(double x, double y, double z, double w) {
        return Math.sqrt(Math.fma(x, x, Math.fma(y, y, Math.fma(z, z, w * w))));
    }
}
