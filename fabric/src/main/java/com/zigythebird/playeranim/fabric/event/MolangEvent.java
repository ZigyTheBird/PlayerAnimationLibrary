package com.zigythebird.playeranim.fabric.event;

import com.zigythebird.playeranim.animation.AnimationController;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.runtime.value.MutableObjectBinding;

/**
 * Register you own Molang queries and variables.
 */
public interface MolangEvent {
    Event<MolangEvent> EVENT = EventFactory.createArrayBacked(MolangEvent.class,
            (listeners) -> (player, engine, q) -> {
                for (MolangEvent listener : listeners) {
                    InteractionResult result = listener.interact(player, engine, q);

                    if(result != InteractionResult.PASS) {
                        return result;
                    }
                }

                return InteractionResult.PASS;
            });

    InteractionResult interact(AnimationController controller, MochaEngine<AnimationController> engine, MutableObjectBinding queryBinding);
}
