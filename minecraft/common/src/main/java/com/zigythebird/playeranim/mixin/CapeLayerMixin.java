package com.zigythebird.playeranim.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zigythebird.playeranim.accessors.IAvatarAnimationState;
import com.zigythebird.playeranim.animation.AvatarAnimManager;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<AvatarRenderState, PlayerModel> {
    @Shadow
    @Final
    private HumanoidModel<AvatarRenderState> model;

    private CapeLayerMixin(RenderLayerParent<AvatarRenderState, PlayerModel> renderLayerParent, Void v) {
        super(renderLayerParent);
    }

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    private void render(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, AvatarRenderState avatarRenderState, float f, float g, CallbackInfo ci) {
        if (model instanceof CapeModelAccessor capeLayer) {
            AvatarAnimManager emote = ((IAvatarAnimationState)avatarRenderState).playerAnimLib$getAnimManager();
            if (emote != null && emote.isActive()) {
                ModelPart torso = this.getParentModel().body;

                torso.translateAndRotate(poseStack);

                poseStack.translate(0.0F, 0.0F, 0.125F);
            }
        }
    }
}
