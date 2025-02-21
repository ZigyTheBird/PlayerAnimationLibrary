package com.zigythebird.playeranim.event;

import gg.moonflower.molangcompiler.api.MolangRuntime;

/**
 * Register you own Molang queries and variables.
 */
public class MolangEvent {
    public static final Event<MolangEventInterface> MOLANG_EVENT = new Event<>(MolangEventInterface.class, listeners -> (builder) -> {
        for (MolangEventInterface listener : listeners) {
            listener.registerMolangQueries(builder);
        }
    });

    @FunctionalInterface
    public interface MolangEventInterface {
        void registerMolangQueries(MolangRuntime.Builder builder);
    }
}
