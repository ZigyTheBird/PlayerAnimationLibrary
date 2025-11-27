package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import com.zigythebird.playeranimcore.animation.layered.AnimationStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.phys.Vec3;

public class PlayerAnimationProcessor extends AnimationProcessor {
    private final AbstractClientPlayer player;

    /**
     * Each AnimationProcessor must be bound to a player
     *
     * @param player The player to whom this processor is bound
     */
    public PlayerAnimationProcessor(AbstractClientPlayer player) {
        super();
        this.player = player;
    }

    @Override
    public void tickAnimation(AnimationStack stack, AnimationData state) {
        super.tickAnimation(stack, state);

        if (stack instanceof PlayerAnimManager playerAnimManager) {
            playerAnimManager.finishFirstTick();
        }
    }

    @Override
    public void handleAnimations(float partialTick, boolean fullTick) {
        Vec3 velocity = player.getDeltaMovement();

        PlayerAnimManager animatableManager = player.playerAnimLib$getAnimManager();
        int currentTick = player.tickCount;

        float currentFrameTime = currentTick + partialTick;

        AnimationData animationData = new PlayerAnimationData(player, (float) ((Math.abs(velocity.x) + Math.abs(velocity.z)) / 2f), partialTick);

        if (fullTick) animatableManager.tick(animationData.copy());

        if (!animatableManager.isFirstTick() && currentFrameTime == animatableManager.getLastUpdateTime())
            return;

        if (!Minecraft.getInstance().isPaused()) {
            animatableManager.updatedAt(currentFrameTime);
        }

        this.tickAnimation(animatableManager, animationData);
    }

    public AbstractClientPlayer getPlayer() {
        return this.player;
    }
}
