package com.zigythebird.playeranim.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zigythebird.playeranim.accessors.IPlayerAnimationState;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.animation.TransformType;
import com.zigythebird.playeranim.math.Vec3f;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
    private void changeItemLocation(ArmedEntityRenderState renderState, ItemStackRenderState itemStackRenderState, HumanoidArm arm, PoseStack matrices, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if(renderState instanceof IPlayerAnimationState state) {
            if (state.playerAnimLib$getAnimManager() != null && state.playerAnimLib$getAnimManager().isActive()) {
                PlayerAnimManager anim = state.playerAnimLib$getAnimManager();

                Vec3f scale = anim.get3DTransform(arm == HumanoidArm.LEFT ? "left_item" : "right_item", TransformType.SCALE, anim.getTickDelta(),
                        new Vec3f(ModelPart.DEFAULT_SCALE, ModelPart.DEFAULT_SCALE, ModelPart.DEFAULT_SCALE)
                );
                Vec3f rot = anim.get3DTransform(arm == HumanoidArm.LEFT ? "left_item" : "right_item", TransformType.ROTATION, anim.getTickDelta(), Vec3f.ZERO);
                Vec3f pos = anim.get3DTransform(arm == HumanoidArm.LEFT ? "left_item" : "right_item", TransformType.POSITION, anim.getTickDelta(), Vec3f.ZERO).scale(1/16f);

                matrices.scale(scale.getX(), scale.getY(), scale.getZ());
                matrices.translate(pos.getX(), pos.getY(), pos.getZ());

                matrices.mulPose(Axis.ZP.rotation(rot.getZ()));    //roll
                matrices.mulPose(Axis.YP.rotation(rot.getY()));    //pitch
                matrices.mulPose(Axis.XP.rotation(rot.getX()));    //yaw
            }
        }
    }
}
