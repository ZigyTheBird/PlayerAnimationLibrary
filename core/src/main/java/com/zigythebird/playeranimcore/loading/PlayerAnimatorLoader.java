package com.zigythebird.playeranimcore.loading;

import com.google.gson.*;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.EasingType;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranimcore.animation.keyframe.Keyframe;
import com.zigythebird.playeranimcore.animation.keyframe.KeyframeStack;
import com.zigythebird.playeranimcore.enums.AnimationFormat;
import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.math.MathHelper;
import com.zigythebird.playeranimcore.math.Vec3f;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.parser.ast.FloatExpression;

import java.lang.reflect.Type;
import java.util.*;

import static com.zigythebird.playeranimcore.loading.UniversalAnimLoader.NO_KEYFRAMES;

public class PlayerAnimatorLoader implements JsonDeserializer<Animation> {
    private final static int modVersion = 3;

    public static final List<Expression> ZERO = Collections.singletonList(FloatExpression.ZERO);
    public static final List<Expression> ONE = Collections.singletonList(FloatExpression.ONE);

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Animation.class, new PlayerAnimatorLoader())
            .create();

    protected PlayerAnimatorLoader() {}
    
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
        if (loopType == Animation.LoopType.PLAY_ONCE)
            endTick = stopTick <= endTick ? endTick + 3 : stopTick; // https://github.com/KosmX/minecraftPlayerAnimator/blob/1.21/coreLib/src/main/java/dev/kosmx/playerAnim/core/data/KeyframeAnimation.java#L80

        boolean degrees = !node.has("degrees") || node.get("degrees").getAsBoolean();
        List<BoneAnimation> bones = moveDeserializer(node.getAsJsonArray("moves").asList(), degrees, version);

        //Replaces all keyframes with their easing set to null with two keyframes to get a constant easing/step bedrock keyframe effect
        //Also shifts all easings to the right by one if easeBeforeKeyframe is false
        //If easings are shifted in order for the last keyframe's easing to not be ignored a 0.001 tick long keyframe gets added at the end with that easing
        //The reason why the last easing can't be ignored is because it's used by endTick lerping
        for (BoneAnimation boneAnimation : bones) {
            correctEasings(boneAnimation.positionKeyFrames(), easeBeforeKeyframe);
            correctEasings(boneAnimation.rotationKeyFrames(), easeBeforeKeyframe);
            correctEasings(boneAnimation.scaleKeyFrames(), easeBeforeKeyframe);
            correctEasings(boneAnimation.bendKeyFrames(), easeBeforeKeyframe);
        }

        return new Animation(extra, endTick, loopType, bones, NO_KEYFRAMES, new HashMap<>(), new HashMap<>());
    }

    public static void correctEasings(KeyframeStack keyframeStack, boolean easeBefore) {
        correctEasings(keyframeStack.xKeyframes(), easeBefore);
        correctEasings(keyframeStack.yKeyframes(), easeBefore);
        correctEasings(keyframeStack.zKeyframes(), easeBefore);
    }

    private static void correctEasings(List<Keyframe> list, boolean easeBefore) {
        if (!easeBefore) {
            EasingType previousEasing = EasingType.EASE_IN_SINE;
            for (int i=0;i<list.size();i++) {
                Keyframe keyframe = list.get(i);
                list.set(i, new Keyframe(keyframe.length(), keyframe.startValue(), keyframe.endValue(), previousEasing, keyframe.easingArgs()));
                if (i == list.size()-1 && previousEasing != keyframe.easingType()) {
                    //If the final easing is constant, it defaults to linear instead
                    //If you don't want your anim to have endTick lerp then just set stopTick to endTick + 1...
                    list.add(new Keyframe(0.001F, keyframe.endValue(), keyframe.endValue(), keyframe.easingType() == null ? EasingType.LINEAR : keyframe.easingType(), keyframe.easingArgs()));
                }
                previousEasing = keyframe.easingType();
            }
        }
        List<Integer> constantIndexes = new ArrayList<>();
        for (Keyframe keyframe : list) {
            if (keyframe.easingType() == null) constantIndexes.add(list.indexOf(keyframe));
        }
        for (Integer index : constantIndexes) {
            Keyframe keyframe = list.get(index);
            list.set(index, new Keyframe(0.001F, keyframe.endValue(), keyframe.endValue(), EasingType.LINEAR, keyframe.easingArgs()));
            list.add(index, new Keyframe(keyframe.length() - 0.001F, keyframe.startValue(), keyframe.startValue(), EasingType.LINEAR, keyframe.easingArgs()));
        }
    }

    private List<BoneAnimation> moveDeserializer(List<JsonElement> node, boolean degrees, int version) {
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
            int turn = obj.has("turn") ? obj.get("turn").getAsInt() : 0;
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()){
                if(entry.getKey().equals("tick") || entry.getKey().equals("comment") || entry.getKey().equals("easing") || entry.getKey().equals("turn")) {
                    continue;
                }

                String boneKey = entry.getKey();
                if(version < 3 && boneKey.equals("torso")) boneKey = "body";// rename part

                StateCollection collection = getDefaultValues(boneKey);
                BoneAnimation bone = bones.computeIfAbsent(UniversalAnimLoader.getCorrectPlayerBoneName(boneKey), boneName ->
                        new BoneAnimation(boneName, new KeyframeStack(), new KeyframeStack(), new KeyframeStack(), new KeyframeStack())
                );
                addBodyPartIfExists(bone, collection, entry.getValue(), degrees, tick, easing, turn);
                resolveMissingKeyframes(bone.positionKeyFrames(), false);
                resolveMissingKeyframes(bone.rotationKeyFrames(), false);
                resolveMissingKeyframes(bone.bendKeyFrames(), false);
                resolveMissingKeyframes(bone.scaleKeyFrames(), true);
            }
        }
        return new ArrayList<>(bones.values());
    }

    private void resolveMissingKeyframes(KeyframeStack stack, boolean isScale) {
        if (!stack.xKeyframes().isEmpty() || !stack.yKeyframes().isEmpty() || !stack.zKeyframes().isEmpty()) {
            resolveMissingKeyframes(stack.xKeyframes(), isScale);
            resolveMissingKeyframes(stack.yKeyframes(), isScale);
            resolveMissingKeyframes(stack.zKeyframes(), isScale);
        }
    }

    private void resolveMissingKeyframes(List<Keyframe> keyframes, boolean isScale) {
        if (keyframes.isEmpty()) {
            keyframes.add(new Keyframe(0, isScale ? ONE : ZERO, isScale ? ONE : ZERO, EasingType.LINEAR, Collections.singletonList(new ObjectArrayList<>(0))));
        }
    }

    private void addBodyPartIfExists(BoneAnimation bone, StateCollection collection, JsonElement node, boolean degrees, float tick, EasingType easing, int turn) {
        JsonObject partNode = node.getAsJsonObject();
        fillKeyframeStack(bone.positionKeyFrames(), collection.pos(), bone.boneName().equals("body") ? TransformType.POSITION : null, "x", "y", "z", partNode, degrees, tick, easing, turn);
        fillKeyframeStack(bone.rotationKeyFrames(), collection.rot(), TransformType.ROTATION, "pitch", "yaw", "roll", partNode, degrees, tick, easing, turn);
        fillKeyframeStack(bone.scaleKeyFrames(), collection.scale(), TransformType.SCALE, "scaleX", "scaleY", "scaleZ", partNode, degrees, tick, easing, turn);
        fillKeyframeStack(bone.bendKeyFrames(), Vec3f.ZERO, TransformType.BEND, "axis", "bend", null, partNode, degrees, tick, easing, turn);
    }

    private void fillKeyframeStack(KeyframeStack stack, Vec3f def, TransformType transformType, String x, String y, @Nullable String z, JsonObject node, boolean degrees, float tick, EasingType easing, int turn) {
        addPartIfExists(stack.getLastXAxisKeyframeTime(), stack.xKeyframes(), def.x(), transformType, x, node, degrees, tick, easing, turn);
        addPartIfExists(stack.getLastYAxisKeyframeTime(), stack.yKeyframes(), def.y(), transformType, y, node, degrees, tick, easing, turn);
        if (z != null) addPartIfExists(stack.getLastZAxisKeyframeTime(), stack.zKeyframes(), def.z(), transformType, z, node, degrees, tick, easing, turn);
    }

    private void addPartIfExists(float lastTick, List<Keyframe> part, float def, TransformType transformType, String name, JsonObject node, boolean degrees, float tick, EasingType easing, int rotate) {
        Keyframe lastFrame = part.isEmpty() ? null : part.getLast();
        float prevTime = lastFrame != null ? lastTick : 0;
        if (node.has(name)) {
            float value = convertPlayerAnimValue(def, node.get(name).getAsFloat(), transformType, degrees, name.equals("y") && transformType == null);
            List<Expression> expressions = Collections.singletonList(FloatExpression.of(value));
            part.add(new Keyframe(tick - prevTime, lastFrame == null ? expressions : lastFrame.endValue(), expressions, easing, Collections.singletonList(new ObjectArrayList<>(0))));
            if (transformType == TransformType.ROTATION && rotate != 0) {
                part.add(new Keyframe(tick - prevTime + 0.001F, expressions, Collections.singletonList(FloatExpression.of(value + MathHelper.PI * 2f * rotate)), easing, Collections.singletonList(new ObjectArrayList<>(0))));
            }
        }
    }

    private static float convertPlayerAnimValue(float def, float value, TransformType transformType, boolean degrees, boolean shouldNegate) {
        if (transformType != TransformType.ROTATION && transformType != TransformType.SCALE) value -= def;
        if (shouldNegate) value *= -1;
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

    public static final StateCollection EMPTY = new StateCollection(Vec3f.ZERO, Vec3f.ZERO, Vec3f.ONE);

    private static final Map<String, StateCollection> DEFAULT_VALUES = Map.of(
            "rightArm", new StateCollection(new Vec3f(-5, 2, 0), Vec3f.ZERO, Vec3f.ONE),
            "leftArm", new StateCollection(new Vec3f(5, 2, 0), Vec3f.ZERO, Vec3f.ONE),
            "leftLeg", new StateCollection(new Vec3f(1.9f, 12, 0.1f), Vec3f.ZERO, Vec3f.ONE),
            "rightLeg", new StateCollection(new Vec3f(-1.9f, 12, 0.1f), Vec3f.ZERO, Vec3f.ONE)
    );

    public record StateCollection(Vec3f pos, Vec3f rot, Vec3f scale) {}

    public static StateCollection getDefaultValues(String bone) {
        return DEFAULT_VALUES.getOrDefault(bone, EMPTY);
    }
}
