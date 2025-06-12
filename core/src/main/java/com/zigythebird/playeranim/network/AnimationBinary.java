package com.zigythebird.playeranim.network;

import com.zigythebird.playeranim.PlayerAnimLib;
import com.zigythebird.playeranim.animation.Animation;
import com.zigythebird.playeranim.animation.EasingType;
import com.zigythebird.playeranim.animation.ExtraAnimationData;
import com.zigythebird.playeranim.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranim.animation.keyframe.Keyframe;
import com.zigythebird.playeranim.animation.keyframe.KeyframeStack;
import com.zigythebird.playeranim.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranim.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranim.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranim.bones.PivotBone;
import io.netty.buffer.ByteBuf;
import team.unnamed.mocha.parser.MolangParser;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.util.ExpressionListUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class AnimationBinary {
    public static final byte CURRENT_VERSION = 0x0;

    public static void write(ByteBuf buf, Animation animation) {
        buf.writeByte(CURRENT_VERSION);
        buf.writeFloat(animation.length());
        // TODO LoopType
        NetworkUtils.writeList(buf, animation.boneAnimations(), AnimationBinary::writeBoneAnimation);
        // TODO Keyframes, bones
        NetworkUtils.writeMap(buf, animation.parents(), ProtocolUtils::writeString, ProtocolUtils::writeString);
    }

    public static void writeBoneAnimation(ByteBuf buf, BoneAnimation bone) {
        ProtocolUtils.writeString(buf, bone.boneName());
        writeKeyframeStack(buf, bone.rotationKeyFrames());
        writeKeyframeStack(buf, bone.positionKeyFrames());
        writeKeyframeStack(buf, bone.scaleKeyFrames());
        writeKeyframeStack(buf, bone.bendKeyFrames());
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
        // buf.writeCharSequence(keyframe.easingType().toString()) TODO easing arg
        NetworkUtils.writeList(buf, keyframe.easingArgs(), AnimationBinary::writeExpressions);
    }

    public static void writeExpressions(ByteBuf buf, List<Expression> expressions) {
        ProtocolUtils.writeString(buf, ExpressionListUtils.toString(expressions));
    }

    public static Animation read(ByteBuf buf) {
        if (buf.readByte() > CURRENT_VERSION) throw new IllegalStateException();

        float length = buf.readFloat();
        Animation.LoopType loopType = Animation.LoopType.DEFAULT; // TODO
        List<BoneAnimation> boneAnimations = NetworkUtils.readList(buf, AnimationBinary::readBoneAnimation);
        Animation.Keyframes keyFrames = new Animation.Keyframes(new SoundKeyframeData[0], new ParticleKeyframeData[0], new CustomInstructionKeyframeData[0]); // TODO
        Map<String, PivotBone > bones = new HashMap<>(); // TODO
        Map<String, String> parents = NetworkUtils.readMap(buf, ProtocolUtils::readString, ProtocolUtils::readString);

        return new Animation(new ExtraAnimationData(), length, loopType, boneAnimations, keyFrames, bones, parents);
    }

    public static BoneAnimation readBoneAnimation(ByteBuf buf) {
        String boneName = ProtocolUtils.readString(buf);

        KeyframeStack rotationKeyFrames = readKeyframeStack(buf);
        KeyframeStack positionKeyFrames = readKeyframeStack(buf);
        KeyframeStack scaleKeyFrames = readKeyframeStack(buf);
        KeyframeStack bendKeyFrames = readKeyframeStack(buf);

        return new BoneAnimation(boneName, rotationKeyFrames, positionKeyFrames, scaleKeyFrames, bendKeyFrames);
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
        EasingType easingType = EasingType.LINEAR; // TODO
        List<List<Expression>> easingArgs = NetworkUtils.readList(buf, AnimationBinary::readExpression);

        return new Keyframe(length, startValue, endValue, easingType, easingArgs);
    }

    public static List<Expression> readExpression(ByteBuf buf) {
        try (MolangParser parser = MolangParser.parser(ProtocolUtils.readString(buf))) {
            return parser.parseAll();
        } catch (IOException e) {
            PlayerAnimLib.LOGGER.error("Failed to read molang!", e);
            return Collections.emptyList();
        }
    }
}
