package com.zigythebird.playeranimcore.loading;

import com.google.gson.*;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranimcore.animation.keyframe.Keyframe;
import com.zigythebird.playeranimcore.animation.keyframe.KeyframeStack;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.math.Vec3f;
import com.zigythebird.playeranimcore.molang.MolangLoader;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.parser.ast.FloatExpression;

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

    private static final float SCALE = 24.0f / 87.0f;

    private static final Map<String, Vec3f> PIVOTS = Map.ofEntries(
            // Body
            entry("origin", new Vec3f(0, 0, 0)),
            entry("pelvis", new Vec3f(0, 51 * SCALE, 0)),
            entry("belly", new Vec3f(0, 55 * SCALE, 0)),
            entry("chest", new Vec3f(0, 68 * SCALE, -3 * SCALE)),

            // Head
            entry("headhy", new Vec3f(0, 87 * SCALE, -1 * SCALE)),

            // Right Arm
            entry("r-shoulder", new Vec3f(14.5f * SCALE, 86.6f * SCALE, -0.94f * SCALE)),
            entry("r-arm", new Vec3f(14.5f * SCALE, 86.6f * SCALE, -0.94f * SCALE)),

            // Left Arm
            entry("l-shoulder", new Vec3f(-14.5f * SCALE, 86.6f * SCALE, -0.94f * SCALE)),
            entry("l-arm", new Vec3f(-14.5f * SCALE, 86.6f * SCALE, -0.94f * SCALE)),

            // Right Leg
            entry("r-thigh", new Vec3f(7.5f * SCALE, 50 * SCALE, 1 * SCALE)),

            // Left Leg
            entry("l-thigh", new Vec3f(-7.5f * SCALE, 50 * SCALE, 1 * SCALE)),

            // Cape
            entry("back-attachment", new Vec3f(0, 79 * SCALE, -18 * SCALE))
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
            entry("right_leg", "r-thigh"), // Minecraft

            // Left Leg
            entry("l-thigh", "pelvis"),
            entry("left_leg", "l-thigh"), // Minecraft

            // Cape
            entry("back-attachment", "chest"),
            entry("cape", "back-attachment") // Minecraft
    );
    /*private static final Map<String, String> BENDS = Map.of(
            "r-forearm", "right_arm",
            "l-forearm", "left_arm",
            "r-calf", "right_leg",
            "l-calf", "left_leg"
    );*/

    @Override
    public Animation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();
        float duration = root.has("duration") ? root.get("duration").getAsFloat() * TIME_SCALE : 0;
        boolean loop = root.has("holdLastKeyframe") && root.get("holdLastKeyframe").getAsBoolean();

        Map<String, BoneAnimation> animations = new HashMap<>();

        if (root.has("nodeAnimations")) {
            for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("nodeAnimations").entrySet()) {
                String src = entry.getKey().toLowerCase(Locale.ROOT);
                JsonObject data = entry.getValue().getAsJsonObject();

                KeyframeStack rot = parseStack(data.getAsJsonArray("orientation"), TransformType.ROTATION);
                KeyframeStack pos = parseStack(data.getAsJsonArray("position"), TransformType.POSITION);
                KeyframeStack scl = parseStack(data.getAsJsonArray("shapeStretch"), TransformType.SCALE);
                animations.put(RENAMES.getOrDefault(src, src), new BoneAnimation(rot, pos, scl, new ArrayList<>()));
            }
        }

        return new Animation(new ExtraAnimationData(), duration, loop ? Animation.LoopType.HOLD_ON_LAST_FRAME : Animation.LoopType.PLAY_ONCE, animations, NO_KEYFRAMES, PIVOTS, PARENTS);
    }

    private KeyframeStack parseStack(JsonArray json, TransformType type) {
        if (json == null || json.isEmpty()) return new KeyframeStack();

        List<Keyframe> xFrames = new ObjectArrayList<>();
        List<Keyframe> yFrames = new ObjectArrayList<>();
        List<Keyframe> zFrames = new ObjectArrayList<>();

        List<Expression> xPrev = null;
        List<Expression> yPrev = null;
        List<Expression> zPrev = null;
        float prevTime = 0;

        Expression defaultValue = type == TransformType.SCALE ? FloatExpression.ONE : FloatExpression.ZERO;

        for (JsonElement element : json) {
            JsonObject keyframe = element.getAsJsonObject();
            JsonObject delta = keyframe.getAsJsonObject("delta");

            float curTime = keyframe.get("time").getAsFloat() * TIME_SCALE;
            float timeDelta = curTime - prevTime;

            List<Expression> xValue, yValue, zValue;
            if (type == TransformType.ROTATION) {
                Vector3f vector3f = new Quaternionf(
                        delta.get("x").getAsFloat(), delta.get("y").getAsFloat(), delta.get("z").getAsFloat(), delta.get("w").getAsFloat()
                ).normalize().getEulerAnglesZYX(new Vector3f());

                xValue = Collections.singletonList(FloatExpression.of(vector3f.x()));
                yValue = Collections.singletonList(FloatExpression.of(-vector3f.y()));
                zValue = Collections.singletonList(FloatExpression.of(-vector3f.z()));
            } else {
                xValue = MolangLoader.parseJson(false, delta.get("x"), defaultValue);
                yValue = MolangLoader.parseJson(false, delta.get("y"), defaultValue);
                zValue = MolangLoader.parseJson(false, delta.get("z"), defaultValue);
            }

            EasingType easingType = keyframe.has("interpolationType") && keyframe.get("interpolationType").getAsString().equals("smooth") ? EasingType.CATMULLROM : EasingType.LINEAR;
            xFrames.add(new Keyframe(timeDelta, xPrev == null ? xValue : xPrev, xValue, easingType, Collections.emptyList()));
            yFrames.add(new Keyframe(timeDelta, yPrev == null ? yValue : yPrev, yValue, easingType, Collections.emptyList()));
            zFrames.add(new Keyframe(timeDelta, zPrev == null ? zValue : zPrev, zValue, easingType, Collections.emptyList()));

            xPrev = xValue; yPrev = yValue; zPrev = zValue;
            prevTime = curTime;
        }

        return new KeyframeStack(
                AnimationLoader.addArgsForKeyframes(xFrames),
                AnimationLoader.addArgsForKeyframes(yFrames),
                AnimationLoader.addArgsForKeyframes(zFrames)
        );
    }
}
