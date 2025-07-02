package com.zigythebird.playeranimcore.network;

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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.parser.ast.FloatExpression;
import team.unnamed.mocha.util.ExprBytesUtils;
import team.unnamed.mocha.util.VarIntUtils;

import java.util.ArrayList;
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
        buf.writeBoolean(animation.usesMolang());
        buf.writeFloat((float) data.getOrDefault("beginTick", Float.NaN));
        buf.writeFloat((float) data.getOrDefault("endTick", Float.NaN));
        NetworkUtils.writeUuid(buf, animation.uuid()); // required by emotecraft to stop animations
        NetworkUtils.writeMap(buf, animation.boneAnimations(), ExprBytesUtils::writeString, (byteBuf, boneAnimation)
                -> writeBoneAnimation(byteBuf, boneAnimation, animation.usesMolang()));

        // Sounds
        VarIntUtils.writeVarInt(buf, animation.keyFrames().sounds().length);
        for (SoundKeyframeData soundKeyframe : animation.keyFrames().sounds()) {
            buf.writeFloat(soundKeyframe.getStartTick());
            ExprBytesUtils.writeString(buf, soundKeyframe.getSound());
        }

        // Particles
        VarIntUtils.writeVarInt(buf, animation.keyFrames().particles().length);
        for (ParticleKeyframeData particleKeyframe : animation.keyFrames().particles()) {
            buf.writeFloat(particleKeyframe.getStartTick());
            ExprBytesUtils.writeString(buf, particleKeyframe.getEffect());
            ExprBytesUtils.writeString(buf, particleKeyframe.getLocator());
            ExprBytesUtils.writeString(buf, particleKeyframe.script());
        }

        // Instructions
        VarIntUtils.writeVarInt(buf, animation.keyFrames().customInstructions().length);
        for (CustomInstructionKeyframeData instructionKeyframe : animation.keyFrames().customInstructions()) {
            buf.writeFloat(instructionKeyframe.getStartTick());
            ExprBytesUtils.writeString(buf, instructionKeyframe.getInstructions());
        }

        NetworkUtils.writeMap(buf, animation.pivotBones(), ExprBytesUtils::writeString, NetworkUtils::writeVec3f);
        NetworkUtils.writeMap(buf, animation.parents(), ExprBytesUtils::writeString, ExprBytesUtils::writeString);
    }

    public static void writeBoneAnimation(ByteBuf buf, BoneAnimation bone, boolean usesMolang) {
        writeKeyframeStack(buf, bone.rotationKeyFrames(), usesMolang);
        writeKeyframeStack(buf, bone.positionKeyFrames(), usesMolang);
        writeKeyframeStack(buf, bone.scaleKeyFrames(), usesMolang);
        ExprBytesUtils.writeList(buf, bone.bendKeyFrames(), (keyframe, byteBuf) -> writeKeyframe(keyframe, byteBuf, usesMolang));
    }

    public static void writeKeyframeStack(ByteBuf buf, KeyframeStack stack, boolean usesMolang) {
        ExprBytesUtils.writeList(buf, stack.xKeyframes(), (keyframe, byteBuf) -> writeKeyframe(keyframe, byteBuf, usesMolang));
        ExprBytesUtils.writeList(buf, stack.yKeyframes(), (keyframe, byteBuf) -> writeKeyframe(keyframe, byteBuf, usesMolang));
        ExprBytesUtils.writeList(buf, stack.zKeyframes(), (keyframe, byteBuf) -> writeKeyframe(keyframe, byteBuf, usesMolang));
    }

    public static void writeKeyframe(Keyframe keyframe, ByteBuf buf, boolean usesMolang) {
        buf.writeFloat(keyframe.length());
        if (usesMolang)
            ExprBytesUtils.writeList(buf, keyframe.endValue(), ExprBytesUtils::writeExpression);
        else
            buf.writeFloat(((FloatExpression)keyframe.endValue().getFirst()).value());
        buf.writeByte(keyframe.easingType().id);
        if (usesMolang)
            ExprBytesUtils.writeList(buf, keyframe.easingArgs(), (expressions, buf1) ->
                ExprBytesUtils.writeList(buf1, expressions, ExprBytesUtils::writeExpression)
        );
        else {
            List<List<Expression>> easingArgs = keyframe.easingArgs();
            if (!easingArgs.isEmpty() && !easingArgs.getFirst().isEmpty())
                buf.writeFloat(((FloatExpression)easingArgs.getFirst().getFirst()).value());
            else buf.writeFloat(Float.NaN);
        }
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
        boolean usesMolang = buf.readBoolean();
        float beginTick = buf.readFloat();
        float endTick = buf.readFloat();
        if (!Float.isNaN(beginTick))
            data.put("beginTick", beginTick);
        if (!Float.isNaN(endTick))
            data.put("endTick", endTick);

        data.put(ExtraAnimationData.UUID_KEY, NetworkUtils.readUuid(buf)); // required by emotecraft to stop animations
        Map<String, BoneAnimation> boneAnimations = NetworkUtils.readMap(buf, ExprBytesUtils::readString, byteBuf -> readBoneAnimation(byteBuf, usesMolang));

        // Sounds
        int soundCount = VarIntUtils.readVarInt(buf);
        SoundKeyframeData[] sounds = new SoundKeyframeData[soundCount];
        for (int i = 0; i < soundCount; i++) {
            float startTick = buf.readFloat();
            String sound = ExprBytesUtils.readString(buf);
            sounds[i] = new SoundKeyframeData(startTick, sound);
        }

        // Particles
        int particleCount = VarIntUtils.readVarInt(buf);
        ParticleKeyframeData[] particles = new ParticleKeyframeData[particleCount];
        for (int i = 0; i < particleCount; i++) {
            float startTick = buf.readFloat();
            String effect = ExprBytesUtils.readString(buf);
            String locator = ExprBytesUtils.readString(buf);
            String script = ExprBytesUtils.readString(buf);
            particles[i] = new ParticleKeyframeData(startTick, effect, locator, script);
        }

        // Instructions
        int customInstructionCount = VarIntUtils.readVarInt(buf);
        CustomInstructionKeyframeData[] customInstructions = new CustomInstructionKeyframeData[customInstructionCount];
        for (int i = 0; i < customInstructionCount; i++) {
            float startTick = buf.readFloat();
            String instructions = ExprBytesUtils.readString(buf);
            customInstructions[i] = new CustomInstructionKeyframeData(startTick, instructions);
        }
        Animation.Keyframes keyFrames = new Animation.Keyframes(sounds, particles, customInstructions);

        Map<String, Vec3f> pivotBones = NetworkUtils.readMap(buf, ExprBytesUtils::readString, NetworkUtils::readVec3f);
        Map<String, String> parents = NetworkUtils.readMap(buf, ExprBytesUtils::readString, ExprBytesUtils::readString);

        return new Animation(data, length, loopType, boneAnimations, keyFrames, pivotBones, parents, usesMolang);
    }

    public static BoneAnimation readBoneAnimation(ByteBuf buf, boolean usesMolang) {
        KeyframeStack rotationKeyFrames = readKeyframeStack(buf, usesMolang);
        KeyframeStack positionKeyFrames = readKeyframeStack(buf, usesMolang);
        KeyframeStack scaleKeyFrames = readKeyframeStack(buf, usesMolang);
        List<Keyframe> bendKeyFrames = readKeyframeList(buf, usesMolang);

        return new BoneAnimation(rotationKeyFrames, positionKeyFrames, scaleKeyFrames, bendKeyFrames);
    }

    public static KeyframeStack readKeyframeStack(ByteBuf buf, boolean usesMolang) {
        List<Keyframe> xKeyframes = readKeyframeList(buf, usesMolang);
        List<Keyframe> yKeyframes = readKeyframeList(buf, usesMolang);
        List<Keyframe> zKeyframes = readKeyframeList(buf, usesMolang);

        return new KeyframeStack(xKeyframes, yKeyframes, zKeyframes);
    }

    public static List<Keyframe> readKeyframeList(ByteBuf buf, boolean usesMolang) {
        int count = buf.readInt();
        List<Keyframe> list = new ArrayList<>(count);

        for(int i = 0; i < count; ++i) {
            float length = buf.readFloat();

            List<Expression> endValue = usesMolang ? ExprBytesUtils.readList(buf, ExprBytesUtils::readExpression) : Collections.singletonList(FloatExpression.of(buf.readFloat()));
            List<Expression> startValue = !list.isEmpty() ? list.getLast().endValue() : endValue;
            EasingType easingType = EasingType.fromId(buf.readByte());
            List<List<Expression>> easingArgs;
            if (usesMolang)
                easingArgs = ExprBytesUtils.readList(buf,
                    buf1 -> ExprBytesUtils.readList(buf1, ExprBytesUtils::readExpression)
                );
            else {
                float easingArg = buf.readFloat();
                if (Float.isNaN(easingArg))
                    easingArgs = new ObjectArrayList<>();
                else easingArgs = Collections.singletonList(Collections.singletonList(FloatExpression.of(easingArg)));
            }

            list.add(new Keyframe(length, startValue, endValue, easingType, easingArgs));
        }

        return list;
    }
}
