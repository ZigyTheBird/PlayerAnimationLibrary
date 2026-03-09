package com.zigythebird.playeranim.mixin;

import com.zigythebird.playeranim.accessors.IAvatarAnimationState;
import com.zigythebird.playeranim.animation.AvatarAnimManager;
import com.zigythebird.playeranimcore.animation.AnimationData;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements IAvatarAnimationState {
    @Unique
    AvatarAnimManager playerAnimLib$avatarAnimManager = null;

    @Unique
    AnimationData playerAnimLib$data = null;

    @Override
    public boolean playerAnimLib$isFirstPersonPass() {
        return this.playerAnimLib$data.isFirstPersonPass();
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
    public void playerAnimLib$setAnimData(AnimationData data) {
        this.playerAnimLib$data = data;
    }

    @Override
    public AnimationData playerAnimLib$getAnimData() {
        return this.playerAnimLib$data;
    }
}

