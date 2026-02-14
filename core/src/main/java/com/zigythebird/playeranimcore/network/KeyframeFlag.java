package com.zigythebird.playeranimcore.network;

import com.zigythebird.playeranimcore.easing.EasingType;

enum KeyframeFlag {
    IS_CONSTANT,
    HAS_EASING_ARGS,
    LENGTH_ZERO,
    LENGTH_ONE;

    static final int EASING_BITS = 6;
    static final int EASING_MASK = (1 << EASING_BITS) - 1;

    final int mask = 1 << ordinal();

    static int pack(int easingId, int flags) {
        return (flags << EASING_BITS) | easingId;
    }

    static int unpackEasing(int combined) {
        return combined & EASING_MASK;
    }

    static int unpackFlags(int combined) {
        return combined >>> EASING_BITS;
    }

    static {
        for (EasingType type : EasingType.values()) {
            if (type.id < 0 || type.id > EASING_MASK)
                throw new AssertionError("EasingType." + type.name() + " id " + type.id + " exceeds " + EASING_BITS + "-bit limit (" + EASING_MASK + ")");
        }
        for (KeyframeFlag flag : values()) {
            if (flag.ordinal() + EASING_BITS >= Integer.SIZE)
                throw new AssertionError("KeyframeFlag." + flag.name() + " ordinal " + flag.ordinal() + " would overflow int with EASING_BITS=" + EASING_BITS);
        }
    }
}
