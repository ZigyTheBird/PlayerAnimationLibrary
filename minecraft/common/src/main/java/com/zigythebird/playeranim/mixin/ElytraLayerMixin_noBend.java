package com.zigythebird.playeranim.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zigythebird.playeranim.accessors.IPlayerAnimationState;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WingsLayer.class)
public abstract class ElytraLayerMixin_noBend<S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    private ElytraLayerMixin_noBend(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V"))
    private void inject(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S humanoidRenderState, float f, float g, CallbackInfo ci) {
        if (humanoidRenderState instanceof IPlayerAnimationState animationState) {
            PlayerAnimManager emote = animationState.playerAnimLib$getAnimManager();
            if (emote != null && emote.isActive()) {
                AnimationProcessor processor = animationState.playerAnimLib$getAnimProcessor();
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
                torso.mulPos(1, -1, 1);
                elytra.applyOtherBone(torso);
                poseStack.translate(elytra.getPosX() / 16, elytra.getPosY() / 16, elytra.getPosZ() / 16);
                poseStack.mulPose(new Quaternionf().rotateZYX(elytra.getRotZ(), elytra.getRotY(), elytra.getRotX()));
                poseStack.scale(elytra.getScaleX(), elytra.getScaleY(), elytra.getScaleZ());
            }
        }
    }
}
