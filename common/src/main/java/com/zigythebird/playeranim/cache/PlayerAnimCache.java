package com.zigythebird.playeranim.cache;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.animation.Animation;
import com.zigythebird.playeranim.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranim.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranim.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranim.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranim.loading.BakedAnimationsLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Cache class for holding loaded {@link Animation Animations}
 */
public final class PlayerAnimCache {
	public static final Animation.Keyframes NO_KEYFRAMES = new Animation.Keyframes(new SoundKeyframeData[]{}, new ParticleKeyframeData[]{}, new CustomInstructionKeyframeData[]{});
	private static final Map<ResourceLocation, Animation> ANIMATIONS = new Object2ObjectOpenHashMap<>();

	public static boolean hasAnimation(ResourceLocation id) {
		return ANIMATIONS.containsKey(id);
	}

	public static @Nullable Animation getAnimation(ResourceLocation id) {
		if (!ANIMATIONS.containsKey(id)) return null;
		return ANIMATIONS.get(id);
	}

	/**
	 * Load animations using ResourceManager
	 * Internal use only!
	 */
	@ApiStatus.Internal
	public static void resourceLoaderCallback(@NotNull ResourceManager manager) {
		ANIMATIONS.clear();

		for (var resource: manager.listResources("player_animations", resourceLocation -> resourceLocation.getPath().endsWith(".json")).entrySet()) {
			try {
				ResourceLocation key = resource.getKey();
				String[] splitPath = key.getPath().split("/");
				loadPlayerAnim(ResourceLocation.fromNamespaceAndPath(key.getNamespace(), splitPath[splitPath.length-1]), resource.getValue().open());
			}
			catch (Exception e) {
				ModInit.LOGGER.error("Player Animation Library failed to load animation {} because:", resource.getKey(), e);
			}
		}
	}

	public static void loadPlayerAnim(ResourceLocation id, InputStream resource) {
		try {
			JsonObject json = ModInit.GSON.fromJson(new InputStreamReader(resource), JsonObject.class);
			if (json.has("animations")) {
				JsonObject model = GsonHelper.getAsJsonObject(json, "model", new JsonObject());
				Map<String, PlayerAnimBone> bones = new HashMap<>();
				for (Map.Entry<String, JsonElement> entry : model.entrySet()) {
					JsonObject object = entry.getValue().getAsJsonObject();
					JsonArray pivot = object.get("pivot").getAsJsonArray();
					PlayerAnimBone bone = new PlayerAnimBone(object.has("parent") ? bones.get(object.get("parent").getAsString()) : null,
							entry.getKey(), new Vec3(pivot.get(0).getAsDouble(), pivot.get(1).getAsDouble(), pivot.get(2).getAsDouble()));
					bone.saveInitialSnapshot();
					bones.put(entry.getKey(), bone);
				}

				JsonObject parentsObj = GsonHelper.getAsJsonObject(json, "parents", new JsonObject());
				Map<String, String> parents = new HashMap<>();
				for (Map.Entry<String, JsonElement> entry : parentsObj.entrySet()) {
					parents.put(getCorrectPlayerBoneName(entry.getKey()), entry.getValue().getAsString());
				}

				json = json.get("animations").getAsJsonObject();
				JsonObject modifiedJson = new JsonObject();
				for (Map.Entry<String, JsonElement> entry : json.asMap().entrySet()) {
					JsonObject modifiedBones = new JsonObject();
					for (Map.Entry<String, JsonElement> entry1 : entry.getValue().getAsJsonObject().get("bones").getAsJsonObject().asMap().entrySet()) {
						modifiedBones.add(getCorrectPlayerBoneName(entry1.getKey()), entry1.getValue());
					}
					JsonObject entryJson = entry.getValue().getAsJsonObject();
					entryJson.add("bones", modifiedBones);
					modifiedJson.add(id.getNamespace() + ":" + entry.getKey(), entryJson);
				}
				Map<String, Animation> anim = BakedAnimationsLoader.deserialize(modifiedJson, bones, parents);
				for (Map.Entry<String, Animation> entry : anim.entrySet()) {
					ANIMATIONS.put(ResourceLocation.parse(entry.getKey()), entry.getValue());
				}
			}
			else {
				Animation animation = loadLegacyPlayerAnim(json);
				ANIMATIONS.put(id, animation);
			}
		}
		catch (Exception e) {
			ModInit.LOGGER.error("Player Animation Library failed to load animation {}:", id, e);
		}
	}

	public static Animation loadLegacyPlayerAnim(JsonElement json) {
		JsonObject obj = json.getAsJsonObject();
		List<BoneAnimation> boneAnims = new ArrayList<>();
		for (JsonElement jsonElement : obj.get("moves").getAsJsonArray()) {
			if (json.isJsonObject()) {
				JsonObject move = (JsonObject) jsonElement;
				int currentTick = move.get("tick").getAsInt();
				for (Map.Entry<String, JsonElement> entry : move.asMap().entrySet()) {
					List<Pair<Integer, Vec3>> transforms = new ArrayList<>();
					List<Pair<Integer, Vec3>> rotations = new ArrayList<>();
					List<Pair<Integer, Vec3>> scales = new ArrayList<>();
					List<Pair<Integer, Vec3>> bends = new ArrayList<>();
					JsonObject jsonObject = (JsonObject) entry.getValue();
					float x = jsonObject.has("x") ? jsonObject.get("x").getAsFloat() : 0;
					float y = jsonObject.has("y") ? jsonObject.get("y").getAsFloat() : 0;
					float z = jsonObject.has("z") ? jsonObject.get("z").getAsFloat() : 0;
					float pitch = jsonObject.has("pitch") ? jsonObject.get("pitch").getAsFloat() : 0;
					float yaw = jsonObject.has("yaw") ? jsonObject.get("yaw").getAsFloat() : 0;
					float roll = jsonObject.has("roll") ? jsonObject.get("roll").getAsFloat() : 0;
					float scaleX = jsonObject.has("scaleX") ? jsonObject.get("scaleX").getAsFloat() : 1;
					float scaleY = jsonObject.has("scaleY") ? jsonObject.get("scaleY").getAsFloat() : 1;
					float scaleZ = jsonObject.has("scaleZ") ? jsonObject.get("scaleZ").getAsFloat() : 1;
					float bendAxis = jsonObject.has("axis") ? jsonObject.get("axis").getAsFloat() : 0;
					float bend = jsonObject.has("bend") ? jsonObject.get("bend").getAsFloat() : 0;
					transforms.add(new Pair<>(currentTick, new Vec3(x, y, z)));
					rotations.add(new Pair<>(currentTick, new Vec3(pitch, yaw, roll)));
					scales.add(new Pair<>(currentTick, new Vec3(scaleX, scaleY, scaleZ)));
					bends.add(new Pair<>(currentTick, new Vec3(bendAxis, bend, 0)));

					boneAnims.add(new BoneAnimation(getCorrectPlayerBoneName(entry.getKey()), BakedAnimationsLoader.buildKeyframeStackFromLegacyAnim(rotations), BakedAnimationsLoader.buildKeyframeStackFromLegacyAnim(rotations), BakedAnimationsLoader.buildKeyframeStackFromLegacyAnim(scales), BakedAnimationsLoader.buildKeyframeStackFromLegacyAnim(bends)));
				}
			}
		}
		BoneAnimation[] boneAnimations = boneAnims.toArray(new BoneAnimation[]{});
		String name = obj.get("name").getAsString();
		return new Animation(name, BakedAnimationsLoader.calculateAnimationLength(boneAnimations),
				obj.get("emote").getAsJsonObject().get("isLoop").getAsBoolean() ? Animation.LoopType.LOOP : Animation.LoopType.PLAY_ONCE, boneAnimations, NO_KEYFRAMES, new HashMap<>(), new HashMap<>());
	}

	public static String getCorrectPlayerBoneName(String name) {
		return name.replaceAll("([A-Z])", "_$1").toLowerCase();
	}
}
