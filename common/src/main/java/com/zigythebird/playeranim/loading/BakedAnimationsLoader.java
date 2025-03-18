package com.zigythebird.playeranim.loading;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.animation.Animation;
import com.zigythebird.playeranim.animation.EasingType;
import com.zigythebird.playeranim.animation.TransformType;
import com.zigythebird.playeranim.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranim.animation.keyframe.Keyframe;
import com.zigythebird.playeranim.animation.keyframe.KeyframeStack;
import com.zigythebird.playeranim.cache.PlayerAnimBone;
import com.zigythebird.playeranim.math.MolangParser;
import com.zigythebird.playeranim.misc.CompoundException;
import com.zigythebird.playeranim.util.JsonUtil;
import gg.moonflower.molangcompiler.api.MolangExpression;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BakedAnimationsLoader {
	public static Map<String, Animation> deserialize(JsonElement json, Map<String, PlayerAnimBone> bones, Map<String, String> parents) throws RuntimeException {
		JsonObject obj = json.getAsJsonObject();
		Map<String, Animation> animations = new Object2ObjectOpenHashMap<>(obj.size());

		for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
			try {
				animations.put(entry.getKey(), bakeAnimation(entry.getKey(), entry.getValue().getAsJsonObject(), bones, parents));
			} catch (Exception ex) {
				ModInit.LOGGER.error("Unable to parse animation: {}", entry.getKey(), ex);
			}
		}

		return animations;
	}

	public static KeyframeStack<Keyframe<MolangExpression>> buildKeyframeStackFromLegacyAnim(List<Pair<Integer, Vec3>> entries) throws CompoundException {
		if (entries.isEmpty())
			return new KeyframeStack<>();

		List<Keyframe<MolangExpression>> xFrames = new ObjectArrayList<>();
		List<Keyframe<MolangExpression>> yFrames = new ObjectArrayList<>();
		List<Keyframe<MolangExpression>> zFrames = new ObjectArrayList<>();

		MolangExpression xPrev = null;
		MolangExpression yPrev = null;
		MolangExpression zPrev = null;
		Pair<Integer, Vec3> prevEntry = null;

		for (Pair<Integer, Vec3> entry : entries) {
			Integer key = entry.getFirst();
			Vec3 keyFrameVector = entry.getSecond();

			float prevTime = prevEntry != null ? prevEntry.getFirst() : 0;
			float curTime = key;
			float timeDelta = curTime - prevTime;

			MolangExpression xValue = MolangExpression.of((float) keyFrameVector.x);
			MolangExpression yValue = MolangExpression.of((float) keyFrameVector.y);
			MolangExpression zValue = MolangExpression.of((float) keyFrameVector.z);

			xFrames.add(new Keyframe<>(timeDelta * 20, prevEntry == null ? xValue : xPrev, xValue, EasingType.LINEAR));
			yFrames.add(new Keyframe<>(timeDelta * 20, prevEntry == null ? yValue : yPrev, yValue, EasingType.LINEAR));
			zFrames.add(new Keyframe<>(timeDelta * 20, prevEntry == null ? zValue : zPrev, zValue, EasingType.LINEAR));

			xPrev = xValue;
			yPrev = yValue;
			zPrev = zValue;
			prevEntry = entry;
		}

		return new KeyframeStack<>(xFrames, yFrames, zFrames);
	}

	private static Animation bakeAnimation(String name, JsonObject animationObj, Map<String, PlayerAnimBone> bones, Map<String, String> parents) throws CompoundException {
		double length = animationObj.has("animation_length") ? GsonHelper.getAsDouble(animationObj, "animation_length") * 20d : -1;
		Animation.LoopType loopType = Animation.LoopType.fromJson(animationObj.get("loop"));
		BoneAnimation[] boneAnimations = bakeBoneAnimations(GsonHelper.getAsJsonObject(animationObj, "bones", new JsonObject()));
		Animation.Keyframes keyframes = KeyFramesLoader.deserialize(animationObj);

		if (length == -1)
			length = calculateAnimationLength(boneAnimations);

		return new Animation(name, length, loopType, boneAnimations, keyframes, bones, parents);
	}

	private static BoneAnimation[] bakeBoneAnimations(JsonObject bonesObj) throws CompoundException {
		BoneAnimation[] animations = new BoneAnimation[bonesObj.size()];
		int index = 0;

		for (Map.Entry<String, JsonElement> entry : bonesObj.entrySet()) {
			JsonObject entryObj = entry.getValue().getAsJsonObject();
			KeyframeStack<Keyframe<MolangExpression>> scaleFrames = buildKeyframeStack(getTripletObj(entryObj.get("scale")), TransformType.SCALE);
			KeyframeStack<Keyframe<MolangExpression>> positionFrames = buildKeyframeStack(getTripletObj(entryObj.get("position")), TransformType.POSITION);
			KeyframeStack<Keyframe<MolangExpression>> rotationFrames = buildKeyframeStack(getTripletObj(entryObj.get("rotation")), TransformType.ROTATION);
			KeyframeStack<Keyframe<MolangExpression>> bendFrames = buildKeyframeStack(getTripletObj(entryObj.get("bend")), TransformType.BEND);

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

	private static KeyframeStack<Keyframe<MolangExpression>> buildKeyframeStack(List<Pair<String, JsonElement>> entries, TransformType type) throws CompoundException {
		if (entries.isEmpty())
			return new KeyframeStack<>();

		List<Keyframe<MolangExpression>> xFrames = new ObjectArrayList<>();
		List<Keyframe<MolangExpression>> yFrames = new ObjectArrayList<>();
		List<Keyframe<MolangExpression>> zFrames = new ObjectArrayList<>();

		MolangExpression xPrev = null;
		MolangExpression yPrev = null;
		MolangExpression zPrev = null;
		Pair<String, JsonElement> prevEntry = null;

		for (Pair<String, JsonElement> entry : entries) {
			String key = entry.getFirst();
			JsonElement element = entry.getSecond();

			if (key.equals("easing") || key.equals("easingArgs") || key.equals("lerp_mode"))
				continue;

			float prevTime = prevEntry != null ? Float.parseFloat(prevEntry.getFirst()) : 0;
			float curTime = NumberUtils.isCreatable(key) ? Float.parseFloat(entry.getFirst()) : 0;
			float timeDelta = curTime - prevTime;

			boolean isForRotation = type == TransformType.ROTATION;
			MolangExpression defaultValue = MolangExpression.of(type == TransformType.SCALE ? 1 : 0);

			JsonArray keyFrameVector = element instanceof JsonArray array ? array : GsonHelper.getAsJsonArray(element.getAsJsonObject(), "vector");
			MolangExpression xValue = MolangParser.parseJson(isForRotation, -1, keyFrameVector.get(0), defaultValue);
			MolangExpression yValue = MolangParser.parseJson(isForRotation, -1, keyFrameVector.get(1), defaultValue);
			MolangExpression zValue = MolangParser.parseJson(isForRotation, keyFrameVector.get(2), defaultValue);

			JsonObject entryObj = element instanceof JsonObject obj ? obj : null;
			EasingType easingType = entryObj != null && entryObj.has("easing") ? EasingType.fromJson(entryObj.get("easing")) : EasingType.LINEAR;
			List<MolangExpression> easingArgs = entryObj != null && entryObj.has("easingArgs") ?
					JsonUtil.jsonArrayToList(GsonHelper.getAsJsonArray(entryObj, "easingArgs"), ele -> MolangExpression.of((float) ele.getAsDouble())) :
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
