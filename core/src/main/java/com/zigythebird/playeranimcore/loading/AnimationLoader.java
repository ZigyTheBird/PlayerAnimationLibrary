package com.zigythebird.playeranimcore.loading;

import com.google.gson.*;
import com.zigythebird.playeranimcore.PlayerAnimLib;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.EasingType;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranimcore.animation.keyframe.Keyframe;
import com.zigythebird.playeranimcore.animation.keyframe.KeyframeStack;
import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.math.Vec3f;
import com.zigythebird.playeranimcore.misc.CompoundException;
import com.zigythebird.playeranimcore.molang.MolangLoader;
import com.zigythebird.playeranimcore.util.JsonUtil;
import it.unimi.dsi.fastutil.floats.FloatObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.parser.ast.FloatExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AnimationLoader {
	public static Map<String, Animation> deserialize(JsonElement json, Map<String, Vec3f> bones, Map<String, String> parents) throws RuntimeException {
		JsonObject obj = json.getAsJsonObject();
		Map<String, Animation> animations = new Object2ObjectOpenHashMap<>(obj.size());

		ExtraAnimationData extraData = new ExtraAnimationData();

		if (obj.has(PlayerAnimLib.MOD_ID)) {
			extraData.fromJson(obj.getAsJsonObject(PlayerAnimLib.MOD_ID));
		}
		if (extraData.has(ExtraAnimationData.NAME_KEY)) extraData.data().remove(ExtraAnimationData.NAME_KEY);

		for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
			try {
				animations.put(entry.getKey(), bakeAnimation(entry.getKey(), entry.getValue().getAsJsonObject(), bones, parents, extraData.copy()));
			} catch (Exception ex) {
				PlayerAnimLib.LOGGER.error("Unable to parse animation: {}", entry.getKey(), ex);
			}
		}

		return animations;
	}

	private static Animation bakeAnimation(String name, JsonObject animationObj, Map<String, Vec3f> bones, Map<String, String> parents, ExtraAnimationData extraData) throws CompoundException {
		float length = animationObj.has("animation_length") ? JsonUtil.getAsFloat(animationObj, "animation_length") * 20f : -1;

		List<BoneAnimation> boneAnimations = bakeBoneAnimations(JsonUtil.getAsJsonObject(animationObj, "bones", new JsonObject()));
		if (length == -1) length = calculateAnimationLength(boneAnimations);

		Animation.LoopType loopType = readLoopType(animationObj, length);
		Animation.Keyframes keyframes = KeyFrameLoader.deserialize(animationObj);

		if (animationObj.has(PlayerAnimLib.MOD_ID)) {
			extraData.fromJson(animationObj.getAsJsonObject(PlayerAnimLib.MOD_ID));
		}

		if (!extraData.data().containsKey(ExtraAnimationData.NAME_KEY)) { // Fallback to name
			extraData.data().put(ExtraAnimationData.NAME_KEY, name);
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

	private static List<BoneAnimation> bakeBoneAnimations(JsonObject bonesObj) throws CompoundException {
		List<BoneAnimation> animations = new ArrayList<>(bonesObj.size());

		for (Map.Entry<String, JsonElement> entry : bonesObj.entrySet()) {
			JsonObject entryObj = entry.getValue().getAsJsonObject();
			KeyframeStack scaleFrames = buildKeyframeStack(getKeyframes(entryObj.get("scale")), TransformType.SCALE);
			KeyframeStack positionFrames = buildKeyframeStack(getKeyframes(entryObj.get("position")), TransformType.POSITION);
			KeyframeStack rotationFrames = buildKeyframeStack(getKeyframes(entryObj.get("rotation")), TransformType.ROTATION);
			KeyframeStack bendFrames = buildKeyframeStack(getKeyframes(entryObj.get("bend")), TransformType.BEND);
			animations.add(new BoneAnimation(entry.getKey(), rotationFrames, positionFrames, scaleFrames, bendFrames));
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

	private static void addBedrockKeyframes(float timestamp, JsonObject keyframe, List<FloatObjectPair<JsonElement>> keyframes) {
		boolean addedFrame = false;

		if (keyframe.has("pre")) {
			JsonElement pre = keyframe.get("pre");
			addedFrame = true;

			keyframes.add(FloatObjectPair.of(timestamp == 0 ? timestamp : timestamp - 0.001f, pre.isJsonArray() ? pre.getAsJsonArray() : JsonUtil.getAsJsonArray(pre.getAsJsonObject(), "vector")));
		}

		if (keyframe.has("post")) {
			JsonElement post = keyframe.get("post");
			JsonArray values = post.isJsonArray() ? post.getAsJsonArray() : JsonUtil.getAsJsonArray(post.getAsJsonObject(), "vector");

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

	private static KeyframeStack buildKeyframeStack(List<FloatObjectPair<JsonElement>> entries, TransformType type) throws CompoundException {
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

			xFrames.add(new Keyframe(timeDelta * 20, prevEntry == null ? xValue : xPrev, xValue, easingType, easingArgs));
			yFrames.add(new Keyframe(timeDelta * 20, prevEntry == null ? yValue : yPrev, yValue, easingType, easingArgs));
			zFrames.add(new Keyframe(timeDelta * 20, prevEntry == null ? zValue : zPrev, zValue, easingType, easingArgs));

			xPrev = xValue;
			yPrev = yValue;
			zPrev = zValue;
			prevEntry = entry;
		}

		return new KeyframeStack(addSplineArgs(xFrames), addSplineArgs(yFrames), addSplineArgs(zFrames));
	}

	private static List<Keyframe> addSplineArgs(List<Keyframe> frames) {
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
		}

		return frames;
	}

	public static float calculateAnimationLength(List<BoneAnimation> boneAnimations) {
		float length = 0;

		for (BoneAnimation animation : boneAnimations) {
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
}
