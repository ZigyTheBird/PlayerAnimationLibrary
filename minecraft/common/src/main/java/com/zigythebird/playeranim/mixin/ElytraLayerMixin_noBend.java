package com.zigythebird.playeranim.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.util.RenderUtil;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraLayer.class)
public abstract class ElytraLayerMixin_noBend<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public ElytraLayerMixin_noBend(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ElytraModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void inject(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (livingEntity instanceof AbstractClientPlayer player) {
            PlayerAnimManager emote = ((IAnimatedPlayer)player).playerAnimLib$getAnimManager();
            if (emote != null && emote.isActive()) {
                AnimationProcessor processor = ((IAnimatedPlayer)player).playerAnimLib$getAnimProcessor();
                PlayerAnimBone torso = processor.getBone("torso");
                PlayerAnimBone cape = processor.getBone("cape");
                PlayerAnimBone elytra = processor.getBone("elytra");
                torso.setToInitialPose();
                cape.setToInitialPose();
                elytra.setToInitialPose();
                emote.get3DTransform(torso);
                emote.get3DTransform(cape);
                emote.get3DTransform(elytra);
                elytra.applyOtherBone(cape);
                elytra.mulPos(-1);
                elytra.mulRot(-1, 1, -1);
                torso.positionY *= -1;
                elytra.applyOtherBone(torso);
                RenderUtil.translateMatrixToBone(poseStack, elytra);
            }
        }
    }
}
