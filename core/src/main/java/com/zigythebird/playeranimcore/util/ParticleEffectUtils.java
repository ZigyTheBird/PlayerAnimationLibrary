package com.zigythebird.playeranimcore.util;

import com.google.gson.JsonObject;
import com.zigythebird.playeranimcore.PlayerAnimLib;

public class ParticleEffectUtils {
    public static String parseIdentifier(String raw) {
        return getIdentifier(PlayerAnimLib.GSON.fromJson(raw, JsonObject.class));
    }

    public static String getIdentifier(JsonObject obj) {
        if (obj.has("particle_effect")) obj = obj.getAsJsonObject("particle_effect");
        return obj.getAsJsonObject("description").get("identifier").getAsString();
    }
}
