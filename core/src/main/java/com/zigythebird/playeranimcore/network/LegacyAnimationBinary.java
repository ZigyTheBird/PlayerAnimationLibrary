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
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.parser.ast.FloatExpression;

import java.io.IOException;
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
        data.put("isEasingBefore", getBoolean(buf));
        data.put("nsfw", getBoolean(buf));
        int keyframeSize = buf.get();
        if (keyframeSize <= 0) throw new IOException("keyframe size must be greater than 0, current: " + keyframeSize);
        List<BoneAnimation> boneAnimations = new ArrayList<>();
        if (version >= 2) {
            int count = buf.getInt();
            for (int i = 0; i < count; i++) {
                String name = getString(buf);
                boneAnimations.add(readPart(buf, new BoneAnimation(name), version, keyframeSize));
            }
        } else {
            boneAnimations.add(readPart(buf, new BoneAnimation("head"), version, keyframeSize));
            boneAnimations.add(readPart(buf, new BoneAnimation("body"), version, keyframeSize));
            boneAnimations.add(readPart(buf, new BoneAnimation("right_arm"), version, keyframeSize));
            boneAnimations.add(readPart(buf, new BoneAnimation("left_arm"), version, keyframeSize));
            boneAnimations.add(readPart(buf, new BoneAnimation("right_left"), version, keyframeSize));
            boneAnimations.add(readPart(buf, new BoneAnimation("left_leg"), version, keyframeSize));

            BoneAnimation body = boneAnimations.get(1);
            if (body.bendKeyFrames().hasKeyframes()) {
                BoneAnimation torso = new BoneAnimation("torso");
                torso.bendKeyFrames().xKeyframes().addAll(body.bendKeyFrames().xKeyframes());
                torso.bendKeyFrames().yKeyframes().addAll(body.bendKeyFrames().yKeyframes());
                body.bendKeyFrames().xKeyframes().clear();
                body.bendKeyFrames().yKeyframes().clear();
                boneAnimations.add(torso);
            }

            boneAnimations.removeIf(boneAnimation -> !boneAnimation.hasKeyframes());
        }
        long msb = buf.getLong();
        long lsb = buf.getLong();
        data.put("uuid", new UUID(msb, lsb));

        return new Animation(data, stopTick, loopType, boneAnimations, UniversalAnimLoader.NO_KEYFRAMES, new HashMap<>(), new HashMap<>());
    }

    private static BoneAnimation readPart(ByteBuffer buf, BoneAnimation part, int version, int keyframeSize){
        readKeyframes(buf, part.positionKeyFrames().xKeyframes(), version, keyframeSize);
        readKeyframes(buf, part.positionKeyFrames().yKeyframes(), version, keyframeSize);
        readKeyframes(buf, part.positionKeyFrames().zKeyframes(), version, keyframeSize);
        readKeyframes(buf, part.rotationKeyFrames().xKeyframes(), version, keyframeSize);
        readKeyframes(buf, part.rotationKeyFrames().yKeyframes(), version, keyframeSize);
        readKeyframes(buf, part.rotationKeyFrames().zKeyframes(), version, keyframeSize);
        String partName = part.boneName();
        if (partName.equals("right_arm") || partName.equals("left_arm") || partName.equals("right_left") ||
                partName.equals("left_leg") || partName.equals("body")) {
            readKeyframes(buf, part.bendKeyFrames().xKeyframes(), version, keyframeSize);
            readKeyframes(buf, part.bendKeyFrames().yKeyframes(), version, keyframeSize);
        }
        if (version >= 3) {
            readKeyframes(buf, part.scaleKeyFrames().xKeyframes(), version, keyframeSize);
            readKeyframes(buf, part.scaleKeyFrames().yKeyframes(), version, keyframeSize);
            readKeyframes(buf, part.scaleKeyFrames().zKeyframes(), version, keyframeSize);
        }
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

    /**
     * Reads a bool value from byteBuffer
     * @param buf buf
     * @return    bool
     */
    public static boolean getBoolean(ByteBuffer buf) {
        return buf.get() != (byte) 0;
    }

    /**
     * Reads string from buf
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