package com.zigythebird.playeranim.loading;

import com.google.gson.*;
import com.zigythebird.playeranim.PlayerAnimLibMod;
import com.zigythebird.playeranim.animation.Animation;
import com.zigythebird.playeranim.animation.EasingType;
import com.zigythebird.playeranim.animation.ExtraAnimationData;
import com.zigythebird.playeranim.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranim.animation.keyframe.Keyframe;
import com.zigythebird.playeranim.animation.keyframe.KeyframeStack;
import com.zigythebird.playeranim.bones.PivotBone;
import com.zigythebird.playeranim.enums.TransformType;
import com.zigythebird.playeranim.misc.CompoundException;
import com.zigythebird.playeranim.molang.MolangLoader;
import com.zigythebird.playeranim.util.JsonUtil;
import it.unimi.dsi.fastutil.floats.FloatObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.math.NumberUtils;
import team.unnamed.mocha.parser.ast.DoubleExpression;
import team.unnamed.mocha.parser.ast.Expression;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AnimationLoader {
	public static Map<String, Animation> deserialize(JsonElement json, Map<String, PivotBone> bones, Map<String, String> parents) throws RuntimeException {
		JsonObject obj = json.getAsJsonObject();
		Map<String, Animation> animations = new Object2ObjectOpenHashMap<>(obj.size());

		ExtraAnimationData extraData = new ExtraAnimationData();

		if (obj.has(PlayerAnimLibMod.MOD_ID)) {
			extraData.fromJson(obj.getAsJsonObject(PlayerAnimLibMod.MOD_ID));
		}
		if (extraData.has(ExtraAnimationData.NAME_KEY)) extraData.data().remove(ExtraAnimationData.NAME_KEY);

		for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
			try {
				animations.put(entry.getKey(), bakeAnimation(entry.getKey(), entry.getValue().getAsJsonObject(), bones, parents, extraData.copy()));
			} catch (Exception ex) {
				PlayerAnimLibMod.LOGGER.error("Unable to parse animation: {}", entry.getKey(), ex);
			}
		}

		return animations;
	}

	private static Animation bakeAnimation(String name, JsonObject animationObj, Map<String, PivotBone> bones, Map<String, String> parents, ExtraAnimationData extraData) throws CompoundException {
		float length = animationObj.has("animation_length") ? GsonHelper.getAsFloat(animationObj, "animation_length") * 20f : -1;
		Animation.LoopType loopType = Animation.LoopType.fromJson(animationObj.get("loop"));
		BoneAnimation[] boneAnimations = bakeBoneAnimations(GsonHelper.getAsJsonObject(animationObj, "bones", new JsonObject()));
		Animation.Keyframes keyframes = KeyFrameLoader.deserialize(animationObj);

		if (length == -1)
			length = calculateAnimationLength(boneAnimations);

		if (animationObj.has(PlayerAnimLibMod.MOD_ID)) {
			extraData.fromJson(animationObj.getAsJsonObject(PlayerAnimLibMod.MOD_ID));
		}

		if (!extraData.data().containsKey(ExtraAnimationData.NAME_KEY)) { // Fallback to name
			extraData.data().put(ExtraAnimationData.NAME_KEY, name);
		}

		return new Animation(extraData, length, loopType, boneAnimations, keyframes, bones, parents);
	}

	private static BoneAnimation[] bakeBoneAnimations(JsonObject bonesObj) throws CompoundException {
		BoneAnimation[] animations = new BoneAnimation[bonesObj.size()];
		int index = 0;

		for (Map.Entry<String, JsonElement> entry : bonesObj.entrySet()) {
			JsonObject entryObj = entry.getValue().getAsJsonObject();
			KeyframeStack<Keyframe> scaleFrames = buildKeyframeStack(getKeyframes(entryObj.get("scale")), TransformType.SCALE);
			KeyframeStack<Keyframe> positionFrames = buildKeyframeStack(getKeyframes(entryObj.get("position")), TransformType.POSITION);
			KeyframeStack<Keyframe> rotationFrames = buildKeyframeStack(getKeyframes(entryObj.get("rotation")), TransformType.ROTATION);
			KeyframeStack<Keyframe> bendFrames = buildKeyframeStack(getKeyframes(entryObj.get("bend")), TransformType.BEND);

			animations[index] = new BoneAnimation(entry.getKey(), rotationFrames, positionFrames, scaleFrames, bendFrames);
			index++;
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

			keyframes.add(FloatObjectPair.of(timestamp == 0 ? timestamp : timestamp - 0.001f, pre.isJsonArray() ? pre.getAsJsonArray() : GsonHelper.getAsJsonArray(pre.getAsJsonObject(), "vector")));
		}

		if (keyframe.has("post")) {
			JsonElement post = keyframe.get("post");
			JsonArray values = post.isJsonArray() ? post.getAsJsonArray() : GsonHelper.getAsJsonArray(post.getAsJsonObject(), "vector");

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

	private static KeyframeStack<Keyframe> buildKeyframeStack(List<FloatObjectPair<JsonElement>> entries, TransformType type) throws CompoundException {
		if (entries.isEmpty())
			return new KeyframeStack<>();

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
			Expression defaultValue = new DoubleExpression(type == TransformType.SCALE ? 1 : 0);

			JsonArray keyFrameVector = element instanceof JsonArray array ? array : GsonHelper.getAsJsonArray(element.getAsJsonObject(), "vector");
			List<Expression> xValue = MolangLoader.parseJson(isForRotation, keyFrameVector.get(0), defaultValue);
			List<Expression> yValue = MolangLoader.parseJson(isForRotation, keyFrameVector.get(1), defaultValue);
			List<Expression> zValue = MolangLoader.parseJson(isForRotation, keyFrameVector.get(2), defaultValue);

			JsonObject entryObj = element instanceof JsonObject obj ? obj : null;
			EasingType easingType = entryObj != null && entryObj.has("easing") ? EasingType.fromJson(entryObj.get("easing")) : EasingType.LINEAR;
			List<List<Expression>> easingArgs = entryObj != null && entryObj.has("easingArgs") ?
					JsonUtil.jsonArrayToList(GsonHelper.getAsJsonArray(entryObj, "easingArgs"), ele -> Collections.singletonList(new DoubleExpression(ele.getAsFloat()))) :
					new ObjectArrayList<>();

			xFrames.add(new Keyframe(timeDelta * 20, prevEntry == null ? xValue : xPrev, xValue, easingType, easingArgs));
			yFrames.add(new Keyframe(timeDelta * 20, prevEntry == null ? yValue : yPrev, yValue, easingType, easingArgs));
			zFrames.add(new Keyframe(timeDelta * 20, prevEntry == null ? zValue : zPrev, zValue, easingType, easingArgs));

			xPrev = xValue;
			yPrev = yValue;
			zPrev = zValue;
			prevEntry = entry;
		}

		return new KeyframeStack<>(addSplineArgs(xFrames), addSplineArgs(yFrames), addSplineArgs(zFrames));
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

	public static float calculateAnimationLength(BoneAnimation[] boneAnimations) {
		float length = 0;

		for (BoneAnimation animation : boneAnimations) {
			length = Math.max(length, animation.rotationKeyFrames().getLastKeyframeTime());
			length = Math.max(length, animation.positionKeyFrames().getLastKeyframeTime());
			length = Math.max(length, animation.scaleKeyFrames().getLastKeyframeTime());
		}

		return length == 0 ? Float.MAX_VALUE : length;
	}

	private static float readTimestamp(String timestamp) {
		return NumberUtils.isCreatable(timestamp) ? Float.parseFloat(timestamp) : 0;
	}
}
