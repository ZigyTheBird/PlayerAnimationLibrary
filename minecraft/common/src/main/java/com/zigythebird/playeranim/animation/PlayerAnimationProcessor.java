package com.zigythebird.playeranim.animation;

import com.zigythebird.mcanimcore.animation.AnimationData;
import com.zigythebird.mcanimcore.animation.AnimationProcessor;
import com.zigythebird.mcanimcore.animation.layered.AnimationStack;
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

        this.registerAnimBone("body");
        this.registerAnimBone("right_arm");
        this.registerAnimBone("left_arm");
        this.registerAnimBone("right_leg");
        this.registerAnimBone("left_leg");
        this.registerAnimBone("head");
        this.registerAnimBone("torso");
        this.registerAnimBone("right_item");
        this.registerAnimBone("left_item");
        this.registerAnimBone("cape");
        this.registerAnimBone("elytra");
    }

    @Override
    public void tickAnimation(AnimationStack AnimManager, AnimationData state) {
        super.tickAnimation(AnimManager, state);

        if (AnimManager instanceof PlayerAnimManager playerAnimManager) {
            playerAnimManager.finishFirstTick();
        }
    }

    @Override
    public void handleAnimations(float partialTick, boolean fullTick) {
        Vec3 velocity = player.getDeltaMovement();
        AnimationData animationData = new PlayerAnimationData(player, partialTick, (float) ((Math.abs(velocity.x) + Math.abs(velocity.z)) / 2f));

        Minecraft mc = Minecraft.getInstance();
        PlayerAnimManager animatableManager = player.playerAnimLib$getAnimManager();
        int currentTick = player.tickCount;

        if (animatableManager.getFirstTickTime() == -1)
            animatableManager.startedAt(currentTick + partialTick);

        float currentFrameTime = currentTick + partialTick;
        boolean isReRender = !animatableManager.isFirstTick() && currentFrameTime == animatableManager.getLastUpdateTime();

        if (isReRender && player.getId() == this.lastRenderedInstance)
            return;

        if (!mc.isPaused()) {
            animatableManager.updatedAt(currentFrameTime);

            float lastUpdateTime = animatableManager.getLastUpdateTime();
            this.animTime += lastUpdateTime - this.lastGameTickTime;
            this.lastGameTickTime = lastUpdateTime;
        }

        animationData.setAnimationTick(this.animTime);
        this.lastRenderedInstance = player.getId();

        if (fullTick) player.playerAnimLib$getAnimManager().tick(animationData.copy());

        if (!this.getRegisteredBones().isEmpty())
            this.tickAnimation(animatableManager, animationData);
    }

    public AbstractClientPlayer getPlayer() {
        return this.player;
    }
}
