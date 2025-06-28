package com.zigythebird.playeranimcore.enums;

public enum AnimationFormat {
    GECKOLIB,
    PLAYER_ANIMATOR;

    public int toInt() {
        return this == GECKOLIB ? 0 : 1;
    }

    public static AnimationFormat fromInt(int i) {
        return i == 0 ? GECKOLIB : PLAYER_ANIMATOR;
    }
}
