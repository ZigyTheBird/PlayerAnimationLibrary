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
import org.joml.Vector3d;
import org.joml.Vector3f;
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
        buf.writeBoolean(animation.loopType().shouldPlayAgain(animation));
        if (animation.loopType() == Animation.LoopType.HOLD_ON_LAST_FRAME) buf.writeBoolean(true);
        else {
            buf.writeBoolean(false);
            buf.writeFloat(animation.loopType().restartFromTick(animation));
        }
        NetworkUtils.writeList(buf, animation.boneAnimations(), AnimationBinary::writeBoneAnimation);
        buf.writeInt(animation.keyFrames().sounds().length);
        for (SoundKeyframeData soundKeyframe : animation.keyFrames().sounds()) {
            buf.writeFloat(soundKeyframe.getStartTick());
            NetworkUtils.writeString(buf, soundKeyframe.getSound(), 100);
        }
        buf.writeInt(animation.keyFrames().particles().length);
        for (ParticleKeyframeData particleKeyframe : animation.keyFrames().particles()) {
            buf.writeFloat(particleKeyframe.getStartTick());
            NetworkUtils.writeString(buf, particleKeyframe.getEffect(), 100);
            NetworkUtils.writeString(buf, particleKeyframe.getLocator(), 100);
            NetworkUtils.writeString(buf, particleKeyframe.script(), 100);
        }
        buf.writeInt(animation.keyFrames().customInstructions().length);
        for (CustomInstructionKeyframeData instructionKeyframe : animation.keyFrames().customInstructions()) {
            buf.writeFloat(instructionKeyframe.getStartTick());
            NetworkUtils.writeString(buf, instructionKeyframe.getInstructions(), 32767);
        }
        buf.writeInt(animation.bones().size());
        for (Map.Entry<String, Vector3f> entry : animation.bones().entrySet()) {
            NetworkUtils.writeString(buf, entry.getKey(), 100);
            buf.writeFloat(entry.getValue().x);
            buf.writeFloat(entry.getValue().y);
            buf.writeFloat(entry.getValue().z);
        }
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
        NetworkUtils.writeString(buf, keyframe.easingType().toString(), 20);
        NetworkUtils.writeList(buf, keyframe.easingArgs(), AnimationBinary::writeExpressions);
    }

    public static void writeExpressions(ByteBuf buf, List<Expression> expressions) {
        ProtocolUtils.writeString(buf, ExpressionListUtils.toString(expressions));
    }

    public static Animation read(ByteBuf buf) {
        if (buf.readByte() > CURRENT_VERSION) throw new IllegalStateException();

        float length = buf.readFloat();
        Animation.LoopType loopType;
        if (buf.readBoolean()) {
            if (buf.readBoolean()) loopType = Animation.LoopType.HOLD_ON_LAST_FRAME;
            else loopType = Animation.LoopType.returnToTickLoop(buf.readFloat());
        }
        else loopType = Animation.LoopType.PLAY_ONCE;
        List<BoneAnimation> boneAnimations = NetworkUtils.readList(buf, AnimationBinary::readBoneAnimation);
        Animation.Keyframes keyFrames = new Animation.Keyframes(new SoundKeyframeData[0], new ParticleKeyframeData[0], new CustomInstructionKeyframeData[0]);
        for (int i = 0; i < buf.readInt(); i++) {
            keyFrames.sounds()[i] = new SoundKeyframeData(buf.readFloat(), NetworkUtils.readString(buf, 100));
        }
        for (int i = 0; i < buf.readInt(); i++) {
            keyFrames.particles()[i] = new ParticleKeyframeData(buf.readFloat(), NetworkUtils.readString(buf, 100), NetworkUtils.readString(buf, 100), NetworkUtils.readString(buf, 100));
        }
        for (int i = 0; i < buf.readInt(); i++) {
            keyFrames.customInstructions()[i] = new CustomInstructionKeyframeData(buf.readFloat(), NetworkUtils.readString(buf, 32767));
        }
        Map<String, Vector3f> bones = new HashMap<>();
        for (int i = 0; i < buf.readInt(); i++) {
            bones.put(NetworkUtils.readString(buf, 100), new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat()));
        }
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
        EasingType easingType = EasingType.fromString(NetworkUtils.readString(buf, 20));
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
