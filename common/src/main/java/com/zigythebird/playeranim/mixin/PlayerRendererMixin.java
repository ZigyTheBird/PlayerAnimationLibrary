package com.zigythebird.playeranim.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.accessors.IPlayerAnimationState;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.animation.TransformType;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {

    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel entityModel, float f, Void v) {
        super(context, entityModel, f);
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V", at = @At("HEAD"))
    private void modifyRenderState(AbstractClientPlayer abstractClientPlayer, PlayerRenderState playerRenderState, float f, CallbackInfo ci) {
        PlayerAnimManager animation = ((IAnimatedPlayer) abstractClientPlayer).playerAnimLib$getAnimManager();
        animation.setTickDelta(f);

        ((IPlayerAnimationState)playerRenderState).playerAnimLib$setAnimProcessor(((IAnimatedPlayer) abstractClientPlayer).playerAnimLib$getAnimProcessor());
        ((IPlayerAnimationState)playerRenderState).playerAnimLib$setAnimManager(animation);
        ((IPlayerAnimationState)playerRenderState).playerAnimLib$setLocalPlayer(abstractClientPlayer.isLocalPlayer());
        ((IPlayerAnimationState)playerRenderState).playerAnimLib$setLocalPlayer(abstractClientPlayer == Minecraft.getInstance().cameraEntity);
    }

    @Inject(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V", at = @At("RETURN"))
    private void applyBodyTransforms(PlayerRenderState playerRenderState, PoseStack matrixStack, float f, float g, CallbackInfo ci){
        var animationPlayer = ((IPlayerAnimationState) playerRenderState).playerAnimLib$getAnimManager();
        if(animationPlayer != null && animationPlayer.isActive()){
            //These are additive properties
            Vec3f vec3e = animationPlayer.get3DTransform("body", TransformType.SCALE, animationPlayer.getTickDelta(),
                    new Vec3f(ModelPart.DEFAULT_SCALE, ModelPart.DEFAULT_SCALE, ModelPart.DEFAULT_SCALE)
            );
            matrixStack.scale(vec3e.getX(), vec3e.getY(), vec3e.getZ());
            Vec3f vec3d = animationPlayer.get3DTransform("body", TransformType.POSITION, animationPlayer.getTickDelta(), Vec3f.ZERO);
            matrixStack.translate(vec3d.getX(), vec3d.getY() + 0.7, vec3d.getZ());
            Vec3f vec3f = animationPlayer.get3DTransform("body", TransformType.ROTATION, animationPlayer.getTickDelta(), Vec3f.ZERO);
            matrixStack.mulPose(Axis.ZP.rotation(vec3f.getZ()));    //roll
            matrixStack.mulPose(Axis.YP.rotation(vec3f.getY()));    //pitch
            matrixStack.mulPose(Axis.XP.rotation(vec3f.getX()));    //yaw
            matrixStack.translate(0, - 0.7d, 0);
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V", at = @At("TAIL"))
    private void extractRenderState(AbstractClientPlayer abstractClientPlayer, PlayerRenderState playerRenderState, float f, CallbackInfo ci) {
        ((IPlayerAnimationState)playerRenderState).playerAnimLib$setCameraEntity(abstractClientPlayer == Minecraft.getInstance().cameraEntity);
    }
}
