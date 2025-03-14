package com.zigythebird.playeranim.fabric.event;

import com.zigythebird.playeranim.animation.AnimationController;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;

/**
 * Register you own Molang queries and variables.
 */
public interface MolangEvent {
    Event<MolangEvent> EVENT = EventFactory.createArrayBacked(MolangEvent.class,
            (listeners) -> (player, sheep) -> {
                for (MolangEvent listener : listeners) {
                    InteractionResult result = listener.interact(player, sheep);

                    if(result != InteractionResult.PASS) {
                        return result;
                    }
                }

                return InteractionResult.PASS;
            });

    InteractionResult interact(AnimationController controller, MolangRuntime.Builder builder);
}
