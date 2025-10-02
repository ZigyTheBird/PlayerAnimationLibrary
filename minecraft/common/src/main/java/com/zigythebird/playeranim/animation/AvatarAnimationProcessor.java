package com.zigythebird.playeranim.animation;

import com.zigythebird.playeranim.accessors.IAnimatedAvatar;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import com.zigythebird.playeranimcore.animation.layered.AnimationStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.phys.Vec3;

public class AvatarAnimationProcessor extends AnimationProcessor {
    private final Avatar avatar;

    /**
     * Each AnimationProcessor must be bound to a player
     *
     * @param avatar The player to whom this processor is bound
     */
    public AvatarAnimationProcessor(Avatar avatar) {
        super();
        this.avatar = avatar;
    }

    @Override
    public void tickAnimation(AnimationStack stack, AnimationData state) {
        super.tickAnimation(stack, state);

        if (stack instanceof AvatarAnimManager avatarAnimManager) {
            avatarAnimManager.finishFirstTick();
        }
    }

    @Override
    public void handleAnimations(float partialTick, boolean fullTick) {
        Vec3 velocity = avatar.getDeltaMovement();

        AvatarAnimManager animatableManager = ((IAnimatedAvatar)avatar).playerAnimLib$getAnimManager();
        int currentTick = avatar.tickCount;

        float currentFrameTime = currentTick + partialTick;

        AnimationData animationData = new AvatarAnimationData(avatar, (float) ((Math.abs(velocity.x) + Math.abs(velocity.z)) / 2f), partialTick);

        if (fullTick) animatableManager.tick(animationData.copy());

        if (!animatableManager.isFirstTick() && currentFrameTime == animatableManager.getLastUpdateTime())
            return;

        if (!Minecraft.getInstance().isPaused()) {
            animatableManager.updatedAt(currentFrameTime);
        }

        this.tickAnimation(animatableManager, animationData);
    }

    public Avatar getAvatar() {
        return this.avatar;
    }
}
