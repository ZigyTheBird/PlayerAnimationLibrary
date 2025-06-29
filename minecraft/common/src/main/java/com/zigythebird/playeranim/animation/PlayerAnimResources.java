package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.PlayerAnimLibMod;
import com.zigythebird.playeranimcore.PlayerAnimLib;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cache class for holding loaded {@link Animation Animations}
 */
public class PlayerAnimResources implements ResourceManagerReloadListener {
	public static final ResourceLocation KEY = PlayerAnimLibMod.id("animation");
	private static final Map<ResourceLocation, Animation> ANIMATIONS = new Object2ObjectOpenHashMap<>();

	/**
	 * Get an animation from the registry, using Identifier(mod_id, animation_name) as the key.
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
		return Collections.unmodifiableMap(ANIMATIONS);
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
	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		ANIMATIONS.clear();

		for (var resource : manager.listResources("player_animations", resourceLocation -> resourceLocation.getPath().endsWith(".json")).entrySet()) {
			try (InputStream is = resource.getValue().open()) {
				for (var entry : UniversalAnimLoader.loadPlayerAnim(is).entrySet()) {
					ANIMATIONS.put(ResourceLocation.parse(entry.getKey()), entry.getValue());
				}
			} catch (Exception e) {
				PlayerAnimLib.LOGGER.error("Player Animation Library failed to load animation {} because:", resource.getKey(), e);
			}
		}
	}
}
