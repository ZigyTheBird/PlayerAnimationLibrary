package com.zigythebird.playeranimcore.network;

import com.zigythebird.playeranimcore.PlayerAnimLib;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.EasingType;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranimcore.animation.keyframe.Keyframe;
import com.zigythebird.playeranimcore.animation.keyframe.KeyframeStack;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranimcore.enums.AnimationFormat;
import com.zigythebird.playeranimcore.math.Vec3f;
import io.netty.buffer.ByteBuf;
import team.unnamed.mocha.parser.MolangParser;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.util.ExpressionListUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public final class AnimationBinary {
    public static final int CURRENT_VERSION = 1;

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
        buf.writeByte(((AnimationFormat)data.getOrDefault("format", AnimationFormat.GECKOLIB)).id);
        buf.writeFloat((float) data.getOrDefault("beginTick", Float.NaN));
        buf.writeFloat((float) data.getOrDefault("endTick", Float.NaN));
        NetworkUtils.writeMap(buf, animation.boneAnimations(), ProtocolUtils::writeString, AnimationBinary::writeBoneAnimation);

        // Sounds
        buf.writeInt(animation.keyFrames().sounds().length);
        for (SoundKeyframeData soundKeyframe : animation.keyFrames().sounds()) {
            buf.writeFloat(soundKeyframe.getStartTick());
            ProtocolUtils.writeString(buf, soundKeyframe.getSound());
        }

        // Particles
        buf.writeInt(animation.keyFrames().particles().length);
        for (ParticleKeyframeData particleKeyframe : animation.keyFrames().particles()) {
            buf.writeFloat(particleKeyframe.getStartTick());
            ProtocolUtils.writeString(buf, particleKeyframe.getEffect());
            ProtocolUtils.writeString(buf, particleKeyframe.getLocator());
            ProtocolUtils.writeString(buf, particleKeyframe.script());
        }

        // Instructions
        buf.writeInt(animation.keyFrames().customInstructions().length);
        for (CustomInstructionKeyframeData instructionKeyframe : animation.keyFrames().customInstructions()) {
            buf.writeFloat(instructionKeyframe.getStartTick());
            ProtocolUtils.writeString(buf, instructionKeyframe.getInstructions());
        }

        NetworkUtils.writeMap(buf, animation.pivotBones(), ProtocolUtils::writeString, NetworkUtils::writeVec3f);
        NetworkUtils.writeMap(buf, animation.parents(), ProtocolUtils::writeString, ProtocolUtils::writeString);
    }

    public static void writeBoneAnimation(ByteBuf buf, BoneAnimation bone) {
        writeKeyframeStack(buf, bone.rotationKeyFrames());
        writeKeyframeStack(buf, bone.positionKeyFrames());
        writeKeyframeStack(buf, bone.scaleKeyFrames());
        NetworkUtils.writeList(buf, bone.bendKeyFrames(), AnimationBinary::writeKeyframe);
    }

    public static void writeKeyframeStack(ByteBuf buf, KeyframeStack stack) {
        NetworkUtils.writeList(buf, stack.xKeyframes(), AnimationBinary::writeKeyframe);
        NetworkUtils.writeList(buf, stack.yKeyframes(), AnimationBinary::writeKeyframe);
        NetworkUtils.writeList(buf, stack.zKeyframes(), AnimationBinary::writeKeyframe);
    }

    public static void writeKeyframe(ByteBuf buf, Keyframe keyframe) {
        buf.writeFloat(keyframe.length());
        writeExpressions(buf, keyframe.startValue());
        writeExpressions(buf, keyframe.endValue());
        buf.writeByte(keyframe.easingType().id);
        NetworkUtils.writeList(buf, keyframe.easingArgs(), AnimationBinary::writeExpressions);
    }

    public static void writeExpressions(ByteBuf buf, List<Expression> expressions) {
        ProtocolUtils.writeString(buf, ExpressionListUtils.toString(expressions));
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
        data.put("format", AnimationFormat.fromId(buf.readByte()));
        float beginTick = buf.readFloat();
        float endTick = buf.readFloat();
        if (!Float.isNaN(beginTick))
            data.put("beginTick", beginTick);
        if (!Float.isNaN(endTick))
            data.put("endTick", endTick);
        Map<String, BoneAnimation> boneAnimations = NetworkUtils.readMap(buf, ProtocolUtils::readString, AnimationBinary::readBoneAnimation);

        // Sounds
        int soundCount = buf.readInt();
        SoundKeyframeData[] sounds = new SoundKeyframeData[soundCount];
        for (int i = 0; i < soundCount; i++) {
            float startTick = buf.readFloat();
            String sound = ProtocolUtils.readString(buf);
            sounds[i] = new SoundKeyframeData(startTick, sound);
        }

        // Particles
        int particleCount = buf.readInt();
        ParticleKeyframeData[] particles = new ParticleKeyframeData[particleCount];
        for (int i = 0; i < particleCount; i++) {
            float startTick = buf.readFloat();
            String effect = ProtocolUtils.readString(buf);
            String locator = ProtocolUtils.readString(buf);
            String script = ProtocolUtils.readString(buf);
            particles[i] = new ParticleKeyframeData(startTick, effect, locator, script);
        }

        // Instructions
        int customInstructionCount = buf.readInt();
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

    public static BoneAnimation readBoneAnimation(ByteBuf buf) {
        KeyframeStack rotationKeyFrames = readKeyframeStack(buf);
        KeyframeStack positionKeyFrames = readKeyframeStack(buf);
        KeyframeStack scaleKeyFrames = readKeyframeStack(buf);
        List<Keyframe> bendKeyFrames = NetworkUtils.readList(buf, AnimationBinary::readKeyframe);

        return new BoneAnimation(rotationKeyFrames, positionKeyFrames, scaleKeyFrames, bendKeyFrames);
    }

    public static KeyframeStack readKeyframeStack(ByteBuf buf) {
        List<Keyframe> xKeyframes = NetworkUtils.readList(buf, AnimationBinary::readKeyframe);
        List<Keyframe> yKeyframes = NetworkUtils.readList(buf, AnimationBinary::readKeyframe);
        List<Keyframe> zKeyframes = NetworkUtils.readList(buf, AnimationBinary::readKeyframe);

        return new KeyframeStack(xKeyframes, yKeyframes, zKeyframes);
    }

    public static Keyframe readKeyframe(ByteBuf buf) {
        float length = buf.readFloat();

        List<Expression> startValue = readExpression(buf);
        List<Expression> endValue = readExpression(buf);
        EasingType easingType = EasingType.fromId(buf.readByte());
        List<List<Expression>> easingArgs = NetworkUtils.readList(buf, AnimationBinary::readExpression);

        return new Keyframe(length, startValue, endValue, easingType, easingArgs);
    }

    public static List<Expression> readExpression(ByteBuf buf) {
        String molang = ProtocolUtils.readString(buf);
        try (MolangParser parser = MolangParser.parser(molang)) {
            return parser.parseAll();
        } catch (IOException e) {
            PlayerAnimLib.LOGGER.error("Failed to parse molang: '{}'!", molang, e);
            return Collections.emptyList();
        }
    }
}
