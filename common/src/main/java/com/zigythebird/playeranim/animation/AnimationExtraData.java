package com.zigythebird.playeranim.animation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record AnimationExtraData(Map<String, Object> data) {
    public static final String NAME_KEY = "name";
    public static final String UUID_KEY = "uuid";

    public AnimationExtraData(String key, Object value) {
        this(new HashMap<>(Collections.singletonMap(key, value)));
    }

    public AnimationExtraData() {
        this(new HashMap<>(1)); // Mutable, 1 for name;
    }

    @Nullable
    public String name() {
        return (String) data().get(NAME_KEY);
    }

    /**
     * Not null for playeranimator animations, for geckolib most likely null
     */
    public UUID uuid() {
        return UUID.fromString((String) data().get(UUID_KEY));
    }

    public void fillJsonData(JsonObject node) {
        for (Map.Entry<String, JsonElement> entry : node.entrySet()) {
            String string = entry.getKey();
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive()) {
                JsonPrimitive p = value.getAsJsonPrimitive();
                if (p.isBoolean()) {
                    data().put(string, p.getAsBoolean());
                } else if (p.isString()) {
                    data().put(string, p.getAsString());
                } else if (p.isNumber()) {
                    data().put(string, p.getAsDouble());
                } else {
                    data().put(string, p.toString());
                }
            }
        }
    }
}
