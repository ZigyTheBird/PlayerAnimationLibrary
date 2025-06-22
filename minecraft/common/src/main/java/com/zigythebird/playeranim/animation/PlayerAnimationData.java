package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranimcore.animation.AnimationData;
import net.minecraft.client.player.AbstractClientPlayer;

public class PlayerAnimationData extends AnimationData {
    private final AbstractClientPlayer player;

    public PlayerAnimationData(AbstractClientPlayer player, float partialTick, float velocity, float animationTick) {
        super(partialTick, velocity, animationTick);
        this.player = player;
    }

    public PlayerAnimationData(AbstractClientPlayer player, float partialTick, float velocity) {
        super(partialTick, velocity);
        this.player = player;
    }

    /**
     * Gets the current player being animated
     */
    public AbstractClientPlayer getPlayer() {
        return this.player;
    }

    /**
     * Gets the current player animation manager
     */
    public PlayerAnimManager getPlayerAnimManager() {
        return getPlayer().playerAnimLib$getAnimManager();
    }

    @Override
    public PlayerAnimationData copy() {
        return new PlayerAnimationData(getPlayer(), getPartialTick(), getVelocity(), getAnimationTick());
    }
}
