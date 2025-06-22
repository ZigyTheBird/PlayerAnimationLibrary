package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.Queue;

public class PlayerAnimationController extends AnimationController {
    protected final AbstractClientPlayer player;

    /**
     * Instantiates a new {@code AnimationController}
     *
     * @param player           The object that will be animated by this controller
     * @param animationHandler The {@link AnimationStateHandler} animation state handler responsible for deciding which animations to play
     */
    public PlayerAnimationController(AbstractClientPlayer player, AnimationStateHandler animationHandler) {
        super(animationHandler);
        this.player = player;
    }

    public AbstractClientPlayer getPlayer() {
        return this.player;
    }

    @Override
    protected Queue<AnimationProcessor.QueuedAnimation> getQueuedAnimations(RawAnimation rawAnimation) {
        if (player == null) return null;
        return this.player.playerAnimLib$getAnimProcessor().buildAnimationQueue(rawAnimation);
    }

    @Override
    protected void internalSetupAnim(AnimationData state) {
        if (state instanceof PlayerAnimationData playerAnimationData) {
            this.isJustStarting = playerAnimationData.getPlayerAnimManager().isFirstTick();
            this.process(state, playerAnimationData.getPlayer().playerAnimLib$getAnimProcessor().animTime, false);
        }
        super.internalSetupAnim(state);
    }
}
