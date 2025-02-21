package com.zigythebird.playeranim.loading;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.animation.Animation;
import com.zigythebird.playeranim.animation.EasingType;
import com.zigythebird.playeranim.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranim.animation.keyframe.Keyframe;
import com.zigythebird.playeranim.animation.keyframe.KeyframeStack;
import com.zigythebird.playeranim.math.MathParser;
import com.zigythebird.playeranim.math.MathValue;
import com.zigythebird.playeranim.math.value.Constant;
import com.zigythebird.playeranim.misc.CompoundException;
import com.zigythebird.playeranim.util.JsonUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.Map;

public class BakedAnimationsAdapter {
	public static final ObjectArrayList NO_EASING_ARGS = new ObjectArrayList<>();

	public static Map<String, Animation> deserialize(JsonElement json) throws RuntimeException {
		JsonObject obj = json.getAsJsonObject();
		Map<String, Animation> animations = new Object2ObjectOpenHashMap<>(obj.size());

		for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
			try {
				animations.put(entry.getKey(), bakeAnimation(entry.getKey(), entry.getValue().getAsJsonObject()));
			}
			catch (Exception ex) {
				if (ex instanceof CompoundException compoundEx) {
					ModInit.LOGGER.error(compoundEx.withMessage("Unable to parse animation: " + entry.getKey()).getLocalizedMessage());
				}
				else {
					ModInit.LOGGER.error("Unable to parse animation: " + entry.getKey());
				}

				ex.printStackTrace();
			}
		}

		return animations;
	}

	public static KeyframeStack<Keyframe<MathValue>> buildKeyframeStackFromPlayerAnim(List<Pair<Integer, Vec3>> entries) throws CompoundException {
		if (entries.isEmpty())
			return new KeyframeStack<>();

		List<Keyframe<MathValue>> xFrames = new ObjectArrayList<>();
		List<Keyframe<MathValue>> yFrames = new ObjectArrayList<>();
		List<Keyframe<MathValue>> zFrames = new ObjectArrayList<>();

		MathValue xPrev = null;
		MathValue yPrev = null;
		MathValue zPrev = null;
		Pair<Integer, Vec3> prevEntry = null;

		for (Pair<Integer, Vec3> entry : entries) {
			Integer key = entry.getFirst();
			Vec3 keyFrameVector = entry.getSecond();

			double prevTime = prevEntry != null ? prevEntry.getFirst() : 0;
			double curTime = key;
			double timeDelta = curTime - prevTime;

			MathValue xValue = new Constant(keyFrameVector.x);
			MathValue yValue = new Constant(keyFrameVector.y);
			MathValue zValue = new Constant(keyFrameVector.z);

			xFrames.add(new Keyframe<>(timeDelta * 20, prevEntry == null ? xValue : xPrev, xValue, EasingType.LINEAR, NO_EASING_ARGS));
			yFrames.add(new Keyframe<>(timeDelta * 20, prevEntry == null ? yValue : yPrev, yValue, EasingType.LINEAR, NO_EASING_ARGS));
			zFrames.add(new Keyframe<>(timeDelta * 20, prevEntry == null ? zValue : zPrev, zValue, EasingType.LINEAR, NO_EASING_ARGS));

			xPrev = xValue;
			yPrev = yValue;
			zPrev = zValue;
			prevEntry = entry;
		}

		return new KeyframeStack<>(xFrames, yFrames, zFrames);
	}

	private static Animation bakeAnimation(String name, JsonObject animationObj) throws CompoundException {
		double length = animationObj.has("animation_length") ? GsonHelper.getAsDouble(animationObj, "animation_length") * 20d : -1;
		Animation.LoopType loopType = Animation.LoopType.fromJson(animationObj.get("loop"));
		BoneAnimation[] boneAnimations = bakeBoneAnimations(GsonHelper.getAsJsonObject(animationObj, "bones", new JsonObject()));
		Animation.Keyframes keyframes = KeyFramesAdapter.deserialize(animationObj);

		if (length == -1)
			length = calculateAnimationLength(boneAnimations);

		return new Animation(name, length, loopType, boneAnimations, keyframes);
	}

	private static BoneAnimation[] bakeBoneAnimations(JsonObject bonesObj) throws CompoundException {
		BoneAnimation[] animations = new BoneAnimation[bonesObj.size()];
		int index = 0;

		for (Map.Entry<String, JsonElement> entry : bonesObj.entrySet()) {
			if (entry.getKey().endsWith("_bend")) continue;
			JsonObject entryObj = entry.getValue().getAsJsonObject();
			KeyframeStack<Keyframe<MathValue>> scaleFrames = buildKeyframeStack(getTripletObj(entryObj.get("scale")), false);
			KeyframeStack<Keyframe<MathValue>> positionFrames = buildKeyframeStack(getTripletObj(entryObj.get("position")), false);
			KeyframeStack<Keyframe<MathValue>> rotationFrames = buildKeyframeStack(getTripletObj(entryObj.get("rotation")), true);
			KeyframeStack<Keyframe<MathValue>> bendFrames;

			if (bonesObj.has(entry.getKey() + "_bend")) {
				bendFrames = buildKeyframeStack(getTripletObj(bonesObj.get(entry.getKey() + "_bend").getAsJsonObject().get("rotation")), true);
			}
			else bendFrames = new KeyframeStack<>();

			animations[index] = new BoneAnimation(entry.getKey(), rotationFrames, positionFrames, scaleFrames, bendFrames);
			index++;
		}

		return animations;
	}

	private static List<Pair<String, JsonElement>> getTripletObj(JsonElement element) {
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
			return ObjectArrayList.of(Pair.of("0", array));

		if (element instanceof JsonObject obj) {
			List<Pair<String, JsonElement>> list = new ObjectArrayList<>();

			for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
				if (entry.getValue() instanceof JsonObject entryObj && !entryObj.has("vector")) {
					list.add(getTripletObjBedrock(entry.getKey(), entryObj));

					continue;
				}

				list.add(Pair.of(entry.getKey(), entry.getValue()));
			}

			return list;
		}

		throw new JsonParseException("Invalid object type provided to getTripletObj, got: " + element);
	}

	private static Pair<String, JsonElement> getTripletObjBedrock(String timestamp, JsonObject keyframe) {
		JsonArray keyframeValues = null;

		if (keyframe.has("pre")) {
			JsonElement pre = keyframe.get("pre");
			keyframeValues = pre.isJsonArray() ? pre.getAsJsonArray() : GsonHelper.getAsJsonArray(pre.getAsJsonObject(), "vector");
		}
		else if (keyframe.has("post")) {
			JsonElement post = keyframe.get("post");
			keyframeValues = post.isJsonArray() ? post.getAsJsonArray() : GsonHelper.getAsJsonArray(post.getAsJsonObject(), "vector");
		}

		if (keyframeValues != null)
			return Pair.of(NumberUtils.isCreatable(timestamp) ? timestamp : "0", keyframeValues);

		throw new JsonParseException("Invalid keyframe data - expected array, found " + keyframe);
	}

	private static KeyframeStack<Keyframe<MathValue>> buildKeyframeStack(List<Pair<String, JsonElement>> entries, boolean isForRotation) throws CompoundException {
		if (entries.isEmpty())
			return new KeyframeStack<>();

		List<Keyframe<MathValue>> xFrames = new ObjectArrayList<>();
		List<Keyframe<MathValue>> yFrames = new ObjectArrayList<>();
		List<Keyframe<MathValue>> zFrames = new ObjectArrayList<>();

		MathValue xPrev = null;
		MathValue yPrev = null;
		MathValue zPrev = null;
		Pair<String, JsonElement> prevEntry = null;

		for (Pair<String, JsonElement> entry : entries) {
			String key = entry.getFirst();
			JsonElement element = entry.getSecond();

			if (key.equals("easing") || key.equals("easingArgs") || key.equals("lerp_mode"))
				continue;

			double prevTime = prevEntry != null ? Double.parseDouble(prevEntry.getFirst()) : 0;
			double curTime = NumberUtils.isCreatable(key) ? Double.parseDouble(entry.getFirst()) : 0;
			double timeDelta = curTime - prevTime;

			JsonArray keyFrameVector = element instanceof JsonArray array ? array : GsonHelper.getAsJsonArray(element.getAsJsonObject(), "vector");
			MathValue rawXValue = MathParser.parseJson(keyFrameVector.get(0));
			MathValue rawYValue = MathParser.parseJson(keyFrameVector.get(1));
			MathValue rawZValue = MathParser.parseJson(keyFrameVector.get(2));
			MathValue xValue = isForRotation && rawXValue instanceof Constant ? new Constant(Math.toRadians(-rawXValue.get())) : rawXValue;
			MathValue yValue = isForRotation && rawYValue instanceof Constant ? new Constant(Math.toRadians(-rawYValue.get())) : rawYValue;
			MathValue zValue = isForRotation && rawZValue instanceof Constant ? new Constant(Math.toRadians(rawZValue.get())) : rawZValue;

			JsonObject entryObj = element instanceof JsonObject obj ? obj : null;
			EasingType easingType = entryObj != null && entryObj.has("easing") ? EasingType.fromJson(entryObj.get("easing")) : EasingType.LINEAR;
			List<MathValue> easingArgs = entryObj != null && entryObj.has("easingArgs") ?
					JsonUtil.jsonArrayToList(GsonHelper.getAsJsonArray(entryObj, "easingArgs"), ele -> new Constant(ele.getAsDouble())) :
					new ObjectArrayList<>();

			xFrames.add(new Keyframe<>(timeDelta * 20, prevEntry == null ? xValue : xPrev, xValue, easingType, easingArgs));
			yFrames.add(new Keyframe<>(timeDelta * 20, prevEntry == null ? yValue : yPrev, yValue, easingType, easingArgs));
			zFrames.add(new Keyframe<>(timeDelta * 20, prevEntry == null ? zValue : zPrev, zValue, easingType, easingArgs));

			xPrev = xValue;
			yPrev = yValue;
			zPrev = zValue;
			prevEntry = entry;
		}

		return new KeyframeStack<>(xFrames, yFrames, zFrames);
	}

	public static double calculateAnimationLength(BoneAnimation[] boneAnimations) {
		double length = 0;

		for (BoneAnimation animation : boneAnimations) {
			length = Math.max(length, animation.rotationKeyFrames().getLastKeyframeTime());
			length = Math.max(length, animation.positionKeyFrames().getLastKeyframeTime());
			length = Math.max(length, animation.scaleKeyFrames().getLastKeyframeTime());
		}

		return length == 0 ? Double.MAX_VALUE : length;
	}
}
