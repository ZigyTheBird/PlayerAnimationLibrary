package com.zigythebird.playeranimcore.network;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranimcore.animation.keyframe.Keyframe;
import com.zigythebird.playeranimcore.animation.keyframe.KeyframeStack;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.enums.AnimationFormat;
import com.zigythebird.playeranimcore.enums.Axis;
import com.zigythebird.playeranimcore.loading.PlayerAnimatorLoader;
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
import java.util.function.Function;

import static com.zigythebird.playeranimcore.molang.MolangLoader.MOCHA_ENGINE;

final class AnimationBinaryV6 {
    private static final int KF_IS_CONSTANT = 0x01;
    private static final int KF_HAS_EASING_ARGS = 0x02;
    private static final int KF_EASING_SHIFT = Integer.SIZE - Integer.numberOfLeadingZeros(KF_IS_CONSTANT | KF_HAS_EASING_ARGS | LengthEncoding.MASK);

    enum HeaderFlag {
        SHOULD_PLAY_AGAIN,
        HOLD_ON_LAST_FRAME,
        PLAYER_ANIMATOR,
        APPLY_BEND,
        EASE_BEFORE,
        HAS_BEGIN_TICK,
        HAS_END_TICK;

        final int mask = 1 << ordinal();

        boolean test(int flags) {
            return (flags & mask) != 0;
        }

        int set(int flags, boolean condition) {
            return condition ? flags | mask : flags;
        }
    }

    enum LengthEncoding {
        FLOAT(Float.NaN),
        ZERO(0.0f),
        ONE(1.0f);

        private static final int BIT_SHIFT = 2;
        private static final LengthEncoding[] VALUES = values();
        static final int MASK = (Integer.highestOneBit(VALUES.length - 1) * 2 - 1) << BIT_SHIFT;

        final int bits = ordinal() << BIT_SHIFT;
        final float value;

        LengthEncoding(float value) {
            this.value = value;
        }

        static LengthEncoding encode(float length) {
            if (length == 0.0f) return ZERO;
            if (length == 1.0f) return ONE;
            return FLOAT;
        }

        static LengthEncoding decode(int combined) {
            int index = (combined & MASK) >>> BIT_SHIFT;
            return index < VALUES.length ? VALUES[index] : FLOAT;
        }
    }

    enum BoneChannel {
        ROTATION_X(BoneAnimation::rotationKeyFrames, Axis.X, false),
        ROTATION_Y(BoneAnimation::rotationKeyFrames, Axis.Y, false),
        ROTATION_Z(BoneAnimation::rotationKeyFrames, Axis.Z, false),
        POSITION_X(BoneAnimation::positionKeyFrames, Axis.X, false),
        POSITION_Y(BoneAnimation::positionKeyFrames, Axis.Y, false),
        POSITION_Z(BoneAnimation::positionKeyFrames, Axis.Z, false),
        SCALE_X(BoneAnimation::scaleKeyFrames, Axis.X, true),
        SCALE_Y(BoneAnimation::scaleKeyFrames, Axis.Y, true),
        SCALE_Z(BoneAnimation::scaleKeyFrames, Axis.Z, true),
        BEND(null, null, false);

        static final BoneChannel[] VALUES = values();

        final int mask = 1 << ordinal();
        final Function<BoneAnimation, KeyframeStack> stackAccessor;
        final Axis axis;
        final boolean isScale;

        BoneChannel(Function<BoneAnimation, KeyframeStack> stackAccessor, Axis axis, boolean isScale) {
            this.stackAccessor = stackAccessor;
            this.axis = axis;
            this.isScale = isScale;
        }

        List<Keyframe> getKeyframes(BoneAnimation bone) {
            if (this == BEND) return bone.bendKeyFrames();
            return stackAccessor.apply(bone).getKeyFramesForAxis(axis);
        }
    }

    record HeaderResult(float length, Animation.LoopType loopType, AnimationFormat format) {}

    // ===== Write =====

    static void writeHeader(ByteBuf buf, Animation animation, Map<String, Object> data) {
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

    static void writeBoneAnimation(ByteBuf buf, BoneAnimation bone) {
        int presenceFlags = 0;
        for (BoneChannel ch : BoneChannel.VALUES) {
            if (!ch.getKeyframes(bone).isEmpty()) {
                presenceFlags |= ch.mask;
            }
        }
        VarIntUtils.writeVarInt(buf, presenceFlags);
        for (BoneChannel ch : BoneChannel.VALUES) {
            if ((presenceFlags & ch.mask) != 0) {
                writeKeyframeList(buf, ch.getKeyframes(bone));
            }
        }
    }

    // ===== Read =====

    static HeaderResult readHeader(ByteBuf buf, ExtraAnimationData data) {
        int flags = VarIntUtils.readVarInt(buf);
        boolean shouldPlayAgain = HeaderFlag.SHOULD_PLAY_AGAIN.test(flags);
        boolean isHoldOnLastFrame = HeaderFlag.HOLD_ON_LAST_FRAME.test(flags);
        AnimationFormat format = HeaderFlag.PLAYER_ANIMATOR.test(flags) ? AnimationFormat.PLAYER_ANIMATOR : AnimationFormat.GECKOLIB;

        data.put(ExtraAnimationData.FORMAT_KEY, format);
        if (HeaderFlag.APPLY_BEND.test(flags)) data.put(ExtraAnimationData.APPLY_BEND_TO_OTHER_BONES_KEY, true);
        if (!HeaderFlag.EASE_BEFORE.test(flags)) data.put(ExtraAnimationData.EASING_BEFORE_KEY, false);

        float length = buf.readFloat();

        Animation.LoopType loopType;
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

        return new HeaderResult(length, loopType, format);
    }

    @SuppressWarnings("unchecked")
    static BoneAnimation readBoneAnimation(ByteBuf buf, boolean shouldStartFromDefault) {
        int presenceFlags = VarIntUtils.readVarInt(buf);
        List<Keyframe>[] lists = new List[BoneChannel.VALUES.length];
        for (BoneChannel ch : BoneChannel.VALUES) {
            lists[ch.ordinal()] = (presenceFlags & ch.mask) != 0
                    ? readKeyframeList(buf, shouldStartFromDefault, ch.isScale)
                    : new ArrayList<>(0);
        }
        return new BoneAnimation(
                new KeyframeStack(lists[0], lists[1], lists[2]),
                new KeyframeStack(lists[3], lists[4], lists[5]),
                new KeyframeStack(lists[6], lists[7], lists[8]),
                lists[9]
        );
    }

    // ===== Private =====

    private static void writeKeyframeList(ByteBuf buf, List<Keyframe> keyframes) {
        VarIntUtils.writeVarInt(buf, keyframes.size());
        for (Keyframe keyframe : keyframes) {
            writeKeyframe(keyframe, buf);
        }
    }

    private static void writeKeyframe(Keyframe keyframe, ByteBuf buf) {
        List<Expression> endValue = keyframe.endValue();
        boolean isConstant = endValue.size() == 1 && IsConstantExpression.test(endValue.getFirst());
        boolean hasEasingArgs = false;
        for (List<Expression> inner : keyframe.easingArgs()) {
            if (!inner.isEmpty()) {
                hasEasingArgs = true;
                break;
            }
        }

        LengthEncoding lengthEnc = LengthEncoding.encode(keyframe.length());

        int combined = (keyframe.easingType().id << KF_EASING_SHIFT) | lengthEnc.bits;
        if (isConstant) combined |= KF_IS_CONSTANT;
        if (hasEasingArgs) combined |= KF_HAS_EASING_ARGS;

        VarIntUtils.writeVarInt(buf, combined);

        if (isConstant) {
            buf.writeFloat(MOCHA_ENGINE.eval(endValue));
        } else {
            ExprBytesUtils.writeExpressions(endValue, buf);
        }

        if (lengthEnc == LengthEncoding.FLOAT) {
            buf.writeFloat(keyframe.length());
        }

        if (hasEasingArgs) {
            ProtocolUtils.writeList(buf, keyframe.easingArgs(), ExprBytesUtils::writeExpressions);
        }
    }

    private static List<Keyframe> readKeyframeList(ByteBuf buf, boolean shouldStartFromDefault, boolean isScale) {
        int count = VarIntUtils.readVarInt(buf);
        List<Keyframe> list = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            int combined = VarIntUtils.readVarInt(buf);
            boolean isConstant = (combined & KF_IS_CONSTANT) != 0;
            boolean hasEasingArgs = (combined & KF_HAS_EASING_ARGS) != 0;
            int easingId = combined >>> KF_EASING_SHIFT;

            List<Expression> endValue;
            if (isConstant) {
                endValue = List.of(FloatExpression.of(buf.readFloat()));
            } else {
                endValue = ExprBytesUtils.readExpressions(buf);
            }

            LengthEncoding lengthEnc = LengthEncoding.decode(combined);
            float length = lengthEnc == LengthEncoding.FLOAT ? buf.readFloat() : lengthEnc.value;

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
}
