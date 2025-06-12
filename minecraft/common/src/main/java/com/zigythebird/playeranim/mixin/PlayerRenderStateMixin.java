package com.zigythebird.playeranim.mixin;

import com.zigythebird.playeranim.accessors.IPlayerAnimationState;
import com.zigythebird.playeranim.animation.AnimationProcessor;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerRenderState.class)
public class PlayerRenderStateMixin implements IPlayerAnimationState {
    @Unique
    boolean playerAnimLib$isCameraEntity = false;

    @Unique
    PlayerAnimManager playerAnimLib$playerAnimManager = null;

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
    public void playerAnimLib$setAnimManager(PlayerAnimManager manager) {
        this.playerAnimLib$playerAnimManager = manager;
    }

    @Override
    public @NotNull PlayerAnimManager playerAnimLib$getAnimManager() {
        return this.playerAnimLib$playerAnimManager;
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

