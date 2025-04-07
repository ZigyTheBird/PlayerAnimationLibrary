package com.zigythebird.playeranim.enums;

import lombok.Getter;

public enum State {
    RUNNING(true),
    TRANSITIONING(false),
    PAUSED(true),
    STOPPED(false);

    @Getter
    private final boolean isActive;

    State(boolean isActive) {
        this.isActive = isActive;
    }
}
