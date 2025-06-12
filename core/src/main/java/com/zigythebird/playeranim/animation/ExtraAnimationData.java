package com.zigythebird.playeranim.animation;

import com.google.gson.JsonArray;
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

    /*@Nullable
    public String displayName() { TODO
        Object name = data().get(NAME_KEY);
        if (name instanceof JsonObject jsonObject) {
            return Component.translatableWithFallback(jsonObject.get("translate").getAsString(), jsonObject.get("fallback").getAsString()).toString();
        }
        return (String) name;
    }*/

    @Nullable
    public String name() {
        Object data = data().get(NAME_KEY);
        String name;
        if (data instanceof JsonObject jsonObject) {
            name = jsonObject.get("fallback").getAsString();
        } else name = (String) data;
        return name != null ? name.toLowerCase(Locale.ROOT).replace("\"", "").replace(" ", "_") : null;
    }

    public boolean has(String name) {
        return data().containsKey(name);
    }

    public Object getRaw(String name) {
        return data().get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        Object obj = getRaw(key);
        if (obj == null) return Optional.empty();

        try {
            return Optional.of((T) obj);
        } catch (Throwable ignored) {}

        return Optional.empty();
    }

    public void put(String name, Object object) {
        data.put(name, object);
    }

    /**
     * Not null for playeranimator animations, for geckolib most likely null
     */
    public UUID uuid() {
        return UUID.fromString((String) data().get(UUID_KEY));
    }

    public void fromJson(JsonObject node) {
        for (Map.Entry<String, JsonElement> entry : node.entrySet()) {
            data().put(entry.getKey(), getValue(entry.getValue()));
        }
    }
    
    public Object getValue(JsonElement element) {
        if (element instanceof JsonPrimitive p) {
            if (p.isBoolean()) {
                return p.getAsBoolean();
            } else if (p.isString()) {
                return p.getAsString();
            } else if (p.isNumber()) {
                return p.getAsFloat();
            }
        }
        if (element instanceof JsonArray array) {
            List<Object> list = new ArrayList<>();
            for (JsonElement element1 : array) {
                list.add(getValue(element1));
            }
        }
        if (element instanceof JsonObject object) {
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                map.put(entry.getKey(), getValue(entry.getValue()));
            }
        }
        return element;
    }

    public ExtraAnimationData copy() {
        return new ExtraAnimationData(new HashMap<>(){{putAll(data());}});
    }
}
