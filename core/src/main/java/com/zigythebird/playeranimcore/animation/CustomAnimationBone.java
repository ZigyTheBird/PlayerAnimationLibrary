package com.zigythebird.playeranimcore.animation;

import com.google.gson.*;
import com.zigythebird.playeranimcore.math.Vec3f;

import java.lang.reflect.Type;

public record CustomAnimationBone(Vec3f pivot, JsonObject geometry) {
    public static class Deserializer implements JsonDeserializer<CustomAnimationBone> {
        @Override
        public CustomAnimationBone deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            return new CustomAnimationBone(ctx.deserialize(obj.get("pivot"), Vec3f.class), obj.getAsJsonObject("geometry"));
        }
    }
}
