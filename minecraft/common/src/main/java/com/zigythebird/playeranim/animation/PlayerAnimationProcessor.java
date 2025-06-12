package com.zigythebird.playeranim.animation;

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
