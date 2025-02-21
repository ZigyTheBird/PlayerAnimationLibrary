package com.zigythebird.playeranim.util;

import net.minecraft.util.Mth;

/**
 * Helper class for various methods and functions useful while rendering
 */
public final class RenderUtil {
	/**
	 * Special helper function for lerping yaw.
	 * <p>
	 * This exists because yaw in Minecraft handles its yaw a bit strangely, and can cause incorrect results if lerped without accounting for special-cases
	 */
	public static double lerpYaw(double delta, double start, double end) {
		start = Mth.wrapDegrees(start);
		end = Mth.wrapDegrees(end);
		double diff = start - end;
		end = diff > 180 || diff < -180 ? start + Math.copySign(360 - Math.abs(diff), diff) : end;

		return Mth.lerp(delta, start, end);
	}
}
