package com.zigythebird.playeranimcore.loading;

import com.google.gson.*;
import com.zigythebird.playeranimcore.PlayerAnimLib;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

public class UniversalAnimLoader implements JsonDeserializer<Map<String, Animation>> {
    public static final Animation.Keyframes NO_KEYFRAMES = new Animation.Keyframes(new SoundKeyframeData[]{}, new ParticleKeyframeData[]{}, new CustomInstructionKeyframeData[]{});

    public static Map<String, Animation> loadPlayerAnim(InputStream resource) throws IOException {
        try (Reader reader = new InputStreamReader(resource)) {
            JsonObject json = PlayerAnimLib.GSON.fromJson(reader, JsonObject.class);

            if (json.has("animations")) {
                return PlayerAnimLib.GSON.fromJson(json.get("animations"), PlayerAnimLib.ANIMATIONS_MAP_TYPE);
            } else {
                Animation animation = PlayerAnimatorLoader.GSON.fromJson(json, Animation.class);
                return Collections.singletonMap(animation.data().name(), animation);
            }
        }
    }

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("([A-Z])");
    public static String getCorrectPlayerBoneName(String name) {
        return UPPERCASE_PATTERN.matcher(name).replaceAll("_$1").toLowerCase();
    }

    @Override
    public Map<String, Animation> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        Map<String, Animation> animations = new Object2ObjectOpenHashMap<>(obj.size());

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            try {
                Animation animation = context.deserialize(entry.getValue().getAsJsonObject(), Animation.class);
                if (!animation.data().has(ExtraAnimationData.NAME_KEY)) { // Fallback to name only
                    animation.data().put(ExtraAnimationData.NAME_KEY, entry.getKey());
                }
                animations.put(entry.getKey(), animation);
            } catch (Exception ex) {
                PlayerAnimLib.LOGGER.error("Unable to parse animation: {}", entry.getKey(), ex);
            }
        }

        return animations;
    }
}
