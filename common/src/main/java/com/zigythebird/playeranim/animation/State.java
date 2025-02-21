package com.zigythebird.playeranim.animation;

import lombok.Getter;

public enum State {
    RUNNING(true),
    TRANSITIONING(true),
    PAUSED(true),
    STOPPED(false);

    @Getter
    private final boolean isActive;

    State(boolean isActive) {
        this.isActive = isActive;
    }
}
