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

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zigythebird.playeranim.accessors.IMutableModel;
import com.zigythebird.playeranim.accessors.IPlayerAnimationState;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.util.RenderUtil;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(value = PlayerModel.class, priority = 2001)//Apply after NotEnoughAnimation's inject
public class PlayerModelMixin extends HumanoidModel<PlayerRenderState> {
    @Unique
    private final PlayerAnimBone pal$head = new PlayerAnimBone("head");
    @Unique
    private final PlayerAnimBone pal$torso = new PlayerAnimBone("torso");
    @Unique
    private final PlayerAnimBone pal$rightArm = new PlayerAnimBone("right_arm");
    @Unique
    private final PlayerAnimBone pal$leftArm = new PlayerAnimBone("left_arm");
    @Unique
    private final PlayerAnimBone pal$rightLeg = new PlayerAnimBone("right_leg");
    @Unique
    private final PlayerAnimBone pal$leftLeg = new PlayerAnimBone("left_leg");
    
    
    public PlayerModelMixin(ModelPart modelPart, Function<ResourceLocation, RenderType> function) {
        super(modelPart, function);
    }

    @Unique
    private void playerAnimLib$setToInitialPose() {
        this.head.resetPose();
        this.body.resetPose();
        this.rightArm.resetPose();
        this.leftArm.resetPose();
        this.rightLeg.resetPose();
        this.leftLeg.resetPose();
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)V", at = @At(value = "HEAD"))
    private void setDefaultBeforeRender(PlayerRenderState playerRenderState, CallbackInfo ci){
        playerAnimLib$setToInitialPose(); //to not make everything wrong
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)V", at = @At(value = "RETURN"))
    private void setupPlayerAnimation(PlayerRenderState playerRenderState, CallbackInfo ci) {
        if (playerRenderState instanceof IPlayerAnimationState state && state.playerAnimLib$getAnimManager() != null && state.playerAnimLib$getAnimManager().isActive()) {
            PlayerAnimManager emote = state.playerAnimLib$getAnimManager();
            ((IMutableModel)this).playerAnimLib$setAnimation(emote);

            RenderUtil.copyVanillaPart(this.head, pal$head);
            RenderUtil.copyVanillaPart(this.body, pal$torso);
            RenderUtil.copyVanillaPart(this.rightArm, pal$rightArm);
            RenderUtil.copyVanillaPart(this.leftArm, pal$leftArm);
            RenderUtil.copyVanillaPart(this.rightLeg, pal$rightLeg);
            RenderUtil.copyVanillaPart(this.leftLeg, pal$leftLeg);

            emote.updatePart(this.head, pal$head);
            emote.updatePart(this.rightArm, pal$rightArm);
            emote.updatePart(this.leftArm, pal$leftArm);
            emote.updatePart(this.rightLeg, pal$rightLeg);
            emote.updatePart(this.leftLeg, pal$leftLeg);
            emote.updatePart(this.body, pal$torso);
        }
        else {
            ((IMutableModel)this).playerAnimLib$setAnimation(null);
        }

        if (FirstPersonMode.isFirstPersonPass() && playerRenderState instanceof IPlayerAnimationState state
                && state.playerAnimLib$isCameraEntity()) {
            var config = state.playerAnimLib$getAnimManager().getFirstPersonConfiguration();
            // Hiding all parts, because they should not be visible in first person
            playerAnimLib$setAllPartsVisible(false);
            // Showing arms based on configuration
            var showRightArm = config.isShowRightArm();
            var showLeftArm = config.isShowLeftArm();
            this.rightArm.visible = showRightArm;
            this.leftArm.visible = showLeftArm;
            // These are children of those ^^^
            //this.rightSleeve.visible = showRightArm;
            //this.leftSleeve.visible = showLeftArm;
        }
    }

    @Unique
    private void playerAnimLib$setAllPartsVisible(boolean visible) {
        this.head.visible = visible;
        this.body.visible = visible;
        this.leftLeg.visible = visible;
        this.rightLeg.visible = visible;
        this.rightArm.visible = visible;
        this.leftArm.visible = visible;

        // These are children of those ^^^
        //this.hat.visible = visible;
        //this.leftSleeve.visible = visible;
        //this.rightSleeve.visible = visible;
        //this.leftPants.visible = visible;
        //this.rightPants.visible = visible;
        //this.jacket.visible = visible;
    }

    @WrapWithCondition(method = "translateToHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private boolean translateToHand(ModelPart modelPart, PoseStack poseStack) {
        if (((IMutableModel)this).playerAnimLib$getAnimation() != null && ((IMutableModel)this).playerAnimLib$getAnimation().isActive()) {
            poseStack.translate(modelPart.x / 16.0F, modelPart.y / 16.0F, modelPart.z / 16.0F);
            if (modelPart.xRot != 0.0F || modelPart.yRot != 0.0F || modelPart.zRot != 0.0F) {
                RenderUtil.rotateZYX(poseStack.last(), modelPart.zRot, modelPart.yRot, modelPart.xRot);
            }
            poseStack.translate(0, (modelPart.yScale - 1) * 0.609375, (modelPart.zScale - 1) * 0.0625);

            return false;
        }
        return true;
    }
}
