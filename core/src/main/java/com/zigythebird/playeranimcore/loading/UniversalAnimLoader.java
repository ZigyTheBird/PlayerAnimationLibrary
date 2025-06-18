package com.zigythebird.playeranimcore.loading;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zigythebird.playeranimcore.PlayerAnimLib;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranimcore.bones.PivotBone;
import com.zigythebird.playeranimcore.math.Vec3f;
import com.zigythebird.playeranimcore.misc.JsonUtil;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UniversalAnimLoader {
    public static final Animation.Keyframes NO_KEYFRAMES = new Animation.Keyframes(new SoundKeyframeData[]{}, new ParticleKeyframeData[]{}, new CustomInstructionKeyframeData[]{});

    public static Map<String, Animation> loadPlayerAnim(String namespace, InputStream resource) {
        JsonObject json = PlayerAnimLib.GSON.fromJson(new InputStreamReader(resource), JsonObject.class);
        if (json.has("animations")) {
            JsonObject model = JsonUtil.getAsJsonObject(json, "model", new JsonObject());
            Map<String, PivotBone> bones = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : model.entrySet()) {
                JsonObject object = entry.getValue().getAsJsonObject();
                JsonArray pivot = object.get("pivot").getAsJsonArray();
                PivotBone bone = new PivotBone(entry.getKey(), new Vec3f(pivot.get(0).getAsFloat(), pivot.get(1).getAsFloat(), pivot.get(2).getAsFloat()));
                bones.put(entry.getKey(), bone);
            }

            JsonObject parentsObj = JsonUtil.getAsJsonObject(json, "parents", new JsonObject());
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
                modifiedJson.add(namespace + ":" + entry.getKey(), entryJson);
            }
            return AnimationLoader.deserialize(modifiedJson, bones, parents);
        } else {
            Animation animation = PlayerAnimatorLoader.GSON.fromJson(json, Animation.class);
            return Collections.singletonMap(namespace + ":" + animation.data().name(), animation);
        }
    }

    public static String getCorrectPlayerBoneName(String name) {
        return name.replaceAll("([A-Z])", "_$1").toLowerCase();
    }
}
