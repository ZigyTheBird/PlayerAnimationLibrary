/*
 * MIT License
 *
 * Copyright (c) 2022 KosmX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.zigythebird.playeranimcore.network;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.EasingType;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranimcore.animation.keyframe.Keyframe;
import com.zigythebird.playeranimcore.enums.AnimationFormat;
import com.zigythebird.playeranimcore.loading.PlayerAnimatorLoader;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import com.zigythebird.playeranimcore.math.Vec3f;
import com.zigythebird.playeranimcore.molang.MolangLoader;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.parser.ast.FloatExpression;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

/**
 * Utility class to convert animation data to a binary format.
 * Includes a size predictor, using {@link java.nio.ByteBuffer}
 * Does <b>not</b> pack extraData, that must be done manually
 */
@SuppressWarnings("unused")
public final class LegacyAnimationBinary {
    private static final MochaEngine<?> MOCHA_ENGINE = MolangLoader.createNewEngine();
    public static final Predicate<String> BEND_BONE = name -> !name.equals("head") && !name.equals("left_item") && !name.equals("right_item");

    /**
     * Write the animation into the ByteBuffer.
     * Versioning:
     * 1. Emotecraft 2.1 features
     * 2. New animation format for Animation library - including enable states, dynamic parts
     * Format type 1 takes less data, but only works for standard models and unable to send data for disabled states
     * @param animation animation
     * @param buf       target byteBuf
     * @param version   Binary version
     * @return          target byteBuf for chaining
     *
     * @throws java.nio.BufferOverflowException if can't write into ByteBuf
     */
    public static ByteBuffer write(Animation animation, ByteBuffer buf, int version) throws BufferOverflowException {
        buf.putInt(animation.data().<Float>get("beginTick").orElse(0F).intValue());
        buf.putInt((animation.data().<Float>get("endTick").orElse(animation.length()).intValue()));
        buf.putInt((int) animation.length());
        putBoolean(buf, animation.loopType().shouldPlayAgain(animation));
        buf.putInt((int)animation.loopType().restartFromTick(animation));
        putBoolean(buf, animation.data().<Boolean>get("isEasingBefore").orElse(false));
        putBoolean(buf, animation.data().<Boolean>get("nsfw").orElse(false));
        buf.put(keyframeSize(version));
        if (version >= 2) {
            buf.putInt(animation.boneAnimations().size());
            for (Map.Entry<String, BoneAnimation> part : animation.boneAnimations().entrySet()) {
                putString(buf, UniversalAnimLoader.restorePlayerBoneName(part.getKey()));
                writePart(buf, part.getKey(), part.getValue(), version);
            }
        } else {
            writePart(buf, "head", animation.getBone("head"), version);
            writePart(buf, "body", animation.getBone("body"), version);
            writePart(buf, "right_arm", animation.getBone("right_arm"), version);
            writePart(buf, "left_arm", animation.getBone("left_arm"), version);
            writePart(buf, "right_leg", animation.getBone("right_leg"), version);
            writePart(buf, "left_leg", animation.getBone("left_leg"), version);
        }
        buf.putLong(animation.uuid().getMostSignificantBits());
        buf.putLong(animation.uuid().getLeastSignificantBits());

        return buf;
    }

    /**
     * Write the animation into the ByteBuffer using the latest format version
     * @param animation animation
     * @param buf       target byteBuf
     * @return          target byteBuf for chaining
     * @throws BufferOverflowException if can't write into byteBuf
     */
    public static ByteBuffer write(Animation animation, ByteBuffer buf) throws BufferOverflowException {
        return write(animation, buf, getCurrentVersion());
    }

    private static void writePart(ByteBuffer buf, String name, BoneAnimation part, int version) {
        if (part == null) {
            int i = 6;
            if (BEND_BONE.test(name)) i += 2;
            if (version >= 3) i += 3;
            for (; i > 0; i--) {
                if (version >= 2) {
                    putBoolean(buf, false);
                    buf.putInt(0);
                } else buf.putInt(-1);
            }
            return;
        }
        Vec3f def = PlayerAnimatorLoader.getDefaultValues(name);
        writeKeyframes(buf, part.positionKeyFrames().xKeyframes(), def.x(), version);
        writeKeyframes(buf, part.positionKeyFrames().yKeyframes(), def.y(), version);
        writeKeyframes(buf, part.positionKeyFrames().zKeyframes(), def.z(), version);
        writeKeyframes(buf, part.rotationKeyFrames().xKeyframes(), 0, version);
        writeKeyframes(buf, part.rotationKeyFrames().yKeyframes(), 0, version);
        writeKeyframes(buf, part.rotationKeyFrames().zKeyframes(), 0, version);
        if (BEND_BONE.test(name)) {
            writeKeyframes(buf, part.bendKeyFrames().xKeyframes(), 0, version);
            writeKeyframes(buf, part.bendKeyFrames().yKeyframes(), 0, version);
        }
        if (version >= 3) {
            writeKeyframes(buf, part.scaleKeyFrames().xKeyframes(), 0, version);
            writeKeyframes(buf, part.scaleKeyFrames().yKeyframes(), 0, version);
            writeKeyframes(buf, part.scaleKeyFrames().zKeyframes(), 0, version);
        }
    }

    private static void writeKeyframes(ByteBuffer buf, List<Keyframe> part, float def, int version) {
        if (version >= 2) {
            putBoolean(buf, !part.isEmpty());
            buf.putInt(part.size());
        } else {
            buf.putInt(part.size());
        }
        int tick = 0;
        for (Keyframe move : part) {
            buf.putInt(tick);
            tick += (int) move.length();
            buf.putFloat(MOCHA_ENGINE.eval(move.endValue()));
            buf.put(move.easingType().id);

            if (version >= 4) {
                if (!move.easingArgs().isEmpty()) {
                    buf.putFloat(MOCHA_ENGINE.eval(move.easingArgs().getFirst()) + def);
                } else buf.putFloat(Float.NaN);
            }
        }
    }

    /**
     * Read keyframe animation from binary data.
     * Creates a Bool extra property with validation data with name <code>valid</code>
     * @param buf       byteBuf
     * @param version   format version (not stored in binary)
     * @return          KeyframeAnimation
     * @throws java.nio.BufferUnderflowException if there is not enough data in ByteBuffer
     * @throws IOException if encounters invalid data
     */
    public static Animation read(ByteBuffer buf, int version) throws IOException {
        ExtraAnimationData data = new ExtraAnimationData();
        data.put("beginTick", (float) buf.getInt());
        data.put("endTick", (float) buf.getInt());
        float stopTick = (float) buf.getInt();
        boolean shouldLoop = getBoolean(buf);
        float returnTick = (float) buf.getInt();
        Animation.LoopType loopType = shouldLoop ? Animation.LoopType.returnToTickLoop(returnTick) : Animation.LoopType.PLAY_ONCE;
        boolean easeBefore = getBoolean(buf);
        data.put("isEasingBefore", easeBefore);
        data.put("nsfw", getBoolean(buf));
        int keyframeSize = buf.get();
        if (keyframeSize <= 0) throw new IOException("keyframe size must be greater than 0, current: " + keyframeSize);
        Map<String, BoneAnimation> boneAnimations = new HashMap<>();
        if (version >= 2) {
            int count = buf.getInt();
            for (int i = 0; i < count; i++) {
                String name = UniversalAnimLoader.getCorrectPlayerBoneName(getString(buf));
                boneAnimations.put(name, readPart(buf, name, new BoneAnimation(), version, keyframeSize, easeBefore));
            }
        } else {
            boneAnimations.put("head", readPart(buf, "head", new BoneAnimation(), version, keyframeSize, easeBefore));
            boneAnimations.put("body", readPart(buf, "body", new BoneAnimation(), version, keyframeSize, easeBefore));
            boneAnimations.put("right_arm", readPart(buf, "right_arm", new BoneAnimation(), version, keyframeSize, easeBefore));
            boneAnimations.put("left_arm", readPart(buf, "left_arm", new BoneAnimation(), version, keyframeSize, easeBefore));
            boneAnimations.put("right_leg", readPart(buf, "right_leg", new BoneAnimation(), version, keyframeSize, easeBefore));
            boneAnimations.put("left_leg", readPart(buf, "left_leg", new BoneAnimation(), version, keyframeSize, easeBefore));

            BoneAnimation body = boneAnimations.get("body");
            if (body.bendKeyFrames().hasKeyframes()) {
                BoneAnimation torso = new BoneAnimation();
                torso.bendKeyFrames().xKeyframes().addAll(body.bendKeyFrames().xKeyframes());
                torso.bendKeyFrames().yKeyframes().addAll(body.bendKeyFrames().yKeyframes());
                body.bendKeyFrames().xKeyframes().clear();
                body.bendKeyFrames().yKeyframes().clear();
                boneAnimations.put("torso", torso);
            }
        }
        long msb = buf.getLong();
        long lsb = buf.getLong();
        data.put("uuid", new UUID(msb, lsb));
        data.put("format", AnimationFormat.PLAYER_ANIMATOR);

        return new Animation(data, stopTick, loopType, boneAnimations, UniversalAnimLoader.NO_KEYFRAMES, new HashMap<>(), new HashMap<>());
    }

    private static BoneAnimation readPart(ByteBuffer buf, String name, BoneAnimation part, int version, int keyframeSize, boolean easeBefore) {
        Vec3f def = PlayerAnimatorLoader.getDefaultValues(name);
        readKeyframes(buf, part.positionKeyFrames().xKeyframes(), def.x(), version, keyframeSize);
        readKeyframes(buf, part.positionKeyFrames().yKeyframes(), def.y(), version, keyframeSize);
        readKeyframes(buf, part.positionKeyFrames().zKeyframes(), def.z(), version, keyframeSize);
        readKeyframes(buf, part.rotationKeyFrames().xKeyframes(), 0, version, keyframeSize);
        readKeyframes(buf, part.rotationKeyFrames().yKeyframes(), 0, version, keyframeSize);
        readKeyframes(buf, part.rotationKeyFrames().zKeyframes(), 0, version, keyframeSize);
        if (BEND_BONE.test(name)) {
            readKeyframes(buf, part.bendKeyFrames().xKeyframes(), 0, version, keyframeSize);
            readKeyframes(buf, part.bendKeyFrames().yKeyframes(), 0, version, keyframeSize);
        }
        if (version >= 3) {
            readKeyframes(buf, part.scaleKeyFrames().xKeyframes(), 0, version, keyframeSize);
            readKeyframes(buf, part.scaleKeyFrames().yKeyframes(), 0, version, keyframeSize);
            readKeyframes(buf, part.scaleKeyFrames().zKeyframes(), 0, version, keyframeSize);
        }
        /*if (!easeBefore) {
            PlayerAnimatorLoader.correctEasings(part.positionKeyFrames());
            PlayerAnimatorLoader.correctEasings(part.rotationKeyFrames());
            PlayerAnimatorLoader.correctEasings(part.scaleKeyFrames());
            PlayerAnimatorLoader.correctEasings(part.bendKeyFrames());
            if (name.equals("right_item") || name.equals("left_item"))
                PlayerAnimatorLoader.swapTheZYAxisOfRotation(part.rotationKeyFrames());
        }*/
        return part;
    }

    private static void readKeyframes(ByteBuffer buf, List<Keyframe> part, float def, int version, int keyframeSize) {
        int length;
        boolean enabled;
        if (version >= 2) {
            enabled = getBoolean(buf);
            length = buf.getInt();
        } else {
            length = buf.getInt();
            enabled = length >= 0;
        }

        if (!enabled) {
            if (length > 0) {
                buf.position(buf.position() + length * keyframeSize);
            }
            part.clear();
            return;
        }

        int lastTick = 0;
        for (int i = 0; i < length; i++) {
            Keyframe prevKeyframe = part.isEmpty() ? null : part.getLast();
            int currentPos = buf.position();

            int tick = buf.getInt();
            float keyframeLength = (float)tick - lastTick;
            lastTick = tick;

            List<Expression> expression = Collections.singletonList(FloatExpression.of(buf.getFloat() - def));
            EasingType easingType = EasingType.fromId(buf.get());
            Float easingArg = null;

            if (version >= 4) {
                easingArg = buf.getFloat();

                if (Float.isNaN(easingArg)) {
                    easingArg = null;
                }
            }

            part.add(new Keyframe(keyframeLength, prevKeyframe == null ? expression : prevKeyframe.endValue(), expression, easingType,
                    easingArg == null ? Collections.singletonList(Collections.emptyList()) :
                            Collections.singletonList(Collections.singletonList(FloatExpression.of(easingArg)))));
            buf.position(currentPos + keyframeSize);
        }
    }

    /**
     * Current animation binary version
     * @return version
     */
    public static int getCurrentVersion() {
        return 4;
    }

    public static int calculateSize(Animation animation, int version) {
        //I will create less efficient loops, but these will be more easily fixable
        int size = 36;//The header makes xx bytes IIIBIBBBLL
        if (version < 2) {
            size += partSize(animation.getBone("head"), false, version);
            size += partSize(animation.getBone("body"), true, version);
            size += partSize(animation.getBone("right_arm"), true, version);
            size += partSize(animation.getBone("left_arm"), true, version);
            size += partSize(animation.getBone("right_leg"), true, version);
            size += partSize(animation.getBone("left_leg"), true, version);
        } else {
            size += 4;
            for (Map.Entry<String, BoneAnimation> entry : animation.boneAnimations().entrySet()) {
                size += stringSize(entry.getKey()) + partSize(entry.getValue(), BEND_BONE.test(entry.getKey()), version);
            }
        }
        //The size of an empty emote is 230 bytes.
        //But that makes the size to be 230 + keyframes count*9 bytes.
        //46 axes, including bends for every body-part except head.
        return size;
    }

    private static int stringSize(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        return bytes.length + 4;
    }

    private static int partSize(BoneAnimation part, boolean bendable, int version) {
        if (part == null) {
            int i = 6;
            if (bendable) i += 2;
            if (version >= 3) i += 3;
            return i * keyframeSize(version) + (version >= 2 ? 5 : 4);
        }
        int size = 0;
        size += axisSize(part.positionKeyFrames().xKeyframes(), version);
        size += axisSize(part.positionKeyFrames().yKeyframes(), version);
        size += axisSize(part.positionKeyFrames().zKeyframes(), version);
        size += axisSize(part.rotationKeyFrames().xKeyframes(), version);
        size += axisSize(part.rotationKeyFrames().yKeyframes(), version);
        size += axisSize(part.rotationKeyFrames().zKeyframes(), version);
        if (bendable) {
            size += axisSize(part.bendKeyFrames().xKeyframes(), version);
            size += axisSize(part.bendKeyFrames().yKeyframes(), version);
        }
        if (version >= 3) {
            size += axisSize(part.scaleKeyFrames().xKeyframes(), version);
            size += axisSize(part.scaleKeyFrames().yKeyframes(), version);
            size += axisSize(part.scaleKeyFrames().zKeyframes(), version);
        }
        return size;
    }

    private static int axisSize(List<Keyframe> axis, int version){
        return axis.size()*keyframeSize(version) + (version >= 2 ? 5 : 4);// count*IFB + I (for count)
    }

    /**
     * Size needed for one keyframe
     */
    private static byte keyframeSize(int version) {
        return version < 4 ? (byte) 9 /* 4 (int) + 4 (float) + 1 (byte) */ : (byte) 13; /* + 4 (float) */
    }

    /**
     * Writes a bool value into byteBuffer, using one byte per bool
     * @param byteBuffer buf
     * @param bl         bool
     */
    public static void putBoolean(ByteBuffer byteBuffer, boolean bl){
        byteBuffer.put((byte) (bl ? 1 : 0));
    }

    /**
     * Reads a bool value from byteBuffer
     * @param buf buf
     * @return    bool
     */
    public static boolean getBoolean(ByteBuffer buf) {
        return buf.get() != (byte) 0;
    }

    /**
     * Writes a binary string into byteBuf
     * first 4 bytes for size, then string data
     * @param buf buf
     * @param str str
     */
    public static void putString(ByteBuffer buf, String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        buf.putInt(bytes.length);
        buf.put(bytes);
    }

    /**
     * Reads string from buf, see {@link LegacyAnimationBinary#putString(ByteBuffer, String)}
     * @param buf buf
     * @return str
     */
    public static String getString(ByteBuffer buf) {
        int len = buf.getInt();
        byte[] bytes = new byte[len];
        buf.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}