package com.zigythebird.playeranim.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zigythebird.playeranim.accessors.IAnimatedAvatar;
import com.zigythebird.playeranim.accessors.IAvatarAnimationState;
import com.zigythebird.playeranim.accessors.ILevelRenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow protected abstract EntityRenderState extractEntity(Entity entity, float partialTick);

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "extractVisibleEntities", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private void doNotTickIfRendering(Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState renderState, CallbackInfo ci, @Local Entity entity) {
        //When the method is called it will return false from now
        //So we won't do an extra tick when the renderer is already about to do one
        if (entity instanceof IAnimatedAvatar animatedAvatar) animatedAvatar.playerAnimLib$isAwaitingTick();
    }

    @Inject(method = "extractVisibleEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z"))
    private void tickIfNotRendering(Camera camera, Frustum frustum, DeltaTracker deltaTracker, LevelRenderState renderState, CallbackInfo ci, @Local Entity entity) {
        if (entity instanceof IAnimatedAvatar animatedAvatar && animatedAvatar.playerAnimLib$isAwaitingTick()
                && renderState instanceof ILevelRenderState levelRenderState) {
            TickRateManager tickRateManager = this.minecraft.level.tickRateManager();
            if (this.extractEntity(entity, deltaTracker.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(entity)))
                    instanceof IAvatarAnimationState avatarAnimationState)
                levelRenderState.playerAnimLib$getAnimatedAvatarsToTick().add(avatarAnimationState);
        }
    }

    @Inject(method = "submitEntities", at = @At(value = "HEAD"))
    private void tickAnimatedAvatars(PoseStack poseStack, LevelRenderState renderState, SubmitNodeCollector nodeCollector, CallbackInfo ci) {
        if (renderState instanceof ILevelRenderState levelRenderState) {
            for (IAvatarAnimationState state : levelRenderState.playerAnimLib$getAnimatedAvatarsToTick()) {
                state.playerAnimLib$getAnimManager().handleAnimations(state, false);
            }
        }
    }
}
