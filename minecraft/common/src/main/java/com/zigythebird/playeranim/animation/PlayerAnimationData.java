package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranimcore.animation.AnimationData;
import net.minecraft.client.player.AbstractClientPlayer;

public class PlayerAnimationData extends AnimationData {
    private final AbstractClientPlayer player;

    public PlayerAnimationData(AbstractClientPlayer player, float velocity, float partialTick) {
        super(velocity, partialTick);
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
        return new PlayerAnimationData(getPlayer(), getVelocity(), getPartialTick());
    }
}
