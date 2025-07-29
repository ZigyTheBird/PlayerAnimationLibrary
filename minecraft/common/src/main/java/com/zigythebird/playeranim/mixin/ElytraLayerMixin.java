package com.zigythebird.playeranim.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zigythebird.playeranim.accessors.IPlayerAnimationState;
import com.zigythebird.playeranim.accessors.IPlayerRenderer;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.util.RenderUtil;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WingsLayer.class)
public abstract class ElytraLayerMixin<S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    @Unique
    private final PlayerAnimBone pal$bone = new PlayerAnimBone("elytra");

    private ElytraLayerMixin(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/resources/ResourceLocation;)V"))
    private void inject(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, S humanoidRenderState, float f, float g, CallbackInfo ci) {
        if (humanoidRenderState instanceof IPlayerAnimationState animationState) {
            PlayerAnimManager emote = animationState.playerAnimLib$getAnimManager();
            if (emote != null && emote.isActive() && this.renderer instanceof PlayerRenderer playerRenderer) {
                ModelPart torso = playerRenderer.getModel().body;
                pal$bone.setToInitialPose();
                emote.get3DTransform(pal$bone);
                RenderUtil.applyVanillaPart(((IPlayerRenderer)renderer).pal$getCape(), pal$bone);
                pal$bone.mulPos(-1);
                pal$bone.mulRot(-1, 1, -1);
                RenderUtil.applyVanillaPart(torso, pal$bone);
                RenderUtil.translateMatrixToBone(poseStack, pal$bone);
            }
        }
    }
}
