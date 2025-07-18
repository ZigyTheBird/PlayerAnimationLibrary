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

        this.registerPlayerAnimBone("body");
        this.registerPlayerAnimBone("right_arm");
        this.registerPlayerAnimBone("left_arm");
        this.registerPlayerAnimBone("right_leg");
        this.registerPlayerAnimBone("left_leg");
        this.registerPlayerAnimBone("head");
        this.registerPlayerAnimBone("torso");
        this.registerPlayerAnimBone("right_item");
        this.registerPlayerAnimBone("left_item");
        this.registerPlayerAnimBone("cape");
        this.registerPlayerAnimBone("elytra");
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
