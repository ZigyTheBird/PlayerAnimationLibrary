package com.zigythebird.playeranim.animation;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.zigythebird.playeranim.ModInit;
import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;
import net.minecraft.util.Mth;
import com.zigythebird.playeranim.animation.keyframe.AnimationPoint;
import com.zigythebird.playeranim.animation.keyframe.Keyframe;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Functional interface defining an easing function
 * <p>
 * {@code value} is the easing value provided from the keyframe's {@link Keyframe#easingArgs()}
 *
 * @see <a href="https://easings.net/">Easings.net</a>
 * @see <a href="https://cubic-bezier.com">Cubic-Bezier.com</a>
 */
@FunctionalInterface
public interface EasingType {
	final Map<String, EasingType> EASING_TYPES = new ConcurrentHashMap<>(64);

	EasingType LINEAR = register("linear", register("none", value -> easeIn(EasingType::linear)));
	EasingType STEP = register("step", value -> easeIn(step(value)));
	EasingType EASE_IN_SINE = register("easeinsine", value -> easeIn(EasingType::sine));
	EasingType EASE_OUT_SINE = register("easeoutsine", value -> easeOut(EasingType::sine));
	EasingType EASE_IN_OUT_SINE = register("easeinoutsine", value -> easeInOut(EasingType::sine));
	EasingType EASE_IN_QUAD = register("easeinquad", value -> easeIn(EasingType::quadratic));
	EasingType EASE_OUT_QUAD = register("easeoutquad", value -> easeOut(EasingType::quadratic));
	EasingType EASE_IN_OUT_QUAD = register("easeinoutquad", value -> easeInOut(EasingType::quadratic));
	EasingType EASE_IN_CUBIC = register("easeincubic", value -> easeIn(EasingType::cubic));
	EasingType EASE_OUT_CUBIC = register("easeoutcubic", value -> easeOut(EasingType::cubic));
	EasingType EASE_IN_OUT_CUBIC = register("easeinoutcubic", value -> easeInOut(EasingType::cubic));
	EasingType EASE_IN_QUART = register("easeinquart", value -> easeIn(pow(4)));
	EasingType EASE_OUT_QUART = register("easeoutquart", value -> easeOut(pow(4)));
	EasingType EASE_IN_OUT_QUART = register("easeinoutquart", value -> easeInOut(pow(4)));
	EasingType EASE_IN_QUINT = register("easeinquint", value -> easeIn(pow(4)));
	EasingType EASE_OUT_QUINT = register("easeoutquint", value -> easeOut(pow(5)));
	EasingType EASE_IN_OUT_QUINT = register("easeinoutquint", value -> easeInOut(pow(5)));
	EasingType EASE_IN_EXPO = register("easeinexpo", value -> easeIn(EasingType::exp));
	EasingType EASE_OUT_EXPO = register("easeoutexpo", value -> easeOut(EasingType::exp));
	EasingType EASE_IN_OUT_EXPO = register("easeinoutexpo", value -> easeInOut(EasingType::exp));
	EasingType EASE_IN_CIRC = register("easeincirc", value -> easeIn(EasingType::circle));
	EasingType EASE_OUT_CIRC = register("easeoutcirc", value -> easeOut(EasingType::circle));
	EasingType EASE_IN_OUT_CIRC = register("easeinoutcirc", value -> easeInOut(EasingType::circle));
	EasingType EASE_IN_BACK = register("easeinback", value -> easeIn(back(value)));
	EasingType EASE_OUT_BACK = register("easeoutback", value -> easeOut(back(value)));
	EasingType EASE_IN_OUT_BACK = register("easeinoutback", value -> easeInOut(back(value)));
	EasingType EASE_IN_ELASTIC = register("easeinelastic", value -> easeIn(elastic(value)));
	EasingType EASE_OUT_ELASTIC = register("easeoutelastic", value -> easeOut(elastic(value)));
	EasingType EASE_IN_OUT_ELASTIC = register("easeinoutelastic", value -> easeInOut(elastic(value)));
	EasingType EASE_IN_BOUNCE = register("easeinbounce", value -> easeIn(bounce(value)));
	EasingType EASE_OUT_BOUNCE = register("easeoutbounce", value -> easeOut(bounce(value)));
	EasingType EASE_IN_OUT_BOUNCE = register("easeinoutbounce", value -> easeInOut(bounce(value)));
	EasingType CATMULLROM = register("catmullrom", value -> easeInOut(EasingType::catmullRom));

	Function<Float, Float> buildTransformer(Float value);

	static double lerpWithOverride(MolangEnvironment env, AnimationPoint animationPoint, EasingType override) {
		EasingType easingType = override;

		if (override == null)
			easingType = animationPoint.keyFrame() == null ? LINEAR : animationPoint.keyFrame().easingType();

		return easingType.apply(env, animationPoint);
	}

	default double apply(MolangEnvironment env, AnimationPoint animationPoint) {
		Float easingVariable = null;

		try {
			if (animationPoint.keyFrame() != null && !animationPoint.keyFrame().easingArgs().isEmpty())
				easingVariable = env.resolve(animationPoint.keyFrame().easingArgs().getFirst());
		} catch (MolangRuntimeException e) {
			ModInit.LOGGER.error(e.getMessage());
		}

		return apply(animationPoint, easingVariable, animationPoint.currentTick() / animationPoint.transitionLength());
	}

	default double apply(AnimationPoint animationPoint, Float easingValue, float lerpValue) {
		if (animationPoint.currentTick() >= animationPoint.transitionLength())
			return (float)animationPoint.animationEndValue();

		return Mth.lerp(buildTransformer(easingValue).apply(lerpValue), animationPoint.animationStartValue(), animationPoint.animationEndValue());
	}

	/**
	 * Register an {@code EasingType} with Geckolib for handling animation transitions and value curves
	 * <p>
	 * <b><u>MUST be called during mod construct</u></b>
	 * <p>
	 * It is recommended you don't call this directly, and instead call it via {@code GeckoLibUtil#addCustomEasingType}
	 *
	 * @param name The name of the easing type
	 * @param easingType The {@code EasingType} to associate with the given name
	 * @return The {@code EasingType} you registered
	 */
	static EasingType register(String name, EasingType easingType) {
		EASING_TYPES.putIfAbsent(name, easingType);

		return easingType;
	}

	/**
	 * Retrieve an {@code EasingType} instance based on a {@link JsonElement}. Returns one of the default {@code EasingTypes} if the name matches, or any other registered {@code EasingType} with a matching name
	 *
	 * @param json The {@code easing} {@link JsonElement} to attempt to parse.
	 * @return A usable {@code EasingType} instance
	 */
	static EasingType fromJson(JsonElement json) {
		if (!(json instanceof JsonPrimitive primitive) || !primitive.isString())
			return LINEAR;

		return fromString(primitive.getAsString().toLowerCase(Locale.ROOT));
	}

	/**
	 * Get an existing {@code EasingType} from a given string, matching the string to its name
	 *
	 * @param name The name of the easing function
	 * @return The relevant {@code EasingType}, or {@link EasingType#LINEAR} if none match
	 */
	static EasingType fromString(String name) {
		return EASING_TYPES.getOrDefault(name, EasingType.LINEAR);
	}

	// ---> Easing Transition Type Functions <--- //

	/**
	 * Returns an easing function running linearly. Functionally equivalent to no easing
	 */
	static Function<Float, Float> linear(Function<Float, Float> function) {
		return function;
	}
	
	/**
	 * Performs a Catmull-Rom interpolation, used to get smooth interpolated motion between keyframes
	 * <p>
	 * <a href="https://pub.dev/documentation/latlong2/latest/spline/CatmullRom-class.html">CatmullRom#position</a>
	 */
	static float catmullRom(float n) {
		return (0.5f * (2.0f * (n + 1) + ((n + 2) - n) * 1
				+ (2.0f * n - 5.0f * (n + 1) + 4.0f * (n + 2) - (n + 3)) * 1
				+ (3.0f * (n + 1) - n - 3.0f * (n + 2) + (n + 3)) * 1));
	}

	/**
	 * Returns an easing function running forward in time
	 */
	static Function<Float, Float> easeIn(Function<Float, Float> function) {
		return function;
	}

	/**
	 * Returns an easing function running backwards in time
	 */
	static Function<Float, Float> easeOut(Function<Float, Float> function) {
		return time -> 1 - function.apply(1 - time);
	}

	/**
	 * Returns an easing function that runs equally both forwards and backwards in time based on the halfway point, generating a symmetrical curve
	 */
	static Function<Float, Float> easeInOut(Function<Float, Float> function) {
		return time -> {
			if (time < 0.5F) {
				return function.apply(time * 2F) / 2F;
			}

			return 1 - function.apply((1 - time) * 2F) / 2F;
		};
	}

	// ---> Mathematical Functions <--- //

	/**
	 * A linear function, equivalent to a null-operation
	 * <p>
	 * {@code f(n) = n}
	 */
	static float linear(float n) {
		return n;
	}

	/**
	 * A quadratic function, equivalent to the square (<i>n</i>^2) of elapsed time
	 * <p>
	 * {@code f(n) = n^2}
	 * <p>
	 * <a href="http://easings.net/#easeInQuad">Easings.net#easeInQuad</a>
	 */
	static float quadratic(float n) {
		return n * n;
	}

	/**
	 * A cubic function, equivalent to cube (<i>n</i>^3) of elapsed time
	 * <p>
	 * {@code f(n) = n^3}
	 * <p>
	 * <a href="http://easings.net/#easeInCubic">Easings.net#easeInCubic</a>
	 */
	static float cubic(float n) {
		return n * n * n;
	}

	/**
	 * A sinusoidal function, equivalent to a sine curve output
	 * <p>
	 * {@code f(n) = 1 - cos(n * π / 2)}
	 * <p>
	 * <a href="http://easings.net/#easeInSine">Easings.net#easeInSine</a>
	 */
	static float sine(float n) {
		return 1 - (float) Math.cos(n * Math.PI / 2F);
	}

	/**
	 * A circular function, equivalent to a normally symmetrical curve
	 * <p>
	 * {@code f(n) = 1 - sqrt(1 - n^2)}
	 * <p>
	 * <a href="http://easings.net/#easeInCirc">Easings.net#easeInCirc</a>
	 */
	static float circle(float n) {
		return 1 - (float) Math.sqrt(1 - n * n);
	}

	/**
	 * An exponential function, equivalent to an exponential curve
	 * <p>
	 * {@code f(n) = 2^(10 * (n - 1))}
	 * <p>
	 * <a href="http://easings.net/#easeInExpo">Easings.net#easeInExpo</a>
	 */
	static float exp(float n) {
		return (float) Math.pow(2, 10 * (n - 1));
	}

	// ---> Easing Curve Functions <--- //

	/**
	 * An elastic function, equivalent to an oscillating curve
	 * <p>
	 * <i>n</i> defines the elasticity of the output
	 * <p>
	 * {@code f(t) = 1 - (cos(t * π) / 2))^3 * cos(t * n * π)}
	 * <p>
	 * <a href="http://easings.net/#easeInElastic">Easings.net#easeInElastic</a>
	 */
	static Function<Float, Float> elastic(Float n) {
		float n2 = n == null ? 1 : n;

		return t -> (float) (1 - Math.pow(Math.cos(t * Math.PI / 2f), 3) * Math.cos(t * n2 * Math.PI));
	}

	/**
	 * A bouncing function, equivalent to a bouncing ball curve
	 * <p>
	 * <i>n</i> defines the bounciness of the output
	 * <p>
	 * Thanks to <b>Waterded#6455</b> for making the bounce adjustable, and <b>GiantLuigi4#6616</b> for additional cleanup
	 * <p>
	 * <a href="http://easings.net/#easeInBounce">Easings.net#easeInBounce</a>
	 */
	static Function<Float, Float> bounce(Float n) {
		final float n2 = n == null ? 0.5F : n;

		Function<Float, Float> one = x -> 121f / 16f * x * x;
		Function<Float, Float> two = x -> (float) (121f / 4f * n2 * Math.pow(x - 6f / 11f, 2) + 1 - n2);
		Function<Float, Float> three = x -> (float) (121 * n2 * n2 * Math.pow(x - 9f / 11f, 2) + 1 - n2 * n2);
		Function<Float, Float> four = x -> (float) (484 * n2 * n2 * n2 * Math.pow(x - 10.5f / 11f, 2) + 1 - n2 * n2 * n2);

		return t -> Math.min(Math.min(one.apply(t), two.apply(t)), Math.min(three.apply(t), four.apply(t)));
	}

	/**
	 * A negative elastic function, equivalent to inverting briefly before increasing
	 * <p>
	 * <code>f(t) = t^2 * ((n * 1.70158 + 1) * t - n * 1.70158)</code>
	 * <p>
	 * <a href="https://easings.net/#easeInBack">Easings.net#easeInBack</a>
	 */
	static Function<Float, Float> back(Float n) {
		final float n2 = n == null ? 1.70158F : n * 1.70158F;

		return t -> t * t * ((n2 + 1) * t - n2);
	}

	/**
	 * An exponential function, equivalent to an exponential curve to the {@code n} root
	 * <p>
	 * <code>f(t) = t^n</code>
	 *
	 * @param n The exponent
	 */
	static Function<Float, Float> pow(float n) {
		return t -> (float) Math.pow(t, n);
	}

	// The MIT license notice below applies to the function step
	/**
	 * The MIT License (MIT)
	 *<br><br>
	 * Copyright (c) 2015 Boris Chumichev
	 *<br><br>
	 * Permission is hereby granted, free of charge, to any person obtaining a copy
	 * of this software and associated documentation files (the "Software"), to deal
	 * in the Software without restriction, including without limitation the rights
	 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	 * copies of the Software, and to permit persons to whom the Software is
	 * furnished to do so, subject to the following conditions:
	 *<br><br>
	 * The above copyright notice and this permission notice shall be included in
	 * all copies or substantial portions of the Software.
	 *<br><br>
	 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	 * SOFTWARE.
	 * <br><br>
	 * Returns a stepped value based on the nearest step to the input value.<br>
	 * The size (grade) of the steps depends on the provided value of {@code n}
	 **/
	static Function<Float, Float> step(Float n) {
		float n2 = n == null ? 2 : n;

		if (n2 < 2)
			throw new IllegalArgumentException("Steps must be >= 2, got: " + n2);

		final int steps = (int)n2;

		return t -> {
			float result = 0;

			if (t < 0)
				return result;

			float stepLength = (1 / (float)steps);

			if (t > (result = (steps - 1) * stepLength))
				return result;

			int testIndex;
			int leftBorderIndex = 0;
			int rightBorderIndex = steps - 1;

			while (rightBorderIndex - leftBorderIndex != 1) {
				testIndex = leftBorderIndex + (rightBorderIndex - leftBorderIndex) / 2;

				if (t >= testIndex * stepLength) {
					leftBorderIndex = testIndex;
				}
				else {
					rightBorderIndex = testIndex;
				}
			}

			return leftBorderIndex * stepLength;
		};
	}
}
