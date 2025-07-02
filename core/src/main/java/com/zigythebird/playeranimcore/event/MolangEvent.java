package com.zigythebird.playeranimcore.event;

import com.zigythebird.playeranimcore.animation.AnimationController;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.value.MutableObjectBinding;

/**
 * Register you own Molang queries and variables.
 */
public class MolangEvent {
    public static final Event<MolangEventInterface> MOLANG_EVENT = new Event<>(listeners -> (controller, engine, queryBinding) -> {
        for (MolangEventInterface listener : listeners) {
            listener.registerMolangQueries(controller, engine, queryBinding);
        }
    });

    @FunctionalInterface
    public interface MolangEventInterface {
        void registerMolangQueries(AnimationController controller, MochaEngine<AnimationController> engine, MutableObjectBinding queryBinding);
    }
}
