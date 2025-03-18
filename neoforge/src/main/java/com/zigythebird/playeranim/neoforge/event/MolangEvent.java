package com.zigythebird.playeranim.neoforge.event;

import com.zigythebird.playeranim.animation.AnimationController;
import com.zigythebird.playeranim.math.MolangLoader;
import net.neoforged.bus.api.Event;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.value.MutableObjectBinding;

import java.util.function.Function;

/**
 * Register you own Molang queries and variables.
 */
public class MolangEvent extends Event {
    private final AnimationController controller;
    private final MochaEngine<AnimationController> engine;
    private final MutableObjectBinding queryBinding;

    public MolangEvent(AnimationController controller, MochaEngine<AnimationController> engine, MutableObjectBinding queryBinding) {
        this.controller = controller;
        this.engine = engine;
        this.queryBinding = queryBinding;
    }

    public AnimationController getAnimationController() {
        return this.controller;
    }

    public MochaEngine<AnimationController> getRuntimeBuilder() {
        return this.engine;
    }

    public boolean setFloatQuery(String name, Function<AnimationController, Double> value) {
        return MolangLoader.setDoubleQuery(this.queryBinding, name, value);
    }

    public boolean setBoolQuery(String name, Function<AnimationController, Boolean> value) {
        return MolangLoader.setBoolQuery(this.queryBinding, name, value);
    }
}
