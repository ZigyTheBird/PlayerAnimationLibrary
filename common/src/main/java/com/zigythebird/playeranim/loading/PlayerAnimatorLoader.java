package com.zigythebird.playeranim.loading;

import com.google.gson.*;
import com.zigythebird.playeranim.animation.Animation;
import com.zigythebird.playeranim.animation.EasingType;
import com.zigythebird.playeranim.animation.ExtraAnimationData;
import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranim.animation.keyframe.Keyframe;
import com.zigythebird.playeranim.animation.keyframe.KeyframeStack;
import com.zigythebird.playeranim.enums.AnimationFormat;
import com.zigythebird.playeranim.enums.TransformType;
import com.zigythebird.playeranim.math.MathHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import team.unnamed.mocha.parser.ast.DoubleExpression;
import team.unnamed.mocha.parser.ast.Expression;

import java.lang.reflect.Type;
import java.util.*;

import static com.zigythebird.playeranim.animation.PlayerAnimResources.NO_KEYFRAMES;

public class PlayerAnimatorLoader implements JsonDeserializer<Animation> {
    private final static int modVersion = 3;

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Animation.class, new PlayerAnimatorLoader())
            .create();

    protected PlayerAnimatorLoader() {}

    /**
     * I think, we can stick with this <i>legacy</i> name: <code>emote</code>
     */
    @Override
    public Animation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject node = json.getAsJsonObject();
        if (!node.has("emote")) {
            throw new JsonParseException("not an emotecraft animation");
        }

        int version = 1;
        if (node.has("version")) {
            version = node.get("version").getAsInt();
        }

        ExtraAnimationData extra = new ExtraAnimationData();
        extra.fromJson(node);
        extra.put("format", AnimationFormat.PLAYER_ANIMATOR);

        if (modVersion < version){
            throw new JsonParseException(extra.name() + " is version " + version + ". Player Animation library can only process version " + modVersion + ".");
        }

        return emoteDeserializer(extra, node.getAsJsonObject("emote"), version);
    }

    private Animation emoteDeserializer(ExtraAnimationData extra, JsonObject node, int version) throws JsonParseException {
        boolean easeBeforeKeyframe = node.has("easeBeforeKeyframe") && node.get("easeBeforeKeyframe").getAsBoolean();
        extra.put("easeBeforeKeyframe", easeBeforeKeyframe);
        float beginTick = 0;
        if (node.has("beginTick")) {
            beginTick = node.get("beginTick").getAsFloat();
            extra.put("beginTick", beginTick);
        }
        float endTick = beginTick + 1;
        if (node.has("endTick")) {
            endTick = Math.max(node.get("endTick").getAsFloat(), endTick);
            extra.put("endTick", endTick);
        }
        if(endTick <= 0) throw new JsonParseException("endTick must be bigger than 0");
        Animation.LoopType loopType = Animation.LoopType.PLAY_ONCE;
        if(node.has("isLoop") && node.has("returnTick")) {
            boolean isLooped = node.get("isLoop").getAsBoolean();
            int returnTick = node.get("returnTick").getAsInt();
            if (isLooped) {
                if (returnTick > endTick || returnTick < 0) {
                    throw new JsonParseException("The returnTick has to be a non-negative value smaller than the endTick value");
                }
                if (returnTick == 0) loopType = Animation.LoopType.LOOP;
                else loopType = Animation.LoopType.returnToTickLoop(returnTick);
            }
        }

        if (node.has("nsfw")) extra.data().put(
                "nsfw", node.get("nsfw").getAsBoolean()
        );

        float stopTick = node.has("stopTick") ? node.get("stopTick").getAsFloat() : 0;
        endTick = stopTick <= endTick ? endTick + 3 : stopTick; // https://github.com/KosmX/minecraftPlayerAnimator/blob/1.21/coreLib/src/main/java/dev/kosmx/playerAnim/core/data/KeyframeAnimation.java#L80

        boolean degrees = !node.has("degrees") || node.get("degrees").getAsBoolean();
        BoneAnimation[] bones = moveDeserializer(node.getAsJsonArray("moves").asList(), degrees, version);

        for (BoneAnimation boneAnimation : bones) {
            resolveConstantEasing(boneAnimation.positionKeyFrames(), easeBeforeKeyframe);
            resolveConstantEasing(boneAnimation.rotationKeyFrames(), easeBeforeKeyframe);
            resolveConstantEasing(boneAnimation.scaleKeyFrames(), easeBeforeKeyframe);
            resolveConstantEasing(boneAnimation.bendKeyFrames(), easeBeforeKeyframe);
        }

        return new Animation(extra, endTick, loopType, bones, NO_KEYFRAMES, new HashMap<>(), new HashMap<>());
    }

    private void resolveConstantEasing(KeyframeStack<Keyframe> keyframeStack, boolean easeBefore) {
        resolveConstantEasing(keyframeStack.xKeyframes(), easeBefore);
        resolveConstantEasing(keyframeStack.yKeyframes(), easeBefore);
        resolveConstantEasing(keyframeStack.zKeyframes(), easeBefore);
    }

    private void resolveConstantEasing(List<Keyframe> list, boolean easeBefore) {
        List<Integer> constantIndexes = new ArrayList<>();
        for (Keyframe keyframe : list) {
            if (keyframe.easingType() == null) constantIndexes.add(list.indexOf(keyframe));
        }
        for (Integer index : constantIndexes) {
            Keyframe keyframe = list.get(index);
            list.set(index, new Keyframe(keyframe.length(), keyframe.startValue(), keyframe.endValue(), EasingType.LINEAR, keyframe.easingArgs()));
            int constantKeyframeIndex = easeBefore ? index : index + 1;
            list.add(constantKeyframeIndex, new Keyframe(list.get(constantKeyframeIndex).length() - 0.001F, keyframe.endValue(), keyframe.endValue(), EasingType.LINEAR, keyframe.easingArgs()));
            keyframe = list.get(constantKeyframeIndex + 1);
            list.set(constantKeyframeIndex + 1, new Keyframe(0.001F, keyframe.startValue(), keyframe.endValue(), EasingType.LINEAR, keyframe.easingArgs()));
        }
    }

    private BoneAnimation[] moveDeserializer(List<JsonElement> node, boolean degrees, int version) {
        Map<String, BoneAnimation> bones = new HashMap<>();
        node.sort((e1, e2) -> {
            final int i1 = e1.getAsJsonObject().get("tick").getAsInt();
            final int i2 = e2.getAsJsonObject().get("tick").getAsInt();
            return Integer.compare(i1, i2);
        });
        for (JsonElement n : node) {
            JsonObject obj = n.getAsJsonObject();
            float tick = obj.get("tick").getAsFloat();
            EasingType easing = easingTypeFromString(obj.has("easing") ? obj.get("easing").getAsString() : "linear");
            Float easingArg = null;
            try {
                if (obj.has("easingArg")) {
                    easingArg = obj.get("easingArg").getAsFloat();
                }
            }
            catch (NullPointerException ignore) {}
            int turn = obj.has("turn") ? obj.get("turn").getAsInt() : 0;
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()){
                if(entry.getKey().equals("tick") || entry.getKey().equals("comment") || entry.getKey().equals("easing") || entry.getKey().equals("turn")) {
                    continue;
                }

                String boneKey = entry.getKey();
                if(version < 3 && boneKey.equals("torso")) boneKey = "body";// rename part

                StateCollection collection = getDefaultValues(boneKey);
                BoneAnimation bone = bones.computeIfAbsent(PlayerAnimResources.getCorrectPlayerBoneName(boneKey), boneName ->
                        new BoneAnimation(boneName, new KeyframeStack<>(), new KeyframeStack<>(), new KeyframeStack<>(), new KeyframeStack<>())
                );
                addBodyPartIfExists(bone, collection, entry.getValue(), degrees, tick, easing, easingArg, turn);
            }
        }
        return bones.values().toArray(BoneAnimation[]::new);
    }

    private void addBodyPartIfExists(BoneAnimation bone, StateCollection collection, JsonElement node, boolean degrees, float tick, EasingType easing, Float easingArg, int turn) {
        JsonObject partNode = node.getAsJsonObject();
        fillKeyframeStack(bone.positionKeyFrames(), collection.pos(), bone.boneName().equals("body") ? TransformType.POSITION : null, "x", "y", "z", partNode, degrees, tick, easing, easingArg, turn);
        fillKeyframeStack(bone.rotationKeyFrames(), collection.rot(), TransformType.ROTATION, "pitch", "yaw", "roll", partNode, degrees, tick, easing, easingArg, turn);
        fillKeyframeStack(bone.scaleKeyFrames(), collection.scale(), TransformType.SCALE, "scaleX", "scaleY", "scaleZ", partNode, degrees, tick, easing, easingArg, turn);
        fillKeyframeStack(bone.bendKeyFrames(), MathHelper.ZERO, TransformType.BEND, "bend", "axis", null, partNode, degrees, tick, easing, easingArg, turn);
    }

    private void fillKeyframeStack(KeyframeStack<Keyframe> stack, Vector3f def, TransformType transformType, String x, String y, @Nullable String z, JsonObject node, boolean degrees, float tick, EasingType easing, Float easingArg, int turn) {
        addPartIfExists(stack.getLastXAxisKeyframeTime(), stack.xKeyframes(), def.x(), transformType, x, node, degrees, tick, easing, easingArg, turn);
        addPartIfExists(stack.getLastYAxisKeyframeTime(), stack.yKeyframes(), def.y(), transformType, y, node, degrees, tick, easing, easingArg, turn);
        if (z != null) addPartIfExists(stack.getLastZAxisKeyframeTime(), stack.zKeyframes(), def.z(), transformType, z, node, degrees, tick, easing, easingArg, turn);
    }

    private void addPartIfExists(float lastTick, List<Keyframe> part, float def, TransformType transformType, String name, JsonObject node, boolean degrees, float tick, EasingType easing, Float easingArg, int rotate) {
        Keyframe lastFrame = part.isEmpty() ? null : part.getLast();
        float prevTime = lastFrame != null ? lastTick : 0;
        List<List<Expression>> easingArgs = Collections.singletonList(easingArg == null ? new ObjectArrayList<>(0) : Collections.singletonList(new DoubleExpression(easingArg)));
        if (node.has(name)) {
            float value = convertPlayerAnimValue(def, node.get(name).getAsFloat(), transformType, degrees);
            List<Expression> expressions = Collections.singletonList(new DoubleExpression(value));
            part.add(new Keyframe(tick - prevTime, lastFrame == null ? expressions : lastFrame.endValue(), expressions, easing, easingArgs));
            if (transformType == TransformType.ROTATION && rotate != 0) {
                part.add(new Keyframe(tick - prevTime + 0.001F, expressions, Collections.singletonList(new DoubleExpression(value + MathHelper.PI * 2f * rotate)), easing, easingArgs));
            }
        }
    }

    private static float convertPlayerAnimValue(float def, float value, TransformType transformType, boolean degrees) {
        if (transformType != TransformType.ROTATION) value -= def;
        if (degrees && transformType == TransformType.ROTATION) value = MathHelper.toRadians(value);
        if (transformType == TransformType.POSITION) value *= 16;

        return value;
    }

    public static EasingType easingTypeFromString(String string) {
        if (string.equalsIgnoreCase("CONSTANT")) return null;
        EasingType easingType = EasingType.fromString(string.toLowerCase());
        if (easingType == EasingType.LINEAR) {
            return EasingType.fromString("ease" + string.toLowerCase());
        }
        return easingType;
    }

    public static final StateCollection EMPTY = new StateCollection(MathHelper.ZERO, MathHelper.ZERO, new Vector3f(1.0F, 1.0F, 1.0F));

    private static final Map<String, StateCollection> DEFAULT_VALUES = Map.of(
            "rightArm", new StateCollection(new Vector3f(-5, 2, 0), MathHelper.ZERO, new Vector3f(1.0F, 1.0F, 1.0F)),
            "leftArm", new StateCollection(new Vector3f(5, 2, 0), MathHelper.ZERO, new Vector3f(1.0F, 1.0F, 1.0F)),
            "leftLeg", new StateCollection(new Vector3f(1.9f, 12, 0.1f), MathHelper.ZERO, new Vector3f(1.0F, 1.0F, 1.0F)),
            "rightLeg", new StateCollection(new Vector3f(-1.9f, 12, 0.1f), MathHelper.ZERO, new Vector3f(1.0F, 1.0F, 1.0F))
    );

    public record StateCollection(Vector3f pos, Vector3f rot, Vector3f scale) {}

    public static StateCollection getDefaultValues(String bone) {
        return DEFAULT_VALUES.getOrDefault(bone, EMPTY);
    }
}
