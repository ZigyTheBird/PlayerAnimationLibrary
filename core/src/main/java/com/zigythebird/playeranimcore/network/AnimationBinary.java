package com.zigythebird.playeranimcore.network;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranimcore.animation.keyframe.Keyframe;
import com.zigythebird.playeranimcore.animation.keyframe.KeyframeStack;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranimcore.enums.AnimationFormat;
import com.zigythebird.playeranimcore.loading.PlayerAnimatorLoader;
import com.zigythebird.playeranimcore.math.Vec3f;
import io.netty.buffer.ByteBuf;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.util.ExprBytesUtils;
import team.unnamed.mocha.util.network.ProtocolUtils;
import team.unnamed.mocha.util.network.VarIntUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public final class AnimationBinary {
    /**
     * Version 1: Initial Release
     * Version 2: Added support for animations that don't apply the torso bend to other bones + easeBefore
     */
    public static final int CURRENT_VERSION = 2;

    public static void write(ByteBuf buf, Animation animation) {
        AnimationBinary.write(buf, CURRENT_VERSION, animation);
    }

    public static void write(ByteBuf buf, int version, Animation animation) {
        buf.writeFloat(animation.length());
        boolean shouldPlayAgain = animation.loopType().shouldPlayAgain(animation);
        buf.writeBoolean(shouldPlayAgain);
        if (shouldPlayAgain) {
            if (animation.loopType() == Animation.LoopType.HOLD_ON_LAST_FRAME) {
                buf.writeBoolean(true);
            } else {
                buf.writeBoolean(false);
                buf.writeFloat(animation.loopType().restartFromTick(animation));
            }
        }
        Map<String, Object> data = animation.data().data();
        buf.writeByte(((AnimationFormat)data.getOrDefault(ExtraAnimationData.FORMAT_KEY, AnimationFormat.GECKOLIB)).id);
        buf.writeFloat((float) data.getOrDefault(ExtraAnimationData.BEGIN_TICK_KEY, Float.NaN));
        buf.writeFloat((float) data.getOrDefault(ExtraAnimationData.END_TICK_KEY, Float.NaN));
        if (version > 1) {
            buf.writeBoolean((boolean) data.getOrDefault(ExtraAnimationData.APPLY_BEND_TO_OTHER_BONES_KEY, false));
            buf.writeBoolean((boolean) data.getOrDefault(ExtraAnimationData.EASING_BEFORE_KEY, true));
        }
        NetworkUtils.writeUuid(buf, animation.uuid()); // required by emotecraft to stop animations
        NetworkUtils.writeMap(buf, animation.boneAnimations(), ProtocolUtils::writeString, AnimationBinary::writeBoneAnimation);

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

    public static void writeBoneAnimation(ByteBuf buf, BoneAnimation bone) {
        writeKeyframeStack(buf, bone.rotationKeyFrames());
        writeKeyframeStack(buf, bone.positionKeyFrames());
        writeKeyframeStack(buf, bone.scaleKeyFrames());
        ProtocolUtils.writeList(buf, bone.bendKeyFrames(), AnimationBinary::writeKeyframe);
    }

    public static void writeKeyframeStack(ByteBuf buf, KeyframeStack stack) {
        ProtocolUtils.writeList(buf, stack.xKeyframes(), AnimationBinary::writeKeyframe);
        ProtocolUtils.writeList(buf, stack.yKeyframes(), AnimationBinary::writeKeyframe);
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
        float length = buf.readFloat();
        Animation.LoopType loopType = Animation.LoopType.PLAY_ONCE;
        if (buf.readBoolean()) {
            if (buf.readBoolean()) loopType = Animation.LoopType.HOLD_ON_LAST_FRAME;
            else loopType = Animation.LoopType.returnToTickLoop(buf.readFloat());
        }
        ExtraAnimationData data = new ExtraAnimationData();
        AnimationFormat format = AnimationFormat.fromId(buf.readByte());
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
        }

        data.put(ExtraAnimationData.UUID_KEY, NetworkUtils.readUuid(buf)); // required by emotecraft to stop animations
        Map<String, BoneAnimation> boneAnimations = NetworkUtils.readMap(buf, ProtocolUtils::readString, buf1 -> readBoneAnimation(buf1, format == AnimationFormat.PLAYER_ANIMATOR));

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

    public static BoneAnimation readBoneAnimation(ByteBuf buf, boolean shouldStartFromZero) {
        KeyframeStack rotationKeyFrames = readKeyframeStack(buf, shouldStartFromZero);
        KeyframeStack positionKeyFrames = readKeyframeStack(buf, shouldStartFromZero);
        KeyframeStack scaleKeyFrames = readKeyframeStack(buf, shouldStartFromZero);
        List<Keyframe> bendKeyFrames = readKeyframeList(buf, shouldStartFromZero);

        return new BoneAnimation(rotationKeyFrames, positionKeyFrames, scaleKeyFrames, bendKeyFrames);
    }

    public static KeyframeStack readKeyframeStack(ByteBuf buf, boolean shouldStartFromZero) {
        List<Keyframe> xKeyframes = readKeyframeList(buf, shouldStartFromZero);
        List<Keyframe> yKeyframes = readKeyframeList(buf, shouldStartFromZero);
        List<Keyframe> zKeyframes = readKeyframeList(buf, shouldStartFromZero);

        return new KeyframeStack(xKeyframes, yKeyframes, zKeyframes);
    }

    public static List<Keyframe> readKeyframeList(ByteBuf buf, boolean shouldStartFromZero) {
        int count = VarIntUtils.readVarInt(buf);
        List<Keyframe> list = new ArrayList<>(count);

        for (int i = 0; i < count; ++i) {
            float length = buf.readFloat();

            List<Expression> endValue = ExprBytesUtils.readExpressions(buf);
            List<Expression> startValue = list.isEmpty() ? (shouldStartFromZero ? PlayerAnimatorLoader.ZERO : endValue) : list.getLast().endValue();
            EasingType easingType = EasingType.fromId(buf.readByte());
            List<List<Expression>> easingArgs = ProtocolUtils.readList(buf, ExprBytesUtils::readExpressions);

            list.add(new Keyframe(length, startValue, endValue, easingType, easingArgs));
        }

        return list;
    }
}
