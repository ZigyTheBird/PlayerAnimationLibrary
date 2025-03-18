package com.zigythebird.playeranim.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zigythebird.playeranim.accessors.IPlayerAnimationState;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.cache.PlayerAnimBone;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WingsLayer.class)
public abstract class ElytraLayerMixin_noBend<S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    @Unique
    private final PlayerAnimBone playerAnimLib$capeAnimBone = new PlayerAnimBone("cape");
    @Unique
    private final PlayerAnimBone playerAnimLib$elytraAnimBone = new PlayerAnimBone("elytra");
    @Unique
    private final PlayerAnimBone playerAnimLib$torsoAnimBone = new PlayerAnimBone("torso");

    private ElytraLayerMixin_noBend(RenderLayerParent<S, M> renderLayerParent, Void v) {
        super(renderLayerParent);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void inject(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S humanoidRenderState, float f, float g, CallbackInfo ci) {
        if (humanoidRenderState instanceof IPlayerAnimationState animationState) {
            PlayerAnimManager emote = animationState.playerAnimLib$getAnimManager();
            if (emote != null && emote.isActive()) {
                //Todo: Optimize this shit fr
                emote.get3DTransform(playerAnimLib$torsoAnimBone);
                playerAnimLib$capeAnimBone.copyOtherBone(playerAnimLib$torsoAnimBone);
                emote.get3DTransform(playerAnimLib$capeAnimBone);
                playerAnimLib$elytraAnimBone.copyOtherBone(playerAnimLib$capeAnimBone);
                playerAnimLib$elytraAnimBone.updateScale(1, 1, 1);
                emote.get3DTransform(playerAnimLib$elytraAnimBone);
                poseStack.translate(playerAnimLib$elytraAnimBone.getPosX() / 16, playerAnimLib$elytraAnimBone.getPosY() / 16, playerAnimLib$elytraAnimBone.getPosZ() / 16);
                poseStack.mulPose((new Quaternionf()).rotateXYZ(playerAnimLib$elytraAnimBone.getRotX(), playerAnimLib$elytraAnimBone.getRotY(), playerAnimLib$elytraAnimBone.getRotZ()));
                poseStack.scale(playerAnimLib$elytraAnimBone.getScaleX(), playerAnimLib$elytraAnimBone.getScaleY(), playerAnimLib$elytraAnimBone.getScaleZ());
            }
        }
    }
}
