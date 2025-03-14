package com.zigythebird.playeranim.neoforge.event;

import com.zigythebird.playeranim.animation.AnimationController;
import com.zigythebird.playeranim.math.MolangParser;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import net.neoforged.bus.api.Event;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Register you own Molang queries and variables.
 */
public class MolangEvent extends Event {
    private final AnimationController controller;
    private final MolangRuntime.Builder builder;

    public MolangEvent(AnimationController controller, MolangRuntime.Builder builder) {
        this.controller = controller;
        this.builder = builder;
    }

    public AnimationController getAnimationController() {
        return this.controller;
    }

    public MolangRuntime.Builder getRuntimeBuilder() {
        return this.builder;
    }

    public MolangRuntime.Builder setFloatQuery(String name, Supplier<Float> value) {
        return MolangParser.setFloatQuery(this.builder, name, value);
    }

    public MolangRuntime.Builder setBoolQuery(String name, BooleanSupplier value) {
        return MolangParser.setBoolQuery(this.builder, name, value);
    }
}
