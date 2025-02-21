package com.zigythebird.playeranim.api;

import com.zigythebird.playeranim.animation.AnimationState;
import com.zigythebird.playeranim.event.Event;

public class PlayerAnimationEvents {
    public static final Event<ApplyMolangQueries> APPLY_MOLANG_QUERIES = new Event<>(ApplyMolangQueries.class, listeners -> (state, animTime) -> {
        for (ApplyMolangQueries listener : listeners) {
            listener.applyMolangQueries(state, animTime);
        }
    });

    @FunctionalInterface
    public interface ApplyMolangQueries {
        void applyMolangQueries(AnimationState state, double animTime);
    }
}
