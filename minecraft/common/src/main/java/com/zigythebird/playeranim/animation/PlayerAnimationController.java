package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.enums.State;
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
    protected void setAnimation(RawAnimation rawAnimation, float startAnimFrom) {
        if (rawAnimation == null || rawAnimation.getAnimationStages().isEmpty()) {
            stop();

            return;
        }

        if (this.needsAnimationReload || !rawAnimation.equals(this.currentRawAnimation)) {
            if (this.player != null) {
                Queue<AnimationProcessor.QueuedAnimation> animations = this.player.playerAnimLib$getAnimProcessor().buildAnimationQueue(rawAnimation);

                if (animations != null) {
                    this.animationQueue = animations;
                    this.currentRawAnimation = rawAnimation;
                    this.startAnimFrom = startAnimFrom;
                    this.shouldResetTick = true;
                    this.animationState = State.TRANSITIONING;
                    this.justStartedTransition = true;
                    this.needsAnimationReload = false;

                    return;
                }
            }

            stop();
        }
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
