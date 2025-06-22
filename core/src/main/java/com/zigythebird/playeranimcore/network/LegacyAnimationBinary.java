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
import com.zigythebird.playeranimcore.loading.PlayerAnimatorLoader;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import com.zigythebird.playeranimcore.misc.UnsupportedKeyframeForLegacyBinaryException;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.parser.ast.FloatExpression;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Utility class to convert animation data to a binary format.
 * Includes a size predictor, using {@link java.nio.ByteBuffer}
 * Does <b>not</b> pack extraData, that must be done manually
 */
@SuppressWarnings("unused")
public final class LegacyAnimationBinary {
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
     * @param <T> ByteBuffer
     *
     * @throws java.nio.BufferOverflowException if can't write into ByteBuf
     */
    public static <T extends ByteBuffer> T write(Animation animation, T buf, int version) throws BufferOverflowException {
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
            for (BoneAnimation part : animation.boneAnimations()) {
                putString(buf, part.boneName());
                writePart(buf, part, version);
            }
        } else {
            writePart(buf, animation.boneAnimations().stream()
                    .filter(boneAnimation -> boneAnimation.boneName().equals("head"))
                    .findFirst().orElse(null), version);

            writePart(buf, animation.boneAnimations().stream()
                    .filter(boneAnimation -> boneAnimation.boneName().equals("head"))
                    .findFirst().orElse(null), version);

            writePart(buf, animation.boneAnimations().stream()
                    .filter(boneAnimation -> boneAnimation.boneName().equals("body"))
                    .findFirst().orElse(null), version);

            writePart(buf, animation.boneAnimations().stream()
                    .filter(boneAnimation -> boneAnimation.boneName().equals("right_arm"))
                    .findFirst().orElse(null), version);

            writePart(buf, animation.boneAnimations().stream()
                    .filter(boneAnimation -> boneAnimation.boneName().equals("left_arm"))
                    .findFirst().orElse(null), version);

            writePart(buf, animation.boneAnimations().stream()
                    .filter(boneAnimation -> boneAnimation.boneName().equals("right_leg"))
                    .findFirst().orElse(null), version);

            writePart(buf, animation.boneAnimations().stream()
                    .filter(boneAnimation -> boneAnimation.boneName().equals("left_leg"))
                    .findFirst().orElse(null), version);
        }
        buf.putLong(animation.data().uuid().getMostSignificantBits());
        buf.putLong(animation.data().uuid().getLeastSignificantBits());

        return buf;
    }

    /**
     * Write the animation into the ByteBuffer using the latest format version
     * @param animation animation
     * @param buf       target byteBuf
     * @return          target byteBuf for chaining
     * @param <T>       ByteBuffer
     * @throws BufferOverflowException if can't write into byteBuf
     */
    public static <T extends ByteBuffer> T write(Animation animation, T buf) throws BufferOverflowException {
        return write(animation, buf, getCurrentVersion());
    }

    @SuppressWarnings("ConstantConditions")
    private static void writePart(ByteBuffer buf, BoneAnimation part, int version) {
        if (part == null) {
            int i = 6;
            if (!part.boneName().equals("head")) i += 2;
            if (version >= 3) i += 3;
            for (; i >= 0; i--) {
                if (version >= 2) {
                    putBoolean(buf, false);
                    buf.putInt(0);
                } else buf.putInt(0);
            }
        }
        writeKeyframes(buf, part.positionKeyFrames().xKeyframes(), version);
        writeKeyframes(buf, part.positionKeyFrames().yKeyframes(), version);
        writeKeyframes(buf, part.positionKeyFrames().zKeyframes(), version);
        writeKeyframes(buf, part.rotationKeyFrames().xKeyframes(), version);
        writeKeyframes(buf, part.rotationKeyFrames().yKeyframes(), version);
        writeKeyframes(buf, part.rotationKeyFrames().zKeyframes(), version);
        if (!part.boneName().equals("head")) {
            writeKeyframes(buf, part.bendKeyFrames().xKeyframes(), version);
            writeKeyframes(buf, part.bendKeyFrames().yKeyframes(), version);
        }
        if (version >= 3) {
            writeKeyframes(buf, part.scaleKeyFrames().xKeyframes(), version);
            writeKeyframes(buf, part.scaleKeyFrames().yKeyframes(), version);
            writeKeyframes(buf, part.scaleKeyFrames().zKeyframes(), version);
        }
    }

    private static void writeKeyframes(ByteBuffer buf, List<Keyframe> part, int version) {
        if (version >= 2) {
            putBoolean(buf, true);
            buf.putInt(part.size());
        } else {
            buf.putInt(part.size());
        }
        int tick = 0;
        for (Keyframe move : part) {
            buf.putInt(tick);
            tick += (int) move.length();
            List<Expression> endValue = move.endValue();
            if (endValue.size() == 1 && endValue.getFirst() instanceof FloatExpression expression) {
                buf.putFloat(expression.value());
                buf.put(EasingType.getIDForEasingType(move.easingType()));
                if (version >= 4) {
                    if (!move.easingArgs().isEmpty() && move.easingArgs().getFirst().getFirst() instanceof FloatExpression expression1) {
                        buf.putFloat(expression1.value());
                    } else buf.putFloat(Float.NaN);
                }
            }
            else throw new UnsupportedKeyframeForLegacyBinaryException();
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
        data.put("beginTick", buf.getInt());
        data.put("endTick", buf.getInt());
        float stopTick = buf.getInt();
        boolean shouldLoop = getBoolean(buf);
        float returnTick = buf.getInt();
        Animation.LoopType loopType = shouldLoop ? Animation.LoopType.returnToTickLoop(returnTick) : Animation.LoopType.PLAY_ONCE;
        boolean easeBefore = getBoolean(buf);
        data.put("isEasingBefore", easeBefore);
        data.put("nsfw", getBoolean(buf));
        int keyframeSize = buf.get();
        if (keyframeSize <= 0) throw new IOException("keyframe size must be greater than 0, current: " + keyframeSize);
        List<BoneAnimation> boneAnimations = new ArrayList<>();
        if (version >= 2) {
            int count = buf.getInt();
            for (int i = 0; i < count; i++) {
                String name = getString(buf);
                boneAnimations.add(readPart(buf, new BoneAnimation(name), version, keyframeSize, easeBefore));
            }
        } else {
            boneAnimations.add(readPart(buf, new BoneAnimation("head"), version, keyframeSize, easeBefore));
            boneAnimations.add(readPart(buf, new BoneAnimation("body"), version, keyframeSize, easeBefore));
            boneAnimations.add(readPart(buf, new BoneAnimation("right_arm"), version, keyframeSize, easeBefore));
            boneAnimations.add(readPart(buf, new BoneAnimation("left_arm"), version, keyframeSize, easeBefore));
            boneAnimations.add(readPart(buf, new BoneAnimation("right_left"), version, keyframeSize, easeBefore));
            boneAnimations.add(readPart(buf, new BoneAnimation("left_leg"), version, keyframeSize, easeBefore));

            BoneAnimation body = boneAnimations.get(1);
            if (body.bendKeyFrames().hasKeyframes()) {
                BoneAnimation torso = new BoneAnimation("torso");
                torso.bendKeyFrames().xKeyframes().addAll(body.bendKeyFrames().xKeyframes());
                torso.bendKeyFrames().yKeyframes().addAll(body.bendKeyFrames().yKeyframes());
                body.bendKeyFrames().xKeyframes().clear();
                body.bendKeyFrames().yKeyframes().clear();
                boneAnimations.add(torso);
            }
        }
        long msb = buf.getLong();
        long lsb = buf.getLong();
        data.put("uuid", new UUID(msb, lsb));

        return new Animation(data, stopTick, loopType, boneAnimations, UniversalAnimLoader.NO_KEYFRAMES, new HashMap<>(), new HashMap<>());
    }

    private static BoneAnimation readPart(ByteBuffer buf, BoneAnimation part, int version, int keyframeSize, boolean easeBefore) {
        readKeyframes(buf, part.positionKeyFrames().xKeyframes(), version, keyframeSize);
        readKeyframes(buf, part.positionKeyFrames().yKeyframes(), version, keyframeSize);
        readKeyframes(buf, part.positionKeyFrames().zKeyframes(), version, keyframeSize);
        readKeyframes(buf, part.rotationKeyFrames().xKeyframes(), version, keyframeSize);
        readKeyframes(buf, part.rotationKeyFrames().yKeyframes(), version, keyframeSize);
        readKeyframes(buf, part.rotationKeyFrames().zKeyframes(), version, keyframeSize);
        String partName = part.boneName();
        if (!partName.equals("head")) {
            readKeyframes(buf, part.bendKeyFrames().xKeyframes(), version, keyframeSize);
            readKeyframes(buf, part.bendKeyFrames().yKeyframes(), version, keyframeSize);
        }
        if (version >= 3) {
            readKeyframes(buf, part.scaleKeyFrames().xKeyframes(), version, keyframeSize);
            readKeyframes(buf, part.scaleKeyFrames().yKeyframes(), version, keyframeSize);
            readKeyframes(buf, part.scaleKeyFrames().zKeyframes(), version, keyframeSize);
        }
        PlayerAnimatorLoader.correctEasings(part.positionKeyFrames());
        PlayerAnimatorLoader.correctEasings(part.rotationKeyFrames());
        PlayerAnimatorLoader.correctEasings(part.scaleKeyFrames());
        PlayerAnimatorLoader.correctEasings(part.bendKeyFrames());
        return part;
    }



    private static void readKeyframes(ByteBuffer buf, List<Keyframe> part, int version, int keyframeSize) {
        int length;
        length = buf.getInt();
        for (int i = 0; i < length; i++) {
            Keyframe prevKeyframe = part.isEmpty() ? null : part.getLast();
            int currentPos = buf.position();

            int tick = buf.getInt();
            List<Expression> expression = Collections.singletonList(FloatExpression.of(buf.getFloat()));
            EasingType easingType = EasingType.getEasingTypeForID(buf.get());
            Float easingArg = null;

            if (version >= 4) {
                easingArg = buf.getFloat();

                if (Float.isNaN(easingArg)) {
                    easingArg = null;
                }
            }

            part.add(new Keyframe(tick, prevKeyframe == null ? expression : prevKeyframe.endValue(), expression, easingType,
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
            for (BoneAnimation boneAnimation : animation.boneAnimations()) {
                String partName = boneAnimation.boneName();
                if (partName.equals("right_arm") || partName.equals("left_arm") || partName.equals("right_left") ||
                        partName.equals("left_leg") || partName.equals("body") || partName.equals("head")) {
                    size += partSize(boneAnimation, version);
                }
            }
        } else {
            size += 4;
            for (BoneAnimation entry : animation.boneAnimations()) {
                size += stringSize(entry.boneName()) + partSize(entry, version);
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

    private static int partSize(BoneAnimation part, int version){
        int size = 0;
        size += axisSize(part.positionKeyFrames().xKeyframes(), version);
        size += axisSize(part.positionKeyFrames().yKeyframes(), version);
        size += axisSize(part.positionKeyFrames().zKeyframes(), version);
        size += axisSize(part.rotationKeyFrames().xKeyframes(), version);
        size += axisSize(part.rotationKeyFrames().yKeyframes(), version);
        size += axisSize(part.rotationKeyFrames().zKeyframes(), version);
        String partName = part.boneName();
        if (!partName.equals("head")) {
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