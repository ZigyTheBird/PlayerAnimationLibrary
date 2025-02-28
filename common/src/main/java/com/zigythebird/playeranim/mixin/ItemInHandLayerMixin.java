package com.zigythebird.playeranim.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zigythebird.playeranim.accessors.IPlayerAnimationState;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.cache.PlayerAnimBone;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {
    @Unique
    private final PlayerAnimBone playerAnimLib$rightItem = new PlayerAnimBone(null, "right_item");
    @Unique
    private final PlayerAnimBone playerAnimLib$leftItem = new PlayerAnimBone(null, "left_item");

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
    private void changeItemLocation(ArmedEntityRenderState renderState, ItemStackRenderState itemStackRenderState, HumanoidArm arm, PoseStack matrices, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if(renderState instanceof IPlayerAnimationState state) {
            if (state.playerAnimLib$getAnimManager() != null && state.playerAnimLib$getAnimManager().isActive()) {
                PlayerAnimManager anim = state.playerAnimLib$getAnimManager();
                PlayerAnimBone bone;

                if (arm == HumanoidArm.LEFT) bone = playerAnimLib$leftItem;
                else bone = playerAnimLib$rightItem;

                anim.get3DTransform(bone);

                matrices.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
                matrices.translate(bone.getPosX()/16, bone.getPosY()/16, bone.getPosZ()/16);

                matrices.mulPose(Axis.ZP.rotation(bone.getRotZ()));    //roll
                matrices.mulPose(Axis.YP.rotation(bone.getRotY()));    //pitch
                matrices.mulPose(Axis.XP.rotation(bone.getRotX()));    //yaw
            }
        }
    }
}
