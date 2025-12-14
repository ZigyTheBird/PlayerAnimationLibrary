package com.zigythebird.playeranim.mixin;

import com.zigythebird.playeranim.accessors.IPlayerAnimationState;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerRenderState.class)
public class PlayerRenderStateMixin implements IPlayerAnimationState {
    @Unique
    boolean playerAnimLib$isCameraEntity = false;

    @Unique
    boolean playerAnimLib$isFirstPersonPass = false;

    @Unique
    PlayerAnimManager playerAnimLib$playerAnimManager = null;

    @Unique
    AnimationProcessor playerAnimLib$playerAnimProcessor = null;

    @Override
    @SuppressWarnings("removal")
    public boolean playerAnimLib$isCameraEntity() {
        return this.playerAnimLib$isCameraEntity;
    }

    @Override
    public void playerAnimLib$setCameraEntity(boolean value) {
        this.playerAnimLib$isCameraEntity = value;
    }

    @Override
    public boolean playerAnimLib$isFirstPersonPass() {
        return playerAnimLib$isFirstPersonPass;
    }

    @Override
    public void playerAnimLib$setFirstPersonPass(boolean value) {
        playerAnimLib$isFirstPersonPass = value;
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

