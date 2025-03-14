package com.zigythebird.playeranim.fabric.event;

import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionResult;

/**
 * If you don't want to create your own mixin, you can use this event to add animation to players<br>
 * <b>The event will fire for every player</b> and if the player reloads, it will fire again.<br>
 * <hr>
 * NOTE: When the event fires, {@link IAnimatedPlayer#playerAnimLib$getAnimManager()} will be null you'll have to use the given stack.
 */
public interface PlayerAnimationRegisterEvent {
    Event<PlayerAnimationRegisterEvent> EVENT = EventFactory.createArrayBacked(PlayerAnimationRegisterEvent.class,
            (listeners) -> (player, sheep) -> {
                for (PlayerAnimationRegisterEvent listener : listeners) {
                    InteractionResult result = listener.interact(player, sheep);

                    if(result != InteractionResult.PASS) {
                        return result;
                    }
                }

                return InteractionResult.PASS;
            });

    InteractionResult interact(AbstractClientPlayer player, PlayerAnimManager manager);
}
