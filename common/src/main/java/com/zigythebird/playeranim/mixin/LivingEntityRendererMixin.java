package com.zigythebird.playeranim.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zigythebird.playeranim.accessors.IPlayerAnimationState;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonMode;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Shadow protected M model;

    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("TAIL"))
    private void render(EntityRenderState entityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (FirstPersonMode.isFirstPersonPass() && entityRenderState instanceof IPlayerAnimationState state
                && state.playerAnimLib$isCameraEntity()) {
            playerAnimLib$setAllPartsVisible(true);
        }
    }

    @Unique
    private void playerAnimLib$setAllPartsVisible(boolean visible) {
        PlayerModel model = (PlayerModel)this.model;

        model.head.visible = visible;
        model.body.visible = visible;
        model.leftLeg.visible = visible;
        model.rightLeg.visible = visible;
        model.rightArm.visible = visible;
        model.leftArm.visible = visible;
    }
}
