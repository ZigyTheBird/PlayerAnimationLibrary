package com.zigythebird.playeranim.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zigythebird.playeranim.accessors.IMutableModel;
import com.zigythebird.playeranim.accessors.IPlayerAnimationState;
import com.zigythebird.playeranim.accessors.IPlayerModel;
import com.zigythebird.playeranim.animation.AnimationProcessor;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonMode;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(value = PlayerModel.class, priority = 2001)//Apply after NotEnoughAnimation's inject
public class PlayerModelMixin extends HumanoidModel<PlayerRenderState> implements IPlayerModel {
    @Shadow
    @Final
    public ModelPart rightSleeve;
    @Shadow
    @Final
    public ModelPart leftSleeve;
    @Unique
    private boolean playerAnimLib$firstPersonNext = false;

    public PlayerModelMixin(ModelPart modelPart, Function<ResourceLocation, RenderType> function) {
        super(modelPart, function);
    }

    @Unique
    private void playerAnimLib$setDefaultPivot(){
        this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
        this.rightLeg.setPos(- 1.9F, 12.0F, 0.0F);
        this.head.setPos(0.0F, 0.0F, 0.0F);
        this.rightArm.z = 0.0F;
        this.rightArm.x = - 5.0F;
        this.leftArm.z = 0.0F;
        this.leftArm.x = 5.0F;
        this.body.xRot = 0.0F;
        this.rightLeg.z = 0.1F;
        this.leftLeg.z = 0.1F;
        this.rightLeg.y = 12.0F;
        this.leftLeg.y = 12.0F;
        this.head.y = 0.0F;
        this.head.zRot = 0f;
        this.body.y = 0.0F;
        this.body.x = 0f;
        this.body.z = 0f;
        this.body.yRot = 0;
        this.body.zRot = 0;

        this.head.xScale = ModelPart.DEFAULT_SCALE;
        this.head.yScale = ModelPart.DEFAULT_SCALE;
        this.head.zScale = ModelPart.DEFAULT_SCALE;
        this.body.xScale = ModelPart.DEFAULT_SCALE;
        this.body.yScale = ModelPart.DEFAULT_SCALE;
        this.body.zScale = ModelPart.DEFAULT_SCALE;
        this.rightArm.xScale = ModelPart.DEFAULT_SCALE;
        this.rightArm.yScale = ModelPart.DEFAULT_SCALE;
        this.rightArm.zScale = ModelPart.DEFAULT_SCALE;
        this.leftArm.xScale = ModelPart.DEFAULT_SCALE;
        this.leftArm.yScale = ModelPart.DEFAULT_SCALE;
        this.leftArm.zScale = ModelPart.DEFAULT_SCALE;
        this.rightLeg.xScale = ModelPart.DEFAULT_SCALE;
        this.rightLeg.yScale = ModelPart.DEFAULT_SCALE;
        this.rightLeg.zScale = ModelPart.DEFAULT_SCALE;
        this.leftLeg.xScale = ModelPart.DEFAULT_SCALE;
        this.leftLeg.yScale = ModelPart.DEFAULT_SCALE;
        this.leftLeg.zScale = ModelPart.DEFAULT_SCALE;
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)V", at = @At(value = "HEAD"))
    private void setDefaultBeforeRender(PlayerRenderState playerRenderState, CallbackInfo ci){
        playerAnimLib$setDefaultPivot(); //to not make everything wrong
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)V", at = @At(value = "RETURN"))
    private void setupPlayerAnimation(PlayerRenderState playerRenderState, CallbackInfo ci) {
        if(!playerAnimLib$firstPersonNext && playerRenderState instanceof IPlayerAnimationState state && state.playerAnimLib$getAnimManager() != null && state.playerAnimLib$getAnimManager().isActive()) {
            PlayerAnimManager emote = state.playerAnimLib$getAnimManager();
            AnimationProcessor processor = state.playerAnimLib$getAnimProcessor();
            processor.handleAnimations(emote.getTickDelta());
            ((IMutableModel)this).playerAnimLib$setAnimation(emote);

            emote.updatePart("head", this.head, processor);
            emote.updatePart("right_arm", this.rightArm, processor);
            emote.updatePart("left_arm", this.leftArm, processor);
            emote.updatePart("right_leg", this.rightLeg, processor);
            emote.updatePart("left_leg", this.leftLeg, processor);
            emote.updatePart("torso", this.body, processor);
        }
        else {
            playerAnimLib$firstPersonNext = false;
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
            this.rightSleeve.visible = showRightArm;
            this.leftArm.visible = showLeftArm;
            this.leftSleeve.visible = showLeftArm;
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

        // these are children of those ^^^
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
                poseStack.mulPose(new Quaternionf().rotationZYX(modelPart.zRot, modelPart.yRot, modelPart.xRot));
            }
            poseStack.translate(0, (modelPart.yScale - 1) * 0.609375, (modelPart.zScale - 1) * 0.0625);

            return false;
        }
        return true;
    }

    @Override
    public void playerAnimLib$prepForFirstPersonRender() {
        playerAnimLib$firstPersonNext = true;
    }

    @Override
    public boolean playerAnimLib$isFirstPersonRender() {
        return this.playerAnimLib$firstPersonNext;
    }
}
