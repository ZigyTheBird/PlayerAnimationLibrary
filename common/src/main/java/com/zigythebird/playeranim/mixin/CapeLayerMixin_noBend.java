package com.zigythebird.playeranim.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zigythebird.playeranim.accessors.IPlayerAnimationState;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.cache.PlayerAnimBone;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin_noBend extends RenderLayer<PlayerRenderState, PlayerModel> {

    @Shadow @Final private HumanoidModel<PlayerRenderState> model;

    @Unique
    private final PlayerAnimBone playerAnimLib$capeAnimBone = new PlayerAnimBone(null, "cape");

    private CapeLayerMixin_noBend(RenderLayerParent<PlayerRenderState, PlayerModel> renderLayerParent, Void v) {
        super(renderLayerParent);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, PlayerRenderState playerRenderState, float f, float g, CallbackInfo ci) {
        if (model instanceof CapeLayerAccessor capeLayer) {
            PlayerAnimManager emote = ((IPlayerAnimationState)playerRenderState).playerAnimLib$getAnimManager();
            if (emote != null && emote.isActive()) {
                ModelPart torso = this.getParentModel().body;

                poseStack.translate(torso.x / 16, torso.y / 16, torso.z / 16);
                poseStack.mulPose((new Quaternionf()).rotateXYZ(torso.xRot, torso.yRot, torso.zRot));

                poseStack.translate(0.0F, 0.0F, 0.125F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180));

                ModelPart part = capeLayer.getCape();
                PartPose initialPose = part.getInitialPose();
                emote.get3DTransform(playerAnimLib$capeAnimBone);

                part.x = playerAnimLib$capeAnimBone.getPosX() + initialPose.x();
                part.y = playerAnimLib$capeAnimBone.getPosY() + initialPose.y();
                part.z = playerAnimLib$capeAnimBone.getPosZ() + initialPose.z();

                part.xRot = playerAnimLib$capeAnimBone.getRotX();
                part.yRot = playerAnimLib$capeAnimBone.getRotY();
                part.zRot = playerAnimLib$capeAnimBone.getRotZ();

                part.xScale = playerAnimLib$capeAnimBone.getScaleX();
                part.yScale = playerAnimLib$capeAnimBone.getScaleY();
                part.zScale = playerAnimLib$capeAnimBone.getScaleZ();
            }
        }
    }


    @WrapWithCondition(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private boolean translate(PoseStack instance, float f, float g, float h, @Local(argsOnly = true) PlayerRenderState playerRenderState) {
        PlayerAnimManager emote = ((IPlayerAnimationState)playerRenderState).playerAnimLib$getAnimManager();
        return emote == null || !emote.isActive();
    }
}
