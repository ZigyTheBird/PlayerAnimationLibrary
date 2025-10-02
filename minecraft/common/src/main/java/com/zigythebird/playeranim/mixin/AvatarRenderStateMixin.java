package com.zigythebird.playeranim.mixin;

import com.zigythebird.playeranim.accessors.IAvatarAnimationState;
import com.zigythebird.playeranim.animation.AvatarAnimManager;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements IAvatarAnimationState {
    @Unique
    boolean playerAnimLib$isCameraEntity = false;

    @Unique
    AvatarAnimManager playerAnimLib$avatarAnimManager = null;

    @Unique
    AnimationProcessor playerAnimLib$playerAnimProcessor = null;

    @Override
    public boolean playerAnimLib$isCameraEntity() {
        return playerAnimLib$isCameraEntity;
    }

    @Override
    public void playerAnimLib$setCameraEntity(boolean value) {
        playerAnimLib$isCameraEntity = value;
    }

    @Override
    public void playerAnimLib$setAnimManager(AvatarAnimManager manager) {
        this.playerAnimLib$avatarAnimManager = manager;
    }

    @Override
    public @NotNull AvatarAnimManager playerAnimLib$getAnimManager() {
        return this.playerAnimLib$avatarAnimManager;
    }

    @Override
    public void playerAnimLib$setAnimProcessor(AnimationProcessor processor) {
        this.playerAnimLib$playerAnimProcessor = processor;
    }

    @Override
    public AnimationProcessor playerAnimLib$getAnimProcessor() {
        return this.playerAnimLib$playerAnimProcessor;
    }
}

