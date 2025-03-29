package com.zigythebird.playeranim.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranim.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranim.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranim.bones.PlayerAnimBone;
import com.zigythebird.playeranim.loading.AnimationLoader;
import com.zigythebird.playeranim.loading.PlayerAnimatorLoader;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Cache class for holding loaded {@link Animation Animations}
 */
public final class PlayerAnimResources {
	public static final Animation.Keyframes NO_KEYFRAMES = new Animation.Keyframes(new SoundKeyframeData[]{}, new ParticleKeyframeData[]{}, new CustomInstructionKeyframeData[]{});
	private static final Map<ResourceLocation, Animation> ANIMATIONS = new Object2ObjectOpenHashMap<>();

	/**
	 * Get an animation from the registry, using Identifier(MODID, animation_name) as the key.
	 * @return animation, <code>null</code> if no animation
	 */
	public static @Nullable Animation getAnimation(ResourceLocation id) {
		if (!ANIMATIONS.containsKey(id)) return null;
		return ANIMATIONS.get(id);
	}

	/**
	 * Get Optional animation from registry
	 * @param identifier identifier
	 * @return Optional animation
	 */
	@NotNull
	public static Optional<Animation> getAnimationOptional(@NotNull ResourceLocation identifier) {
		return Optional.ofNullable(getAnimation(identifier));
	}

	/**
	 * @return an unmodifiable map of all the animations
	 */
	public static Map<ResourceLocation, Animation> getAnimations() {
		return Map.copyOf(ANIMATIONS);
	}

	/**
	 * Returns the animations of a specific mod/namespace
	 * @param modid namespace (assets/modid)
	 * @return map of path and animations
	 */
	@NotNull
	public static Map<String, Animation> getModAnimations(@NotNull String modid) {
		HashMap<String, Animation> map = new HashMap<>();
		for (Map.Entry<ResourceLocation, Animation> entry: ANIMATIONS.entrySet()) {
			if (entry.getKey().getNamespace().equals(modid)) {
				map.put(entry.getKey().getPath(), entry.getValue());
			}
		}
		return map;
	}

	/**
	 * @param id ID of the desired animation.
	 * @return Returns true if that animation is available.
	 */
	public static boolean hasAnimation(ResourceLocation id) {
		return ANIMATIONS.containsKey(id);
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
				Map<String, Animation> anim = AnimationLoader.deserialize(modifiedJson, bones, parents);
				for (Map.Entry<String, Animation> entry : anim.entrySet()) {
					ANIMATIONS.put(ResourceLocation.parse(entry.getKey()), entry.getValue());
				}
			} else {
				Animation animation = PlayerAnimatorLoader.GSON.fromJson(json, Animation.class);
				ANIMATIONS.put(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), animation.data().name()), animation);
			}
		}
		catch (Exception e) {
			ModInit.LOGGER.error("Player Animation Library failed to load animation {}:", id, e);
		}
	}

	public static String getCorrectPlayerBoneName(String name) {
		return name.replaceAll("([A-Z])", "_$1").toLowerCase();
	}
}
