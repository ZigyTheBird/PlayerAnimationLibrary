package com.zigythebird.playeranim.mixin;

import com.zigythebird.playeranim.accessors.IAvatarAnimationState;
import com.zigythebird.playeranim.animation.AvatarAnimManager;
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
}

