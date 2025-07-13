package com.zigythebird.playeranimcore.loading;

import com.google.gson.*;
import com.zigythebird.playeranimcore.PlayerAnimLib;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranimcore.math.Vec3f;
import com.zigythebird.playeranimcore.util.JsonUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UniversalAnimLoader implements JsonDeserializer<Map<String, Animation>> {
    public static final Animation.Keyframes NO_KEYFRAMES = new Animation.Keyframes(new SoundKeyframeData[0], new ParticleKeyframeData[0], new CustomInstructionKeyframeData[0]);

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("([A-Z])");
    private static final Pattern UNDERSCORE_PATTERN = Pattern.compile("_(.)");

    public static Map<String, Animation> loadAnimations(InputStream resource) throws IOException {
        try (Reader reader = new InputStreamReader(resource)) {
            JsonObject json = PlayerAnimLib.GSON.fromJson(reader, JsonObject.class);

            if (json.has("animations")) {
                Map<String, Animation> animationMap = PlayerAnimLib.GSON.fromJson(json.get("animations"), PlayerAnimLib.ANIMATIONS_MAP_TYPE);
                if (json.has("parents") && json.has("model")) {
                    Map<String, String> parents = UniversalAnimLoader.getParents(JsonUtil.getAsJsonObject(json, "parents", new JsonObject()));
                    Map<String, Vec3f> bones = UniversalAnimLoader.getModel(JsonUtil.getAsJsonObject(json, "model", new JsonObject()));
                    for (Animation animation : animationMap.values()) {
                        if (animation.bones().isEmpty()) {
                            animation.parents().putAll(parents);
                            animation.bones().putAll(bones);
                        }
                    }
                }
                return animationMap;
            } else {
                Animation animation = PlayerAnimatorLoader.GSON.fromJson(json, Animation.class);
                return Collections.singletonMap(animation.data().name(), animation);
            }
        }
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

    public static Map<String, String> getParents(JsonObject parentsObj) {
        Map<String, String> parents = new HashMap<>(parentsObj.size());
        for (Map.Entry<String, JsonElement> entry : parentsObj.entrySet()) {
            parents.put(UniversalAnimLoader.getCorrectPlayerBoneName(entry.getKey()), entry.getValue().getAsString());
        }
        return parents;
    }

    public static Map<String, Vec3f> getModel(JsonObject modelObj) {
        Map<String, Vec3f> bones = new HashMap<>(modelObj.size());
        for (Map.Entry<String, JsonElement> entry : modelObj.entrySet()) {
            JsonObject object = entry.getValue().getAsJsonObject();
            JsonArray pivot = object.get("pivot").getAsJsonArray();
            Vec3f bone = new Vec3f(pivot.get(0).getAsFloat(), pivot.get(1).getAsFloat(), pivot.get(2).getAsFloat());
            bones.put(entry.getKey(), bone);
        }
        return bones;
    }

    public static String getCorrectPlayerBoneName(String name) {
        return UPPERCASE_PATTERN.matcher(name).replaceAll("_$1").toLowerCase();
    }

    public static String restorePlayerBoneName(String name) {
        StringBuilder result = new StringBuilder();
        String lowerCase = name.toLowerCase();

        Matcher matcher = UNDERSCORE_PATTERN.matcher(lowerCase);
        int lastEnd = 0;
        while (matcher.find()) {
            result.append(lowerCase, lastEnd, matcher.start());
            result.append(Character.toUpperCase(matcher.group(1).charAt(0)));
            lastEnd = matcher.end();
        }
        result.append(lowerCase.substring(lastEnd));
        return result.toString();
    }
}
