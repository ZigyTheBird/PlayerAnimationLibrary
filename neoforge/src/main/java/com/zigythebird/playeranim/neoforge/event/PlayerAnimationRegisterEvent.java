package com.zigythebird.playeranim.neoforge.event;

import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import net.minecraft.client.player.AbstractClientPlayer;
import net.neoforged.bus.api.Event;

/**
 * If you don't want to create your own mixin, you can use this event to add animation to players<br>
 * <b>The event will fire for every player</b> and if the player reloads, it will fire again.<br>
 * <hr>
 * NOTE: When the event fires, {@link IAnimatedPlayer#playerAnimLib$getAnimManager()} will be null you'll have to use the given stack.
 */
public class PlayerAnimationRegisterEvent extends Event {
    private final AbstractClientPlayer player;
    private final PlayerAnimManager manager;

    /**
     * Player object is in construction, it will be invoked when you can register animation
     * It will be invoked for every player only ONCE
     */
    public PlayerAnimationRegisterEvent(AbstractClientPlayer player, PlayerAnimManager manager) {
        this.player = player;
        this.manager = manager;
    }

    public AbstractClientPlayer getClientPlayer() {
        return this.player;
    }

    public PlayerAnimManager getAnimManager() {
        return this.manager;
    }
}
