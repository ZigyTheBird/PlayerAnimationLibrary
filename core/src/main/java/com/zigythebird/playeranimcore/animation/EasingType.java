package com.zigythebird.playeranimcore.animation;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.zigythebird.playeranimcore.animation.keyframe.AnimationPoint;
import com.zigythebird.playeranimcore.animation.keyframe.Keyframe;
import com.zigythebird.playeranimcore.math.MathHelper;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.parser.ast.Expression;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	Map<String, EasingType> EASING_TYPES = new ConcurrentHashMap<>(64);

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
	EasingType CATMULLROM = register("catmullrom", new CatmullRomEasing());

	Float2FloatFunction buildTransformer(@Nullable Float value);

	static float lerpWithOverride(MochaEngine<?> env, AnimationPoint animationPoint, @Nullable EasingType override) {
		EasingType easingType = override;

		if (override == null)
			easingType = animationPoint.easingType();

		return easingType.apply(env, animationPoint);
	}

	default float apply(MochaEngine<?> env, AnimationPoint animationPoint) {
		Float easingVariable = null;

		if (animationPoint.easingArgs() != null && !animationPoint.easingArgs().isEmpty())
			easingVariable = env.eval(animationPoint.easingArgs().getFirst());

		return apply(env, animationPoint, easingVariable, animationPoint.currentTick() / animationPoint.transitionLength());
	}

	default float apply(MochaEngine<?> env, AnimationPoint animationPoint, @Nullable Float easingValue, float lerpValue) {
		if (lerpValue >= 1 || Float.isNaN(lerpValue))
			return animationPoint.animationEndValue();

		return apply(animationPoint.animationStartValue(), animationPoint.animationEndValue(), easingValue, lerpValue);
	}

	default float apply(float startValue, float endValue, float lerpValue) {
		return apply(startValue, endValue, null, lerpValue);
	}

	default float apply(float startValue, float endValue, @Nullable Float easingValue, float lerpValue) {
		return MathHelper.lerp(buildTransformer(easingValue).apply(lerpValue), startValue, endValue);
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
	 * Returns an easing function running forward in time
	 */
	static Float2FloatFunction easeIn(Float2FloatFunction function) {
		return function;
	}

	/**
	 * Returns an easing function running backwards in time
	 */
	static Float2FloatFunction easeOut(Float2FloatFunction function) {
		return time -> 1 - function.apply(1 - time);
	}

	/**
	 * Returns an easing function that runs equally both forwards and backwards in time based on the halfway point, generating a symmetrical curve
	 */
	static Float2FloatFunction easeInOut(Float2FloatFunction function) {
		return time -> {
			if (time < 0.5d) {
				return function.apply(time * 2f) / 2f;
			}

			return 1 - function.apply((1 - time) * 2f) / 2f;
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
		return 1 - MathHelper.cos(n * MathHelper.PI / 2f);
	}

	/**
	 * A circular function, equivalent to a normally symmetrical curve
	 * <p>
	 * {@code f(n) = 1 - sqrt(1 - n^2)}
	 * <p>
	 * <a href="http://easings.net/#easeInCirc">Easings.net#easeInCirc</a>
	 */
	static float circle(float n) {
		return 1 - MathHelper.sqrt(1 - n * n);
	}

	/**
	 * An exponential function, equivalent to an exponential curve
	 * <p>
	 * {@code f(n) = 2^(10 * (n - 1))}
	 * <p>
	 * <a href="http://easings.net/#easeInExpo">Easings.net#easeInExpo</a>
	 */
	static float exp(float n) {
		return MathHelper.pow(2, 10 * (n - 1));
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
	static Float2FloatFunction elastic(Float n) {
		float n2 = n == null ? 1 : n;

		return t -> 1 - MathHelper.pow(MathHelper.cos(t * MathHelper.PI / 2f), 3) * MathHelper.cos(t * n2 * MathHelper.PI);
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
	static Float2FloatFunction bounce(Float n) {
		final float n2 = n == null ? 0.5f : n;

		Float2FloatFunction one = x -> 121f / 16f * x * x;
		Float2FloatFunction two = x -> 121f / 4f * n2 * MathHelper.pow(x - 6f / 11f, 2) + 1 - n2;
		Float2FloatFunction three = x -> 121 * n2 * n2 * MathHelper.pow(x - 9f / 11f, 2) + 1 - n2 * n2;
		Float2FloatFunction four = x -> 484 * n2 * n2 * n2 * MathHelper.pow(x - 10.5f / 11f, 2) + 1 - n2 * n2 * n2;

		return t -> Math.min(Math.min(one.apply(t), two.apply(t)), Math.min(three.apply(t), four.apply(t)));
	}

	/**
	 * A negative elastic function, equivalent to inverting briefly before increasing
	 * <p>
	 * <code>f(t) = t^2 * ((n * 1.70158 + 1) * t - n * 1.70158)</code>
	 * <p>
	 * <a href="https://easings.net/#easeInBack">Easings.net#easeInBack</a>
	 */
	static Float2FloatFunction back(Float n) {
		final float n2 = n == null ? 1.70158f : n * 1.70158f;

		return t -> t * t * ((n2 + 1) * t - n2);
	}

	/**
	 * An exponential function, equivalent to an exponential curve to the {@code n} root
	 * <p>
	 * <code>f(t) = t^n</code>
	 *
	 * @param n The exponent
	 */
	static Float2FloatFunction pow(float n) {
		return t -> MathHelper.pow(t, n);
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
	static Float2FloatFunction step(Float n) {
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

	/**
	 * Custom EasingType implementation required for special-handling of spline-based interpolation
	 */
	class CatmullRomEasing implements EasingType {
		/**
		 * Generates a value from a given Catmull-Rom spline range with Centripetal parameterization (alpha=0.5)
		 * <p>
		 * Per standard implementation, this generates a spline curve over control points p1-p2, with p0 and p3
		 * acting as curve anchors.<br>
		 * We then apply the delta to determine the point on the generated spline to return.
		 * <p>
		 * Functionally equivalent to {@link Mth#catmullrom(float, float, float, float, float)}
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
			return easeIn(EasingType::linear);
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
}
