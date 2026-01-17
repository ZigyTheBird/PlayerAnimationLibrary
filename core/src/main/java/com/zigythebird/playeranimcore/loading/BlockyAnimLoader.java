package com.zigythebird.playeranimcore.loading;

import com.google.gson.*;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranimcore.math.Vec3f;

import java.lang.reflect.Type;
import java.util.*;

import static com.zigythebird.playeranimcore.loading.UniversalAnimLoader.NO_KEYFRAMES;
import static java.util.Map.entry;

public class BlockyAnimLoader implements JsonDeserializer<Animation> {
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(Animation.class, new BlockyAnimLoader()).create();

    private static final float TIME_SCALE = 20.0f / 60.0f;

    private static final Map<String, String> RENAMES = Map.of(
            "head", "headhy"
    );
    private static final Map<String, Vec3f> PIVOTS = Map.ofEntries(
            // Body
            entry("origin", new Vec3f(0, 0, 0)),
            entry("pelvis", new Vec3f(0, 51, 0)),
            entry("belly", new Vec3f(0, 55, 0)),
            entry("chest", new Vec3f(0, 68, -3)),

            // Head
            entry("headhy", new Vec3f(0, 87, -1)),

            // Right Arm
            entry("r-shoulder", new Vec3f(-14.5017F, 86.594F, -0.9388F)),
            entry("r-arm", new Vec3f(-14.5017F, 86.594F, -0.9388F)),

            // Left Arm
            entry("l-shoulder", new Vec3f(14.5017F, 86.594F, -0.9388F)),
            entry("l-arm", new Vec3f(14.5017F, 86.594F, -0.9388F)),

            // Right Leg
            entry("r-thigh", new Vec3f(-7.5F, 50, 1)),

            // Left Leg
            entry("l-thigh", new Vec3f(7.5F, 50, 1)),

            // Cape
            entry("back-attachment", new Vec3f(0, 79, -18))
    );
    private static final Map<String, String> PARENTS = Map.ofEntries(
            // Body
            entry("pelvis", "origin"),
            entry("belly", "pelvis"),
            entry("chest", "belly"),
            entry("body", "pelvis"), // Minecraft

            // Torso
            entry("torso", "chest"), // Minecraft TODO

            // Head
            entry("headhy", "chest"),
            entry("head", "headhy"), // Minecraft

            // Right Arm
            entry("r-shoulder", "chest"),
            entry("r-arm", "r-shoulder"),
            entry("right_arm", "r-arm"), // Minecraft

            // Left Arm
            entry("l-shoulder", "chest"),
            entry("l-arm", "l-shoulder"),
            entry("left_arm", "l-arm"), // Minecraft

            // Right Leg
            entry("r-thigh", "pelvis"),
            entry("right_leg", "r-thigh"),

            // Left Leg
            entry("l-thigh", "pelvis"),
            entry("left_leg", "l-thigh"),

            // Cape
            entry("back-attachment", "chest"),
            entry("cape", "back-attachment") // Minecraft
    );
    private static final Map<String, String> BENDS = Map.of(
            "r-forearm", "right_arm",
            "l-forearm", "left_arm",
            "r-calf", "right_leg",
            "l-calf", "left_leg"
    );

    @Override
    public Animation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();
        float duration = root.has("duration") ? root.get("duration").getAsFloat() * TIME_SCALE : 0;
        boolean loop = root.has("holdLastKeyframe") && root.get("holdLastKeyframe").getAsBoolean();

        Map<String, BoneAnimation> animations = new HashMap<>();

        if (root.has("nodeAnimations")) {
            for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("nodeAnimations").entrySet()) {
                String src = entry.getKey();
                JsonObject data = entry.getValue().getAsJsonObject();


            }
        }

        return new Animation(new ExtraAnimationData(), duration, loop ? Animation.LoopType.HOLD_ON_LAST_FRAME : Animation.LoopType.PLAY_ONCE, animations, NO_KEYFRAMES, PIVOTS, PARENTS);
    }
}
