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
import com.mojang.blaze3d.vertex.QuadInstance;
import com.zigythebird.playeranim.accessors.IAvatarAnimationState;
import com.zigythebird.playeranim.animation.AvatarAnimManager;
import com.zigythebird.playeranim.animation.MinecraftModel;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.util.RenderUtil;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.layered.AnimationContainer;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.animation.layered.ModifierLayer;
import com.zigythebird.playeranimcore.bindings.PlatformModel;
import com.zigythebird.playeranimcore.bones.CustomBone;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<S extends LivingEntityRenderState> {
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;scale(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void doTranslations(S livingEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (livingEntityRenderState instanceof IAvatarAnimationState avatarRenderState) {
            var animationPlayer = avatarRenderState.playerAnimLib$getAnimManager();
            if (animationPlayer != null && animationPlayer.isActive()) {
                avatarRenderState.playerAnimLib$getAnimManager().handleAnimations(animationPlayer.getTickDelta(), false, avatarRenderState.playerAnimLib$isFirstPersonPass());
                poseStack.scale(-1.0F, -1.0F, 1.0F);

                //These are additive properties
                PlayerAnimBone body = animationPlayer.get3DTransform("body");

                poseStack.translate(-body.position.x/16, body.position.y/16 + 0.75, body.position.z/16);
                body.rotation.x *= -1;
                body.rotation.y *= -1;
                RenderUtil.rotateMatrixAroundBone(poseStack, body);
                poseStack.scale(body.scale.x, body.scale.y, body.scale.z);

                poseStack.translate(0, -0.75, 0);

                poseStack.scale(-1.0F, -1.0F, 1.0F);
            }
        }
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
            )
    )
    public void pal$renderCustomModels(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        if (!(state instanceof IAvatarAnimationState avatarRenderState)) return;
        AvatarAnimManager animationPlayer = avatarRenderState.playerAnimLib$getAnimManager();
        if (animationPlayer == null || !animationPlayer.isActive()) return;

        int lightCoords = state.lightCoords;

        for (Pair<Integer, IAnimation> layer : animationPlayer.getLayers()) {
            AnimationController controller = pal$unwrapController(layer.right());
            if (controller == null || !controller.isActive()) continue;

            for (CustomBone bone : controller.getPivotBones().values()) {
                PlatformModel platformModel = bone.getModel();
                if (!(platformModel instanceof MinecraftModel mcModel)) continue;

                BlockModelPart bakedPart = mcModel.getBakedModel();
                if (bakedPart == null) continue;

                boolean translucent = bakedPart instanceof net.minecraft.client.renderer.block.model.SimpleModelWrapper smw && smw.hasTranslucency();

                poseStack.pushPose();
                controller.get3DTransform(bone);
                RenderUtil.translateMatrixToBone(poseStack, bone);

                submitNodeCollector.submitCustomGeometry(
                        poseStack,
                        translucent ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet(),
                        (pose, buffer) -> {
                            QuadInstance instance = new QuadInstance();
                            instance.setLightCoords(lightCoords);
                            instance.setOverlayCoords(OverlayTexture.NO_OVERLAY);

                            for (BakedQuad quad : bakedPart.getQuads(null)) {
                                buffer.putBakedQuad(pose, quad, instance);
                            }
                            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                                for (BakedQuad quad : bakedPart.getQuads(dir)) {
                                    buffer.putBakedQuad(pose, quad, instance);
                                }
                            }
                        }
                );

                poseStack.popPose();
            }
        }
    }

    private static AnimationController pal$unwrapController(IAnimation anim) {
        while (true) {
            if (anim instanceof AnimationController controller) return controller;
            if (anim instanceof ModifierLayer<?> ml) anim = ml.getAnimation();
            else if (anim instanceof AnimationContainer<?> ac) anim = ac.getAnim();
            else return null;
            if (anim == null) return null;
        }
    }
}
