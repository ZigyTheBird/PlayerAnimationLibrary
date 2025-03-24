package com.zigythebird.playeranim.animation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record ExtraAnimationData(Map<String, Object> data) {
    public static final String NAME_KEY = "name";
    public static final String UUID_KEY = "uuid";

    public ExtraAnimationData(String key, Object value) {
        this(new HashMap<>(Collections.singletonMap(key, value)));
    }

    public ExtraAnimationData() {
        this(new HashMap<>(1)); // Mutable, 1 for name
    }

    @Nullable
    public String displayName() {
        return (String) data().get(NAME_KEY);
    }

    @Nullable
    public String name() {
        String name = displayName();
        return name != null ? name.toLowerCase(Locale.ROOT).replace("\"", "").replace(" ", "_") : null;
    }

    public boolean has(String name) {
        return data().containsKey(name);
    }

    public Object get(String name) {
        return data().get(name);
    }

    /**
     * Not null for playeranimator animations, for geckolib most likely null
     */
    public UUID uuid() {
        return UUID.fromString((String) data().get(UUID_KEY));
    }

    public void fromJson(JsonObject node) {
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
