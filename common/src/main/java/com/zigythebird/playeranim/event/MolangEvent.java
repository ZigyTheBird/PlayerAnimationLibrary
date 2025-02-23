package com.zigythebird.playeranim.event;

import com.zigythebird.playeranim.animation.AnimationController;
import gg.moonflower.molangcompiler.api.MolangRuntime;

/**
 * Register you own Molang queries and variables.
 */
public class MolangEvent {
    public static final Event<MolangEventInterface> MOLANG_EVENT = new Event<>(MolangEventInterface.class, listeners -> (controller, builder) -> {
        for (MolangEventInterface listener : listeners) {
            listener.registerMolangQueries(controller, builder);
        }
    });

    @FunctionalInterface
    public interface MolangEventInterface {
        void registerMolangQueries(AnimationController controller, MolangRuntime.Builder builder);
    }
}
