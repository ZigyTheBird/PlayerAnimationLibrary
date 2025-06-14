package com.zigythebird.playeranimcore.enums;

public enum State {
    RUNNING(true),
    TRANSITIONING(false),
    PAUSED(true),
    STOPPED(false);

    public boolean isActive() {
        return isActive;
    }

    private final boolean isActive;

    State(boolean isActive) {
        this.isActive = isActive;
    }
}
