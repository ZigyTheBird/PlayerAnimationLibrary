package com.zigythebird.playeranim.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.accessors.IPlayerAnimationState;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.animation.TransformType;
import com.zigythebird.playeranim.cache.PlayerAnimBone;
import com.zigythebird.playeranim.math.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {
    @Unique
    private final PlayerAnimBone playerAnimLib$bodyAnimBone = new PlayerAnimBone(null, "body");
    
    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel entityModel, float f, Void v) {
        super(context, entityModel, f);
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V", at = @At("HEAD"))
    private void modifyRenderState(AbstractClientPlayer abstractClientPlayer, PlayerRenderState playerRenderState, float f, CallbackInfo ci) {
        PlayerAnimManager animation = abstractClientPlayer.playerAnimLib$getAnimManager();
        animation.setTickDelta(f);

        ((IPlayerAnimationState)playerRenderState).playerAnimLib$setAnimProcessor(abstractClientPlayer.playerAnimLib$getAnimProcessor());
        ((IPlayerAnimationState)playerRenderState).playerAnimLib$setAnimManager(animation);
        ((IPlayerAnimationState)playerRenderState).playerAnimLib$setLocalPlayer(abstractClientPlayer.isLocalPlayer());
        ((IPlayerAnimationState)playerRenderState).playerAnimLib$setLocalPlayer(abstractClientPlayer == Minecraft.getInstance().cameraEntity);
    }

    @Inject(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V", at = @At("RETURN"))
    private void applyBodyTransforms(PlayerRenderState playerRenderState, PoseStack matrices, float f, float g, CallbackInfo ci){
        var animationPlayer = ((IPlayerAnimationState)playerRenderState).playerAnimLib$getAnimManager();
        if(animationPlayer != null && animationPlayer.isActive()){
            //These are additive properties
            animationPlayer.get3DTransform(playerAnimLib$bodyAnimBone);
            
            matrices.scale(playerAnimLib$bodyAnimBone.getScaleX(), playerAnimLib$bodyAnimBone.getScaleY(), playerAnimLib$bodyAnimBone.getScaleZ());
            matrices.translate(playerAnimLib$bodyAnimBone.getPosX()/16, playerAnimLib$bodyAnimBone.getPosY()/16, playerAnimLib$bodyAnimBone.getPosZ()/16);

            matrices.mulPose(Axis.ZP.rotation(playerAnimLib$bodyAnimBone.getRotZ()));    //roll
            matrices.mulPose(Axis.YP.rotation(playerAnimLib$bodyAnimBone.getRotY()));    //pitch
            matrices.mulPose(Axis.XP.rotation(playerAnimLib$bodyAnimBone.getRotX()));    //yaw
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V", at = @At("TAIL"))
    private void extractRenderState(AbstractClientPlayer abstractClientPlayer, PlayerRenderState playerRenderState, float f, CallbackInfo ci) {
        ((IPlayerAnimationState)playerRenderState).playerAnimLib$setCameraEntity(abstractClientPlayer == Minecraft.getInstance().cameraEntity);
    }
}
