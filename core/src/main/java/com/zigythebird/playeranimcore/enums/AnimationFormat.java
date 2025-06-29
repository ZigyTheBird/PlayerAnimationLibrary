package com.zigythebird.playeranimcore.enums;

public enum AnimationFormat {
    GECKOLIB(0),
    PLAYER_ANIMATOR(1);

    public final byte id;

    AnimationFormat(int id) {
        this.id = (byte) id;
    }

    public static AnimationFormat fromId(byte i) {
        return i == 0 ? GECKOLIB : PLAYER_ANIMATOR;
    }
}
