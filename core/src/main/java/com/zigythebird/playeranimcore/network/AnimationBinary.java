package com.zigythebird.playeranimcore.network;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranimcore.animation.keyframe.Keyframe;
import com.zigythebird.playeranimcore.animation.keyframe.KeyframeStack;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.enums.AnimationFormat;
import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.loading.PlayerAnimatorLoader;
import com.zigythebird.playeranimcore.math.Vec3f;
import io.netty.buffer.ByteBuf;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.parser.ast.FloatExpression;
import team.unnamed.mocha.runtime.IsConstantExpression;
import team.unnamed.mocha.util.ExprBytesUtils;
import team.unnamed.mocha.util.network.ProtocolUtils;
import team.unnamed.mocha.util.network.VarIntUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.zigythebird.playeranimcore.molang.MolangLoader.MOCHA_ENGINE;

@SuppressWarnings("unused")
public final class AnimationBinary {
    /**
     * Version 1: Initial Release
     * Version 2: Added support for animations that don't apply the torso bend to other bones + easeBefore
     * Version 3: No change client side, but the server won't send some animations to versions lower than 3 due to the possibility of a crash.
     * Version 4: Fixed some issues with the body bone.
     * Version 5: Fixed the Y position axis on items being negated.
     * Version 6: Compact binary format - bit-packed header, presence flags for bone axes, compact keyframe encoding.
     */
    public static final int CURRENT_VERSION = 6;

    public static void write(ByteBuf buf, Animation animation) {
        AnimationBinary.write(buf, CURRENT_VERSION, animation);
    }

    public static void write(ByteBuf buf, int version, Animation animation) {
        Map<String, Object> data = animation.data().data();

        if (version >= 6) {
            writeHeaderV6(buf, animation, data);
        } else {
            boolean applyBendToOtherBones = (boolean) data.getOrDefault(ExtraAnimationData.APPLY_BEND_TO_OTHER_BONES_KEY, false);
            if (version < 3 && applyBendToOtherBones && animation.boneAnimations().containsKey("torso")
                    && !animation.boneAnimations().get("torso").bendKeyFrames().isEmpty()) {
                applyBendToOtherBones = false;
            }
            buf.writeFloat(animation.length());
            boolean shouldPlayAgain = animation.loopType().shouldPlayAgain(null, animation);
            buf.writeBoolean(shouldPlayAgain);
            if (shouldPlayAgain) {
                if (animation.loopType() == Animation.LoopType.HOLD_ON_LAST_FRAME) {
                    buf.writeBoolean(true);
                } else {
                    buf.writeBoolean(false);
                    buf.writeFloat(animation.loopType().restartFromTick(null, animation));
                }
            }
            buf.writeByte(((AnimationFormat) data.getOrDefault(ExtraAnimationData.FORMAT_KEY, AnimationFormat.GECKOLIB)).id);
            buf.writeFloat((float) data.getOrDefault(ExtraAnimationData.BEGIN_TICK_KEY, Float.NaN));
            buf.writeFloat((float) data.getOrDefault(ExtraAnimationData.END_TICK_KEY, Float.NaN));
            if (version > 1) {
                buf.writeBoolean(applyBendToOtherBones);
                buf.writeBoolean((boolean) data.getOrDefault(ExtraAnimationData.EASING_BEFORE_KEY, true));
            }
            NetworkUtils.writeUuid(buf, animation.uuid());
        }

        VarIntUtils.writeVarInt(buf, animation.boneAnimations().size());
        for (Map.Entry<String, BoneAnimation> entry : animation.boneAnimations().entrySet()) {
            ProtocolUtils.writeString(buf, entry.getKey());
            if (version >= 6) {
                writeBoneAnimationV6(buf, entry.getValue(), version);
            } else {
                writeBoneAnimation(buf, entry.getValue(), version < 4 && entry.getKey().equals("body"), version < 5 && LegacyAnimationBinary.ITEM_BONE.test(entry.getKey()));
            }
        }

        // Sounds
        VarIntUtils.writeVarInt(buf, animation.keyFrames().sounds().length);
        for (SoundKeyframeData soundKeyframe : animation.keyFrames().sounds()) {
            buf.writeFloat(soundKeyframe.getStartTick());
            ProtocolUtils.writeString(buf, soundKeyframe.getSound());
        }

        // Particles
        VarIntUtils.writeVarInt(buf, animation.keyFrames().particles().length);
        for (ParticleKeyframeData particleKeyframe : animation.keyFrames().particles()) {
            buf.writeFloat(particleKeyframe.getStartTick());
            ProtocolUtils.writeString(buf, particleKeyframe.getEffect());
            ProtocolUtils.writeString(buf, particleKeyframe.getLocator());
            ProtocolUtils.writeString(buf, particleKeyframe.script());
        }

        // Instructions
        VarIntUtils.writeVarInt(buf, animation.keyFrames().customInstructions().length);
        for (CustomInstructionKeyframeData instructionKeyframe : animation.keyFrames().customInstructions()) {
            buf.writeFloat(instructionKeyframe.getStartTick());
            ProtocolUtils.writeString(buf, instructionKeyframe.getInstructions());
        }

        NetworkUtils.writeMap(buf, animation.bones(), ProtocolUtils::writeString, NetworkUtils::writeVec3f);
        NetworkUtils.writeMap(buf, animation.parents(), ProtocolUtils::writeString, ProtocolUtils::writeString);
    }

    public static void writeBoneAnimation(ByteBuf buf, BoneAnimation bone, boolean isBody, boolean isItem) {
        writeKeyframeStack(buf, bone.rotationKeyFrames(), isBody, false, TransformType.ROTATION);
        writeKeyframeStack(buf, bone.positionKeyFrames(), isBody, isItem, TransformType.POSITION);
        writeKeyframeStack(buf, bone.scaleKeyFrames(), false, false, TransformType.SCALE);
        ProtocolUtils.writeList(buf, bone.bendKeyFrames(), AnimationBinary::writeKeyframe);
    }

    public static void writeKeyframeStack(ByteBuf buf, KeyframeStack stack, boolean isBody, boolean isItem, TransformType type) {
        ProtocolUtils.writeList(buf, isBody ? negateKeyframes(stack.xKeyframes()) : stack.xKeyframes(), AnimationBinary::writeKeyframe);
        ProtocolUtils.writeList(buf, isItem || (isBody && type == TransformType.ROTATION) ? negateKeyframes(stack.yKeyframes()) : stack.yKeyframes(), AnimationBinary::writeKeyframe);
        ProtocolUtils.writeList(buf, stack.zKeyframes(), AnimationBinary::writeKeyframe);
    }

    public static void writeKeyframe(Keyframe keyframe, ByteBuf buf) {
        buf.writeFloat(keyframe.length());
        ExprBytesUtils.writeExpressions(keyframe.endValue(), buf);
        buf.writeByte(keyframe.easingType().id);
        ProtocolUtils.writeList(buf, keyframe.easingArgs(), ExprBytesUtils::writeExpressions);
    }

    public static Animation read(ByteBuf buf) {
        return AnimationBinary.read(buf, CURRENT_VERSION);
    }

    public static Animation read(ByteBuf buf, int version) {
        ExtraAnimationData data = new ExtraAnimationData();
        float length;
        Animation.LoopType loopType;
        AnimationFormat format;

        if (version >= 6) {
            int flags = VarIntUtils.readVarInt(buf);
            boolean shouldPlayAgain = HeaderFlag.SHOULD_PLAY_AGAIN.test(flags);
            boolean isHoldOnLastFrame = HeaderFlag.HOLD_ON_LAST_FRAME.test(flags);
            format = HeaderFlag.PLAYER_ANIMATOR.test(flags) ? AnimationFormat.PLAYER_ANIMATOR : AnimationFormat.GECKOLIB;

            data.put(ExtraAnimationData.FORMAT_KEY, format);
            if (HeaderFlag.APPLY_BEND.test(flags)) data.put(ExtraAnimationData.APPLY_BEND_TO_OTHER_BONES_KEY, true);
            if (!HeaderFlag.EASE_BEFORE.test(flags)) data.put(ExtraAnimationData.EASING_BEFORE_KEY, false);

            length = buf.readFloat();

            if (shouldPlayAgain) {
                if (isHoldOnLastFrame) {
                    loopType = Animation.LoopType.HOLD_ON_LAST_FRAME;
                } else {
                    loopType = Animation.LoopType.returnToTickLoop(buf.readFloat());
                }
            } else {
                loopType = Animation.LoopType.PLAY_ONCE;
            }

            if (HeaderFlag.HAS_BEGIN_TICK.test(flags)) data.put(ExtraAnimationData.BEGIN_TICK_KEY, buf.readFloat());
            if (HeaderFlag.HAS_END_TICK.test(flags)) data.put(ExtraAnimationData.END_TICK_KEY, buf.readFloat());

            data.put(ExtraAnimationData.UUID_KEY, NetworkUtils.readUuid(buf));
        } else {
            length = buf.readFloat();
            loopType = Animation.LoopType.PLAY_ONCE;
            if (buf.readBoolean()) {
                if (buf.readBoolean()) loopType = Animation.LoopType.HOLD_ON_LAST_FRAME;
                else loopType = Animation.LoopType.returnToTickLoop(buf.readFloat());
            }
            format = AnimationFormat.fromId(buf.readByte());
            data.put(ExtraAnimationData.FORMAT_KEY, format);
            float beginTick = buf.readFloat();
            float endTick = buf.readFloat();
            if (!Float.isNaN(beginTick))
                data.put(ExtraAnimationData.BEGIN_TICK_KEY, beginTick);
            if (!Float.isNaN(endTick))
                data.put(ExtraAnimationData.END_TICK_KEY, endTick);
            if (version > 1) {
                boolean applyBendToOtherBones = buf.readBoolean();
                boolean easeBefore = buf.readBoolean();
                if (applyBendToOtherBones)
                    data.put(ExtraAnimationData.APPLY_BEND_TO_OTHER_BONES_KEY, true);
                if (!easeBefore)
                    data.put(ExtraAnimationData.EASING_BEFORE_KEY, false);
            } else data.put(ExtraAnimationData.APPLY_BEND_TO_OTHER_BONES_KEY, true);

            data.put(ExtraAnimationData.UUID_KEY, NetworkUtils.readUuid(buf));
        }

        boolean isPlayerAnimator = format == AnimationFormat.PLAYER_ANIMATOR;
        Map<String, BoneAnimation> boneAnimations;
        if (version >= 6) {
            boneAnimations = NetworkUtils.readMap(buf, ProtocolUtils::readString, buf1 -> readBoneAnimationV6(buf1, isPlayerAnimator, version));
        } else {
            boneAnimations = NetworkUtils.readMap(buf, ProtocolUtils::readString, buf1 -> readBoneAnimation(buf1, isPlayerAnimator));

            if (version < 4 && boneAnimations.containsKey("body")) {
                BoneAnimation body = boneAnimations.get("body");
                body.positionKeyFrames().xKeyframes().replaceAll(AnimationBinary::negateKeyframeExpressions);
                body.rotationKeyFrames().xKeyframes().replaceAll(AnimationBinary::negateKeyframeExpressions);
                body.rotationKeyFrames().yKeyframes().replaceAll(AnimationBinary::negateKeyframeExpressions);
            }

            if (version < 5) {
                if (boneAnimations.containsKey("right_item"))
                    boneAnimations.get("right_item").positionKeyFrames().yKeyframes().replaceAll(AnimationBinary::negateKeyframeExpressions);
                if (boneAnimations.containsKey("left_item"))
                    boneAnimations.get("left_item").positionKeyFrames().yKeyframes().replaceAll(AnimationBinary::negateKeyframeExpressions);
            }
        }

        // Sounds
        int soundCount = VarIntUtils.readVarInt(buf);
        SoundKeyframeData[] sounds = new SoundKeyframeData[soundCount];
        for (int i = 0; i < soundCount; i++) {
            float startTick = buf.readFloat();
            String sound = ProtocolUtils.readString(buf);
            sounds[i] = new SoundKeyframeData(startTick, sound);
        }

        // Particles
        int particleCount = VarIntUtils.readVarInt(buf);
        ParticleKeyframeData[] particles = new ParticleKeyframeData[particleCount];
        for (int i = 0; i < particleCount; i++) {
            float startTick = buf.readFloat();
            String effect = ProtocolUtils.readString(buf);
            String locator = ProtocolUtils.readString(buf);
            String script = ProtocolUtils.readString(buf);
            particles[i] = new ParticleKeyframeData(startTick, effect, locator, script);
        }

        // Instructions
        int customInstructionCount = VarIntUtils.readVarInt(buf);
        CustomInstructionKeyframeData[] customInstructions = new CustomInstructionKeyframeData[customInstructionCount];
        for (int i = 0; i < customInstructionCount; i++) {
            float startTick = buf.readFloat();
            String instructions = ProtocolUtils.readString(buf);
            customInstructions[i] = new CustomInstructionKeyframeData(startTick, instructions);
        }
        Animation.Keyframes keyFrames = new Animation.Keyframes(sounds, particles, customInstructions);

        Map<String, Vec3f> pivotBones = NetworkUtils.readMap(buf, ProtocolUtils::readString, NetworkUtils::readVec3f);
        Map<String, String> parents = NetworkUtils.readMap(buf, ProtocolUtils::readString, ProtocolUtils::readString);

        return new Animation(data, length, loopType, boneAnimations, keyFrames, pivotBones, parents);
    }

    public static BoneAnimation readBoneAnimation(ByteBuf buf, boolean shouldStartFromDefault) {
        KeyframeStack rotationKeyFrames = readKeyframeStack(buf, shouldStartFromDefault, false);
        KeyframeStack positionKeyFrames = readKeyframeStack(buf, shouldStartFromDefault, false);
        KeyframeStack scaleKeyFrames = readKeyframeStack(buf, shouldStartFromDefault, true);
        List<Keyframe> bendKeyFrames = readKeyframeList(buf, shouldStartFromDefault, false);

        return new BoneAnimation(rotationKeyFrames, positionKeyFrames, scaleKeyFrames, bendKeyFrames);
    }

    public static KeyframeStack readKeyframeStack(ByteBuf buf, boolean shouldStartFromDefault, boolean isScale) {
        List<Keyframe> xKeyframes = readKeyframeList(buf, shouldStartFromDefault, isScale);
        List<Keyframe> yKeyframes = readKeyframeList(buf, shouldStartFromDefault, isScale);
        List<Keyframe> zKeyframes = readKeyframeList(buf, shouldStartFromDefault, isScale);

        return new KeyframeStack(xKeyframes, yKeyframes, zKeyframes);
    }

    public static List<Keyframe> readKeyframeList(ByteBuf buf, boolean shouldStartFromDefault, boolean isScale) {
        int count = VarIntUtils.readVarInt(buf);
        List<Keyframe> list = new ArrayList<>(count);

        for (int i = 0; i < count; ++i) {
            float length = buf.readFloat();

            List<Expression> endValue = ExprBytesUtils.readExpressions(buf);
            List<Expression> startValue = list.isEmpty() ? (shouldStartFromDefault ?(isScale ? PlayerAnimatorLoader.ONE : PlayerAnimatorLoader.ZERO) : endValue) : list.getLast().endValue();
            EasingType easingType = EasingType.fromId(buf.readByte());
            List<List<Expression>> easingArgs = ProtocolUtils.readList(buf, ExprBytesUtils::readExpressions);

            list.add(new Keyframe(length, startValue, endValue, easingType, easingArgs));
        }

        return list;
    }

    private static void writeHeaderV6(ByteBuf buf, Animation animation, Map<String, Object> data) {
        boolean shouldPlayAgain = animation.loopType().shouldPlayAgain(null, animation);
        boolean isHoldOnLastFrame = animation.loopType() == Animation.LoopType.HOLD_ON_LAST_FRAME;
        AnimationFormat format = (AnimationFormat) data.getOrDefault(ExtraAnimationData.FORMAT_KEY, AnimationFormat.GECKOLIB);
        boolean applyBend = (boolean) data.getOrDefault(ExtraAnimationData.APPLY_BEND_TO_OTHER_BONES_KEY, false);
        boolean easeBefore = (boolean) data.getOrDefault(ExtraAnimationData.EASING_BEFORE_KEY, true);
        float beginTick = (float) data.getOrDefault(ExtraAnimationData.BEGIN_TICK_KEY, Float.NaN);
        float endTick = (float) data.getOrDefault(ExtraAnimationData.END_TICK_KEY, Float.NaN);

        int flags = 0;
        flags = HeaderFlag.SHOULD_PLAY_AGAIN.set(flags, shouldPlayAgain);
        flags = HeaderFlag.HOLD_ON_LAST_FRAME.set(flags, isHoldOnLastFrame);
        flags = HeaderFlag.PLAYER_ANIMATOR.set(flags, format == AnimationFormat.PLAYER_ANIMATOR);
        flags = HeaderFlag.APPLY_BEND.set(flags, applyBend);
        flags = HeaderFlag.EASE_BEFORE.set(flags, easeBefore);
        flags = HeaderFlag.HAS_BEGIN_TICK.set(flags, !Float.isNaN(beginTick));
        flags = HeaderFlag.HAS_END_TICK.set(flags, !Float.isNaN(endTick));

        VarIntUtils.writeVarInt(buf, flags);
        buf.writeFloat(animation.length());
        if (shouldPlayAgain && !isHoldOnLastFrame) {
            buf.writeFloat(animation.loopType().restartFromTick(null, animation));
        }
        if (HeaderFlag.HAS_BEGIN_TICK.test(flags)) buf.writeFloat(beginTick);
        if (HeaderFlag.HAS_END_TICK.test(flags)) buf.writeFloat(endTick);
        NetworkUtils.writeUuid(buf, animation.uuid());
    }

    private static void writeBoneAnimationV6(ByteBuf buf, BoneAnimation bone, int version) {
        int presenceFlags = 0;
        for (BoneChannel ch : BoneChannel.VALUES) {
            if (!ch.getKeyframes(bone).isEmpty()) {
                presenceFlags |= ch.mask;
            }
        }
        VarIntUtils.writeVarInt(buf, presenceFlags);
        for (BoneChannel ch : BoneChannel.VALUES) {
            if ((presenceFlags & ch.mask) != 0) {
                writeKeyframeListV6(buf, ch.getKeyframes(bone), version);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static BoneAnimation readBoneAnimationV6(ByteBuf buf, boolean shouldStartFromDefault, int version) {
        int presenceFlags = VarIntUtils.readVarInt(buf);
        List<Keyframe>[] lists = new List[BoneChannel.VALUES.length];
        for (BoneChannel ch : BoneChannel.VALUES) {
            lists[ch.ordinal()] = (presenceFlags & ch.mask) != 0
                    ? readKeyframeListV6(buf, shouldStartFromDefault, ch.isScale, version)
                    : new ArrayList<>(0);
        }
        return new BoneAnimation(
                new KeyframeStack(lists[0], lists[1], lists[2]),
                new KeyframeStack(lists[3], lists[4], lists[5]),
                new KeyframeStack(lists[6], lists[7], lists[8]),
                lists[9]
        );
    }

    private static void writeKeyframeListV6(ByteBuf buf, List<Keyframe> keyframes, int version) {
        VarIntUtils.writeVarInt(buf, keyframes.size());
        for (Keyframe keyframe : keyframes) {
            writeKeyframeV6(keyframe, buf, version);
        }
    }

    private static void writeKeyframeV6(Keyframe keyframe, ByteBuf buf, int version) {
        List<Expression> endValue = keyframe.endValue();
        boolean isConstant = endValue.size() == 1 && IsConstantExpression.test(endValue.getFirst());
        boolean hasEasingArgs = false;
        for (List<Expression> inner : keyframe.easingArgs()) {
            if (!inner.isEmpty()) {
                hasEasingArgs = true;
                break;
            }
        }

        int flags = 0;
        if (isConstant) flags |= KeyframeFlag.IS_CONSTANT.mask;
        if (hasEasingArgs) flags |= KeyframeFlag.HAS_EASING_ARGS.mask;
        if (keyframe.length() == 0.0f) flags |= KeyframeFlag.LENGTH_ZERO.mask;
        else if (keyframe.length() == 1.0f) flags |= KeyframeFlag.LENGTH_ONE.mask;
        VarIntUtils.writeVarInt(buf, KeyframeFlag.pack(keyframe.easingType().id, flags, version));

        if (isConstant) {
            buf.writeFloat(MOCHA_ENGINE.eval(endValue));
        } else {
            ExprBytesUtils.writeExpressions(endValue, buf);
        }

        if ((flags & (KeyframeFlag.LENGTH_ZERO.mask | KeyframeFlag.LENGTH_ONE.mask)) == 0) {
            buf.writeFloat(keyframe.length());
        }

        if (hasEasingArgs) {
            ProtocolUtils.writeList(buf, keyframe.easingArgs(), ExprBytesUtils::writeExpressions);
        }
    }

    private static List<Keyframe> readKeyframeListV6(ByteBuf buf, boolean shouldStartFromDefault, boolean isScale, int version) {
        int count = VarIntUtils.readVarInt(buf);
        List<Keyframe> list = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            int combined = VarIntUtils.readVarInt(buf);
            int easingId = KeyframeFlag.unpackEasing(combined, version);
            int flags = KeyframeFlag.unpackFlags(combined, version);
            boolean isConstant = (flags & KeyframeFlag.IS_CONSTANT.mask) != 0;
            boolean hasEasingArgs = (flags & KeyframeFlag.HAS_EASING_ARGS.mask) != 0;

            List<Expression> endValue;
            if (isConstant) {
                endValue = List.of(FloatExpression.of(buf.readFloat()));
            } else {
                endValue = ExprBytesUtils.readExpressions(buf);
            }

            float length;
            if ((flags & KeyframeFlag.LENGTH_ZERO.mask) != 0) length = 0.0f;
            else if ((flags & KeyframeFlag.LENGTH_ONE.mask) != 0) length = 1.0f;
            else length = buf.readFloat();

            List<Expression> startValue = list.isEmpty()
                    ? (shouldStartFromDefault ? (isScale ? PlayerAnimatorLoader.ONE : PlayerAnimatorLoader.ZERO) : endValue)
                    : list.getLast().endValue();
            EasingType easingType = EasingType.fromId((byte) easingId);
            List<List<Expression>> easingArgs;
            if (hasEasingArgs) {
                easingArgs = ProtocolUtils.readList(buf, ExprBytesUtils::readExpressions);
            } else if (shouldStartFromDefault && i > 0) {
                easingArgs = Collections.singletonList(new ArrayList<>(0));
            } else {
                easingArgs = new ArrayList<>(0);
            }

            list.add(new Keyframe(length, startValue, endValue, easingType, easingArgs));
        }

        return list;
    }

    private static List<Keyframe> negateKeyframes(List<Keyframe> keyframes) {
        keyframes = new ArrayList<>(keyframes);
        keyframes.replaceAll(AnimationBinary::negateKeyframeExpressions);
        return keyframes;
    }

    private static Keyframe negateKeyframeExpressions(Keyframe keyframe) {
        keyframe = new Keyframe(keyframe.length(), new ArrayList<>(keyframe.startValue()), new ArrayList<>(keyframe.endValue()), keyframe.easingType(), keyframe.easingArgs());
        negateKeyframeExpressions(keyframe.startValue());
        negateKeyframeExpressions(keyframe.endValue());
        return keyframe;
    }

    private static void negateKeyframeExpressions(List<Expression> expressions) {
        if (expressions.size() == 1 && IsConstantExpression.test(expressions.getFirst())) {
            expressions.set(0, FloatExpression.of(-MOCHA_ENGINE.eval(expressions)));
        }
    }
}
