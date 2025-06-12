/*
 * MIT License
 *
 * Copyright (c) 2022 KosmX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.zigythebird.playeranim.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zigythebird.playeranim.accessors.IPlayerAnimationState;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranim.bones.PlayerAnimBone;
import com.zigythebird.playeranim.util.RenderUtil;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Shadow protected M model;

    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("TAIL"))
    private void render(S entityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (FirstPersonMode.isFirstPersonPass() && entityRenderState instanceof IPlayerAnimationState state
                && state.playerAnimLib$isCameraEntity()) {
            playerAnimLib$setAllPartsVisible(true);
        }
    }
    
    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;isBodyVisible(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)Z"))
    private void doTranslations(S livingEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (livingEntityRenderState instanceof PlayerRenderState playerRenderState) {
            var animationPlayer = ((IPlayerAnimationState)playerRenderState).playerAnimLib$getAnimManager();
            if (animationPlayer != null && animationPlayer.isActive()) {
                poseStack.translate(0.0F, 1.501F, 0.0F);
                poseStack.scale(-1.0F, -1.0F, 1.0F);

                PlayerAnimBone body = ((IPlayerAnimationState)playerRenderState).playerAnimLib$getAnimProcessor().getBone("body");
                body.setToInitialPose();

                //These are additive properties
                body = animationPlayer.get3DTransform(body);

                poseStack.scale(body.getScaleX(), body.getScaleY(), body.getScaleZ());
                poseStack.translate(body.getPosX()/16, body.getPosY()/16 + 0.75, body.getPosZ()/16);

                RenderUtil.rotateMatrixAroundBone(poseStack, body);

                poseStack.translate(0, -0.75, 0);

                poseStack.scale(-1.0F, -1.0F, 1.0F);
                poseStack.translate(0.0F, -1.501F, 0.0F);
            }
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
