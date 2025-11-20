/*
 * MIT License
 *
 * Copyright (c) 2024 GeckoLib
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.zigythebird.playeranimcore.loading;

import com.google.gson.*;
import com.zigythebird.playeranimcore.PlayerAnimLib;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranimcore.animation.keyframe.Keyframe;
import com.zigythebird.playeranimcore.animation.keyframe.KeyframeStack;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.math.Vec3f;
import com.zigythebird.playeranimcore.molang.MolangLoader;
import com.zigythebird.playeranimcore.util.JsonUtil;
import it.unimi.dsi.fastutil.floats.FloatObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.parser.ast.FloatExpression;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationLoader implements JsonDeserializer<Animation> {
	public static String OVERRIDE_PREVIOUS_ANIMATION = "override_previous_animation";

	private static final List<List<Expression>> ZERO_ARRAY;
	private static final List<List<Expression>> ZERO_POINT_ONE_ARRAY;
	private static final List<List<Expression>> MINUS_ZERO_POINT_ONE_ARRAY;

	@Override
	public Animation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject animationObj = json.getAsJsonObject();

		float length = animationObj.has("animation_length") ? JsonUtil.getAsFloat(animationObj, "animation_length") * 20f : -1;
		Map<String, BoneAnimation> boneAnimations = bakeBoneAnimations(JsonUtil.getAsJsonObject(animationObj, "bones", new JsonObject()));
		if (length == -1) length = calculateAnimationLength(boneAnimations);

		Animation.LoopType loopType = readLoopType(animationObj, length);
		Animation.Keyframes keyframes = context.deserialize(animationObj, Animation.Keyframes.class);

		Map<String, String> parents = UniversalAnimLoader.getParents(JsonUtil.getAsJsonObject(animationObj, "parents", new JsonObject()));
		Map<String, Vec3f> bones = UniversalAnimLoader.getModel(JsonUtil.getAsJsonObject(animationObj, "model", new JsonObject()));

		// Extra data
		ExtraAnimationData extraData = new ExtraAnimationData();
		if (animationObj.has(OVERRIDE_PREVIOUS_ANIMATION)) {
			if (animationObj.get(OVERRIDE_PREVIOUS_ANIMATION) instanceof JsonPrimitive primitive && primitive.isBoolean() && primitive.getAsBoolean()) {
				extraData.put(ExtraAnimationData.DISABLE_AXIS_IF_NOT_MODIFIED, false);
			}
			else throw new JsonParseException("Expected boolean value for override_previous_animation, but got " + animationObj.get(OVERRIDE_PREVIOUS_ANIMATION));
		}
		if (animationObj.has(PlayerAnimLib.MOD_ID)) {
			extraData.fromJson(animationObj.getAsJsonObject(PlayerAnimLib.MOD_ID), false);
		}

		return new Animation(extraData, length, loopType, boneAnimations, keyframes, bones, parents);
	}

	private static Animation.LoopType readLoopType(JsonObject animationObj, float length) throws JsonParseException {
		if (animationObj.has("loopTick")) {
			float returnTick = JsonUtil.getAsFloat(animationObj, "loopTick") * 20f;
			if (returnTick > length || returnTick < 0) {
				throw new JsonParseException("The returnTick has to be a non-negative value smaller than the endTick value");
			}
			return Animation.LoopType.returnToTickLoop(returnTick);
		}
		return Animation.LoopType.fromJson(animationObj.get("loop"));
	}

	private static Map<String, BoneAnimation> bakeBoneAnimations(JsonObject bonesObj) {
		Map<String, BoneAnimation> animations = new HashMap<>(bonesObj.size());

		for (Map.Entry<String, JsonElement> entry : bonesObj.entrySet()) {
			JsonObject entryObj = entry.getValue().getAsJsonObject();
			KeyframeStack scaleFrames = buildKeyframeStack(getKeyframes(entryObj.get("scale")), TransformType.SCALE);
			KeyframeStack positionFrames = buildKeyframeStack(getKeyframes(entryObj.get("position")), TransformType.POSITION);
			KeyframeStack rotationFrames = buildKeyframeStack(getKeyframes(entryObj.get("rotation")), TransformType.ROTATION);
			KeyframeStack bendFrames = buildKeyframeStack(getKeyframes(entryObj.get("bend")), TransformType.BEND);
			animations.put(
					UniversalAnimLoader.getCorrectPlayerBoneName(entry.getKey()),
					new BoneAnimation(rotationFrames, positionFrames, scaleFrames, bendFrames.xKeyframes())
			);
		}

		return animations;
	}

	private static List<FloatObjectPair<JsonElement>> getKeyframes(JsonElement element) {
		if (element == null)
			return List.of();

		if (element instanceof JsonPrimitive primitive) {
			JsonArray array = new JsonArray(3);

			array.add(primitive);
			array.add(primitive);
			array.add(primitive);

			element = array;
		}

		if (element instanceof JsonArray array)
			return ObjectArrayList.of(FloatObjectPair.of(0, array));

		if (element instanceof JsonObject obj) {
			if (obj.has("vector"))
				return ObjectArrayList.of(FloatObjectPair.of(0, obj));

			List<FloatObjectPair<JsonElement>> list = new ObjectArrayList<>();

			for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
				float timestamp = readTimestamp(entry.getKey());

				if (timestamp == 0 && !list.isEmpty())
					throw new JsonParseException("Invalid keyframe data - multiple starting keyframes?" + entry.getKey());

				if (entry.getValue() instanceof JsonObject entryObj && !entryObj.has("vector")) {
					addBedrockKeyframes(timestamp, entryObj, list);

					continue;
				}

				list.add(FloatObjectPair.of(timestamp, entry.getValue()));
			}

			return list;
		}

		throw new JsonParseException("Invalid object type provided to getTripletObj, got: " + element);
	}

	// Blockbench is just getting silly now
	private static JsonArray extractBedrockKeyframe(JsonElement keyframe) {
		if (keyframe.isJsonArray())
			return keyframe.getAsJsonArray();

		//For bends
		if (keyframe.isJsonPrimitive()) {
			JsonArray array = new JsonArray(3);
			array.add(keyframe.getAsFloat());
			array.add(0);
			array.add(0);
			return array;
		}

		if (!keyframe.isJsonObject())
			throw new JsonParseException("Invalid keyframe data - expected array or object, found " + keyframe);

		JsonObject keyframeObj = keyframe.getAsJsonObject();

		if (keyframeObj.has("vector"))
			return keyframeObj.get("vector").getAsJsonArray();

		if (keyframeObj.has("pre"))
			return keyframeObj.get("pre").getAsJsonArray();

		return keyframeObj.get("post").getAsJsonArray();
	}

	private static void addBedrockKeyframes(float timestamp, JsonObject keyframe, List<FloatObjectPair<JsonElement>> keyframes) {
		boolean addedFrame = false;

		if (keyframe.has("pre")) {
			addedFrame = true;

			JsonArray value = extractBedrockKeyframe(keyframe.get("pre"));
			JsonObject result = null;
			if (keyframe.has("easing")) {
				result = new JsonObject();
				result.add("vector", value);
				result.add("easing", keyframe.get("easing"));
				if (keyframe.has("easingArgs")) result.add("easingArgs", keyframe.get("easingArgs"));
			}

			keyframes.add(FloatObjectPair.of(timestamp == 0 ? timestamp : timestamp - 0.001f, result != null ? result : value));
		}

		if (keyframe.has("post")) {
			JsonArray values = extractBedrockKeyframe(keyframe.get("post"));

			if (keyframe.has("lerp_mode")) {
				JsonObject keyframeObj = new JsonObject();

				keyframeObj.add("vector", values);
				keyframeObj.add("easing", keyframe.get("lerp_mode"));

				keyframes.add(FloatObjectPair.of(timestamp, keyframeObj));
			}
			else {
				keyframes.add(FloatObjectPair.of(timestamp, values));
			}

			return;
		}

		if (!addedFrame)
			throw new JsonParseException("Invalid keyframe data - expected array, found " + keyframe);
	}

	private static KeyframeStack buildKeyframeStack(List<FloatObjectPair<JsonElement>> entries, TransformType type) {
		if (entries.isEmpty()) return new KeyframeStack();

		List<Keyframe> xFrames = new ObjectArrayList<>();
		List<Keyframe> yFrames = new ObjectArrayList<>();
		List<Keyframe> zFrames = new ObjectArrayList<>();

		List<Expression> xPrev = null;
		List<Expression> yPrev = null;
		List<Expression> zPrev = null;
		FloatObjectPair<JsonElement> prevEntry = null;

		for (FloatObjectPair<JsonElement> entry : entries) {
			JsonElement element = entry.right();

			float prevTime = prevEntry != null ? prevEntry.leftFloat() : 0;
			float curTime = entry.leftFloat();
			float timeDelta = curTime - prevTime;

			boolean isForRotation = type == TransformType.ROTATION || type == TransformType.BEND;
			Expression defaultValue = type == TransformType.SCALE ? FloatExpression.ONE : FloatExpression.ZERO;

			JsonArray keyFrameVector = element instanceof JsonArray array ? array : JsonUtil.getAsJsonArray(element.getAsJsonObject(), "vector");
			List<Expression> xValue = MolangLoader.parseJson(isForRotation, keyFrameVector.get(0), defaultValue);
			List<Expression> yValue = MolangLoader.parseJson(isForRotation, keyFrameVector.get(1), defaultValue);
			List<Expression> zValue = MolangLoader.parseJson(isForRotation, keyFrameVector.get(2), defaultValue);

			JsonObject entryObj = element instanceof JsonObject obj ? obj : null;
			EasingType easingType = entryObj != null && entryObj.has("easing") ? EasingType.fromJson(entryObj.get("easing")) : EasingType.LINEAR;
			List<List<Expression>> easingArgs = entryObj != null && entryObj.has("easingArgs") ?
					JsonUtil.jsonArrayToList(JsonUtil.getAsJsonArray(entryObj, "easingArgs"), ele -> Collections.singletonList(FloatExpression.of(ele.getAsFloat()))) :
					new ObjectArrayList<>();

			boolean isBezier = EasingType.BEZIER == easingType;

			List<List<Expression>> leftValues = null;
			List<List<Expression>> rightValues = null;
			List<List<Expression>> leftTimes = null;
			List<List<Expression>> rightTimes = null;

			if (isBezier) {
				leftValues = entryObj.has("left") ? JsonUtil.jsonArrayToList(JsonUtil.getAsJsonArray(entryObj, "left"), ele -> Collections.singletonList(FloatExpression.of(ele.getAsDouble()))) : ZERO_ARRAY;
				rightValues = entryObj.has("right") ? JsonUtil.jsonArrayToList(JsonUtil.getAsJsonArray(entryObj, "right"), ele -> Collections.singletonList(FloatExpression.of(ele.getAsDouble()))) : ZERO_ARRAY;
				leftTimes = entryObj.has("left_time") ? JsonUtil.jsonArrayToList(JsonUtil.getAsJsonArray(entryObj, "left_time"), ele -> Collections.singletonList(FloatExpression.of(ele.getAsDouble()))) : MINUS_ZERO_POINT_ONE_ARRAY;
				rightTimes = entryObj.has("right_time") ? JsonUtil.jsonArrayToList(JsonUtil.getAsJsonArray(entryObj, "right_time"), ele -> Collections.singletonList(FloatExpression.of(ele.getAsDouble()))) : ZERO_POINT_ONE_ARRAY;
			}

			xFrames.add(new Keyframe(timeDelta * 20, prevEntry == null ? xValue : xPrev, xValue, easingType, isBezier ? ObjectArrayList.of(leftValues.get(0), leftTimes.get(0), rightValues.get(0), rightTimes.get(0)) : easingArgs));
			yFrames.add(new Keyframe(timeDelta * 20, prevEntry == null ? yValue : yPrev, yValue, easingType, isBezier ? ObjectArrayList.of(leftValues.get(1), leftTimes.get(1), rightValues.get(1), rightTimes.get(1)) : easingArgs));
			zFrames.add(new Keyframe(timeDelta * 20, prevEntry == null ? zValue : zPrev, zValue, easingType, isBezier ? ObjectArrayList.of(leftValues.get(2), leftTimes.get(2), rightValues.get(2), rightTimes.get(2)) : easingArgs));
			
			xPrev = xValue;
			yPrev = yValue;
			zPrev = zValue;
			prevEntry = entry;
		}

		return new KeyframeStack(finalKeyframeModifications(xFrames), finalKeyframeModifications(yFrames), finalKeyframeModifications(zFrames));
	}

	private static List<Keyframe> finalKeyframeModifications(List<Keyframe> frames) {
		boolean isAllMoLangThis = true;
		for (Keyframe keyframe : frames) {
			if (isNotAllMoLangThis(keyframe.startValue()) || isNotAllMoLangThis(keyframe.endValue())) {
				isAllMoLangThis = false;
				break;
			}
		}
		if (isAllMoLangThis) return Collections.emptyList();

		boolean isAllTheSame = true;
		for (Keyframe keyframe : frames) {
			if (!keyframe.startValue().equals(keyframe.endValue())) {
				isAllTheSame = false;
				break;
			}
		}
		if (isAllTheSame) return Collections.singletonList(new Keyframe(0, frames.getFirst().startValue(), frames.getFirst().startValue()));

		if (frames.size() == 1) {
			Keyframe frame = frames.getFirst();

			if (frame.easingType() != EasingType.LINEAR) {
				frames.set(0, new Keyframe(frame.length(), frame.startValue(), frame.endValue()));

				return frames;
			}
		}

		for (int i = 0; i < frames.size(); i++) {
			Keyframe frame = frames.get(i);

			if (frame.easingType() == EasingType.CATMULLROM) {
				frames.set(i, new Keyframe(frame.length(), frame.startValue(), frame.endValue(), frame.easingType(), ObjectArrayList.of(
						i == 0 ? frame.startValue() : frames.get(i - 1).endValue(),
						i + 1 >= frames.size() ? frame.endValue() : frames.get(i + 1).endValue()
				)));
			}
			else if (frame.easingType() == EasingType.BEZIER) {
				List<Expression> rightValue = frame.easingArgs().get(2);
				List<Expression> rightTime = frame.easingArgs().get(3);
				frame.easingArgs().remove(2);
				frame.easingArgs().remove(2);
				if (frames.size() > i + 1) {
					Keyframe nextKeyframe = frames.get(i + 1);
					if (nextKeyframe.easingType() == EasingType.BEZIER) {
						nextKeyframe.easingArgs().add(rightValue);
						nextKeyframe.easingArgs().add(rightTime);
					}
					else frames.set(i + 1, new Keyframe(nextKeyframe.length(), nextKeyframe.startValue(), nextKeyframe.endValue(), EasingType.BEZIER_AFTER, ObjectArrayList.of(rightValue, rightTime)));
				}
			}
		}

		return frames;
	}

	public static boolean isNotAllMoLangThis(List<Expression> expressions) {
		if (expressions.size() > 1) return true;
		return !(expressions.isEmpty() || expressions.getFirst() == MolangLoader.THIS);
	}

	public static float calculateAnimationLength(Map<String, BoneAnimation> boneAnimations) {
		float length = 0;

		for (BoneAnimation animation : boneAnimations.values()) {
			length = Math.max(length, animation.rotationKeyFrames().getLastKeyframeTime());
			length = Math.max(length, animation.positionKeyFrames().getLastKeyframeTime());
			length = Math.max(length, animation.scaleKeyFrames().getLastKeyframeTime());
		}

		return length == 0 ? Float.MAX_VALUE : length;
	}

	private static float readTimestamp(String timestamp) {
		try {
			return Float.parseFloat(timestamp);
		} catch (Throwable th) {
			return 0;
		}
	}

	static {
		List<Expression> ZERO = Collections.singletonList(FloatExpression.of(0));
		List<Expression> ZERO_POINT_ONE = Collections.singletonList(FloatExpression.of(0.1));
		List<Expression> MINUS_ZERO_POINT_ONE = Collections.singletonList(FloatExpression.of(-0.1));

		ZERO_ARRAY = ObjectArrayList.of(ZERO, ZERO, ZERO);
		ZERO_POINT_ONE_ARRAY = ObjectArrayList.of(ZERO_POINT_ONE, ZERO_POINT_ONE, ZERO_POINT_ONE);
		MINUS_ZERO_POINT_ONE_ARRAY = ObjectArrayList.of(MINUS_ZERO_POINT_ONE, MINUS_ZERO_POINT_ONE, MINUS_ZERO_POINT_ONE);
	}
}
