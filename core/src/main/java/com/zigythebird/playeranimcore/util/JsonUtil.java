package com.zigythebird.playeranimcore.util;

import com.google.gson.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Json helper class for various json functions
 */
public final class JsonUtil {
    private JsonUtil() {}

    /**
     * Convert a {@link JsonArray} of floats to a {@code float[]}
     * <p>
     * No type checking is done, so if the array contains anything other than floats, this will throw an exception
     * <p>
     * Ensures a minimum size of 3, as this is the expected usage of this method
     */
    public static float[] jsonArrayToFloatArray(@Nullable JsonArray array) throws JsonParseException{
        if (array == null)
            return new float[3];

        float[] output = new float[array.size()];

        for (int i = 0; i < array.size(); i++) {
            output[i] = array.get(i).getAsFloat();
        }

        return output;
    }

    /**
     * Converts a {@link JsonArray} of a given object type to an array of that object, deserialized from their respective {@link JsonElement JsonElements}
     *
     * @param array The array containing the objects to be converted
     * @param context The {@link com.google.gson.Gson} context for deserialization
     * @param objectClass The object type that the array contains
     */
    public static <T> T[] jsonArrayToObjectArray(JsonArray array, JsonDeserializationContext context, Class<T> objectClass) {
        T[] objArray = (T[]) Array.newInstance(objectClass, array.size());

        for (int i = 0; i < array.size(); i++) {
            objArray[i] = context.deserialize(array.get(i), objectClass);
        }

        return objArray;
    }

    /**
     * Converts a {@link JsonArray} to a {@link List} of elements of a pre-determined type
     *
     * @param array The {@code JsonArray} to convert
     * @param elementTransformer Transformation function that converts a {@link JsonElement} to the intended output object
     */
    public static <T> List<T> jsonArrayToList(@Nullable JsonArray array, Function<JsonElement, T> elementTransformer) {
        if (array == null)
            return new ObjectArrayList<>();

        List<T> list = new ObjectArrayList<>(array.size());

        for (JsonElement element : array) {
            list.add(elementTransformer.apply(element));
        }

        return list;
    }

    /**
     * Converts a {@link JsonObject} to a {@link Map} of String keys to their respective objects
     *
     * @param obj The base {@code JsonObject} to convert
     * @param context The {@link Gson} deserialization context
     * @param objectType The object class that the map should contain
     */
    public static <T> Map<String, T> jsonObjToMap(JsonObject obj, JsonDeserializationContext context, Class<T> objectType) {
        Map<String, T> map = new Object2ObjectOpenHashMap<>(obj.size());

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            map.put(entry.getKey(), context.deserialize(entry.getValue(), objectType));
        }

        return map;
    }

    @Nullable
    @Contract("_,_,!null->!null;_,_,null->_")
    public static String getAsString(JsonObject json, String memberName, @Nullable String fallback) {
        return json.has(memberName) ? convertToString(json.get(memberName), memberName) : fallback;
    }

    /**
     * Retrieves an optionally present Long from the provided {@link JsonObject}, or null if the element isn't present
     */
    @Nullable
    public static Long getOptionalLong(JsonObject obj, String elementName) {
        return obj.has(elementName) ? getAsLong(obj, elementName) : null;
    }

    public static long convertToLong(JsonElement json, String memberName) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            return json.getAsLong();
        } else {
            throw new JsonSyntaxException("Expected " + memberName + " to be a Long, was " + getType(json));
        }
    }

    public static long getAsLong(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return convertToLong(json.get(memberName), memberName);
        } else {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Long");
        }
    }

    public static long getAsLong(JsonObject json, String memberName, long fallback) {
        return json.has(memberName) ? convertToLong(json.get(memberName), memberName) : fallback;
    }

    /**
     * Retrieves an optionally present Boolean from the provided {@link JsonObject}, or null if the element isn't present
     */
    @Nullable
    public static Boolean getOptionalBoolean(JsonObject obj, String elementName) {
        return obj.has(elementName) ? getAsBoolean(obj, elementName) : null;
    }

    public static boolean convertToBoolean(JsonElement json, String memberName) {
        if (json.isJsonPrimitive()) {
            return json.getAsBoolean();
        } else {
            throw new JsonSyntaxException("Expected " + memberName + " to be a Boolean, was " + getType(json));
        }
    }

    public static boolean getAsBoolean(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return convertToBoolean(json.get(memberName), memberName);
        } else {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Boolean");
        }
    }

    public static boolean getAsBoolean(JsonObject json, String memberName, boolean fallback) {
        return json.has(memberName) ? convertToBoolean(json.get(memberName), memberName) : fallback;
    }

    /**
     * Retrieves an optionally present Float from the provided {@link JsonObject}, or null if the element isn't present
     */
    @Nullable
    public static Float getOptionalFloat(JsonObject obj, String elementName) {
        return obj.has(elementName) ? getAsFloat(obj, elementName) : null;
    }

    public static float convertToFloat(JsonElement json, String memberName) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            return json.getAsFloat();
        } else {
            throw new JsonSyntaxException("Expected " + memberName + " to be a Float, was " + getType(json));
        }
    }

    public static float getAsFloat(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return convertToFloat(json.get(memberName), memberName);
        } else {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Float");
        }
    }

    public static float getAsFloat(JsonObject json, String memberName, float fallback) {
        return json.has(memberName) ? convertToFloat(json.get(memberName), memberName) : fallback;
    }

    /**
     * Retrieves an optionally present Double from the provided {@link JsonObject}, or null if the element isn't present
     */
    @Nullable
    public static Double getOptionalDouble(JsonObject obj, String elementName) {
        return obj.has(elementName) ? getAsDouble(obj, elementName) : null;
    }

    public static double convertToDouble(JsonElement json, String memberName) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            return json.getAsDouble();
        } else {
            throw new JsonSyntaxException("Expected " + memberName + " to be a Double, was " + getType(json));
        }
    }

    public static double getAsDouble(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return convertToDouble(json.get(memberName), memberName);
        } else {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Double");
        }
    }

    public static double getAsDouble(JsonObject json, String memberName, double fallback) {
        return json.has(memberName) ? convertToDouble(json.get(memberName), memberName) : fallback;
    }

    /**
     * Retrieves an optionally present Integer from the provided {@link JsonObject}, or null if the element isn't present
     */
    @Nullable
    public static Integer getOptionalInteger(JsonObject obj, String elementName) {
        return obj.has(elementName) ? getAsInt(obj, elementName) : null;
    }

    public static int convertToInt(JsonElement json, String memberName) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            return json.getAsInt();
        } else {
            throw new JsonSyntaxException("Expected " + memberName + " to be a Int, was " + getType(json));
        }
    }

    public static int getAsInt(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return convertToInt(json.get(memberName), memberName);
        } else {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Int");
        }
    }

    public static int getAsInt(JsonObject json, String memberName, int fallback) {
        return json.has(memberName) ? convertToInt(json.get(memberName), memberName) : fallback;
    }

    public static JsonObject getAsJsonObject(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return convertToJsonObject(json.get(memberName), memberName);
        } else {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find a JsonObject");
        }
    }

    public static JsonObject convertToJsonObject(JsonElement json, String memberName) {
        if (json.isJsonObject()) {
            return json.getAsJsonObject();
        } else {
            throw new JsonSyntaxException("Expected " + memberName + " to be a JsonObject, was " + getType(json));
        }
    }

    @Nullable
    @Contract("_,_,!null->!null;_,_,null->_")
    public static JsonObject getAsJsonObject(JsonObject json, String memberName, @Nullable JsonObject fallback) {
        return json.has(memberName) ? convertToJsonObject(json.get(memberName), memberName) : fallback;
    }

    public static String convertToString(JsonElement json, String memberName) {
        if (json.isJsonPrimitive()) {
            return json.getAsString();
        } else {
            throw new JsonSyntaxException("Expected " + memberName + " to be a string, was " + getType(json));
        }
    }

    public static String getAsString(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return convertToString(json.get(memberName), memberName);
        } else {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find a string");
        }
    }

    public static JsonArray convertToJsonArray(JsonElement json, String memberName) {
        if (json.isJsonArray()) {
            return json.getAsJsonArray();
        } else {
            throw new JsonSyntaxException("Expected " + memberName + " to be a JsonArray, was " + getType(json));
        }
    }

    public static JsonArray getAsJsonArray(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return convertToJsonArray(json.get(memberName), memberName);
        } else {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find a JsonArray");
        }
    }

    @Nullable
    @Contract("_,_,!null->!null;_,_,null->_")
    public static JsonArray getAsJsonArray(JsonObject json, String memberName, @Nullable JsonArray fallback) {
        return json.has(memberName) ? convertToJsonArray(json.get(memberName), memberName) : fallback;
    }

    public static String getType(@Nullable JsonElement json) {
        String string = String.valueOf(json);
        if (json == null) {
            return "null (missing)";
        } else if (json.isJsonNull()) {
            return "null (json)";
        } else if (json.isJsonArray()) {
            return "an array (" + string + ")";
        } else if (json.isJsonObject()) {
            return "an object (" + string + ")";
        } else {
            if (json.isJsonPrimitive()) {
                JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
                if (jsonPrimitive.isNumber()) {
                    return "a number (" + string + ")";
                }

                if (jsonPrimitive.isBoolean()) {
                    return "a boolean (" + string + ")";
                }
            }

            return string;
        }
    }
}
