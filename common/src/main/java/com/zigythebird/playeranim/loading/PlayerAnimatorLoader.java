package com.zigythebird.playeranim.loading;

import static com.zigythebird.playeranim.cache.PlayerAnimCache.NO_KEYFRAMES;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.zigythebird.playeranim.animation.Animation;
import com.zigythebird.playeranim.animation.ExtraAnimationData;
import com.zigythebird.playeranim.animation.EasingType;
import com.zigythebird.playeranim.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranim.animation.keyframe.Keyframe;
import com.zigythebird.playeranim.animation.keyframe.KeyframeStack;
import com.zigythebird.playeranim.cache.PlayerAnimCache;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.parser.ast.DoubleExpression;
import team.unnamed.mocha.parser.ast.Expression;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        if (modVersion < version){
            throw new JsonParseException(extra.name() + " is version " + version + ". Player Animation library can only process version " + modVersion + ".");
        }

        return emoteDeserializer(extra, node.getAsJsonObject("emote"), version);
    }

    private Animation emoteDeserializer(ExtraAnimationData extra, JsonObject node, int version) throws JsonParseException{
        /*int beginTick = 0; TODO
        if(node.has("beginTick")) {
            beginTick = node.get("beginTick").getAsInt();
        }*/
        double endTick = node.get("endTick").getAsDouble();
        if(endTick <= 0) throw new JsonParseException("endTick must be bigger than 0");
        Animation.LoopType loopType = Animation.LoopType.PLAY_ONCE;
        if(node.has("isLoop") && node.has("returnTick")) {
            boolean isLooped = node.get("isLoop").getAsBoolean();
            int returnTick = node.get("returnTick").getAsInt();
            if (isLooped) {
                if (returnTick > endTick || returnTick < 0) {
                    throw new JsonParseException("return tick have to be smaller than endTick and not smaller than 0");
                }
                loopType = Animation.LoopType.returnToTickLoop(returnTick);
            }
        }

        if (node.has("nsfw")) extra.data().put(
                "nsfw", node.get("nsfw").getAsBoolean()
        );
        if (node.has("easeBeforeKeyframe")) extra.data().put(
                "easeBeforeKeyframe", node.get("easeBeforeKeyframe").getAsBoolean()
        );

        endTick = node.has("stopTick") ? node.get("stopTick").getAsInt() : endTick; // TODO
        boolean degrees = ! node.has("degrees") || node.get("degrees").getAsBoolean();
        BoneAnimation[] bones = moveDeserializer(node.getAsJsonArray("moves").asList(), degrees, version);

        return new Animation(extra, endTick, loopType, bones, NO_KEYFRAMES, new HashMap<>(), new HashMap<>());
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
            int tick = obj.get("tick").getAsInt();
            EasingType easing = easingTypeFromString(obj.has("easing") ? obj.get("easing").getAsString() : "linear");
            Double easingArg = null;
            try {
                if (obj.has("easingArg")) {
                    easingArg = obj.get("easingArg").getAsDouble();
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
                BoneAnimation bone = bones.computeIfAbsent(PlayerAnimCache.getCorrectPlayerBoneName(boneKey),boneName ->
                        new BoneAnimation(boneName, new KeyframeStack<>(), new KeyframeStack<>(), new KeyframeStack<>(), new KeyframeStack<>())
                );
                addBodyPartIfExists(bone, collection, entry.getValue(), degrees, tick, easing, easingArg, turn);
            }
        }
        return bones.values().toArray(BoneAnimation[]::new);
    }

    private void addBodyPartIfExists(BoneAnimation bone, StateCollection collection, JsonElement node, boolean degrees, int tick, EasingType easing, Double easingArg, int turn) {
        JsonObject partNode = node.getAsJsonObject();
        fillKeyframeStack(bone.positionKeyFrames(), collection.pos(), false, "x", "y", "z", partNode, degrees, tick, easing, easingArg, turn);
        fillKeyframeStack(bone.rotationKeyFrames(), collection.rot(), true, "pitch", "yaw", "roll", partNode, degrees, tick, easing, easingArg, turn);
        fillKeyframeStack(bone.scaleKeyFrames(), collection.scale(), false, "scaleX", "scaleY", "scaleZ", partNode, degrees, tick, easing, easingArg, turn);
        fillKeyframeStack(bone.bendKeyFrames(), Vec3.ZERO, true, "bend", "axis", null, partNode, degrees, tick, easing, easingArg, turn);
    }

    private void fillKeyframeStack(KeyframeStack<Keyframe> stack, Vec3 def, boolean isAngle, String x, String y, @Nullable String z, JsonObject node, boolean degrees, int tick, EasingType easing, Double easingArg, int turn) {
        addPartIfExists(stack.getLastXAxisKeyframeTime(), stack.xKeyframes(), def.x(), isAngle, x, node, degrees, tick, easing, easingArg, turn);
        addPartIfExists(stack.getLastYAxisKeyframeTime(), stack.yKeyframes(), def.y(), isAngle, y, node, degrees, tick, easing, easingArg, turn);
        if (z != null) addPartIfExists(stack.getLastZAxisKeyframeTime(), stack.zKeyframes(), def.z(), isAngle, z, node, degrees, tick, easing, easingArg, turn);
    }

    private void addPartIfExists(double lastTick, List<Keyframe> part, double def, boolean isAngle, String name, JsonObject node, boolean degrees, int tick, EasingType easing, Double easingArg, int rotate) {
        Keyframe lastFrame = part.isEmpty() ? null : part.getLast();
        double prevTime = lastFrame != null ? lastTick : 0;
        List<List<Expression>> easingArgs = Collections.singletonList(easingArg == null ? new ObjectArrayList<>(0) : Collections.singletonList(new DoubleExpression(easingArg)));
        if (node.has(name)) {
            double value = convertPlayerAnimValue(def, node.get(name).getAsDouble(), isAngle, degrees);
            List<Expression> expressions = Collections.singletonList(new DoubleExpression(value));
            part.add(new Keyframe(tick - prevTime, lastFrame == null ? expressions : lastFrame.endValue(), expressions, easing, easingArgs));
            if (isAngle && rotate != 0) {
                part.add(new Keyframe(tick - prevTime, expressions, Collections.singletonList(new DoubleExpression(value + Math.PI * 2d * rotate)), easing, easingArgs));
            }
        } /*else {
            List<Expression> expressions = Collections.singletonList(name.contains("scale") ? DoubleExpression.ONE : DoubleExpression.ZERO);
            part.add(new Keyframe(tick, tick - prevTime, lastFrame == null ? expressions : lastFrame.endValue(), expressions, easingTypeFromString(easing), easingArgs));
        }*/
    }

    private static double convertPlayerAnimValue(double def, double value, boolean isAngle, boolean degrees) {
        if (!isAngle) value -= def;
        if (degrees && isAngle) value = 0.01745329251f;

        return value;
    }

    public static EasingType easingTypeFromString(String string) {
        EasingType easingType = EasingType.fromString(string.toLowerCase());
        if (easingType == EasingType.LINEAR) {
            return EasingType.fromString("ease" + string.toLowerCase());
        }
        return easingType;
    }

    public static final StateCollection EMPTY = new StateCollection(Vec3.ZERO, Vec3.ZERO, new Vec3(1.0F, 1.0F, 1.0F));

    private static final Map<String, StateCollection> DEFAULT_VALUES = Map.of(
            "rightArm", new StateCollection(new Vec3(-5, 2, 0), Vec3.ZERO, new Vec3(1.0F, 1.0F, 1.0F)),
            "leftArm", new StateCollection(new Vec3(5, 2, 0), Vec3.ZERO, new Vec3(1.0F, 1.0F, 1.0F)),
            "leftLeg", new StateCollection(new Vec3(1.9f, 12, 0.1f), Vec3.ZERO, new Vec3(1.0F, 1.0F, 1.0F)),
            "rightLeg", new StateCollection(new Vec3(-1.9f, 12, 0.1f), Vec3.ZERO, new Vec3(1.0F, 1.0F, 1.0F))
    );

    public record StateCollection(Vec3 pos, Vec3 rot, Vec3 scale) {}

    public static StateCollection getDefaultValues(String bone) {
        return DEFAULT_VALUES.getOrDefault(bone, EMPTY);
    }
}
