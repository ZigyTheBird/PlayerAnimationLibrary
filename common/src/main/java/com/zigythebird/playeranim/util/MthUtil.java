package com.zigythebird.playeranim.util;

import org.joml.Vector3f;

/**
 * Some casts to {@link float} to make my life easier.
 */
public class MthUtil {
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

    /**
     * <a href="https://github.com/KosmX/minecraftPlayerAnimator/blob/1.21/coreLib/src/main/java/dev/kosmx/playerAnim/core/data/KeyframeAnimation.java#L557">KeyframeAnimation.java#557</a>
     */
    public static float toRadians(float value) {
        return value * 0.01745329251f;
    }
}
