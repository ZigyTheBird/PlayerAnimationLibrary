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

package com.zigythebird.playeranim.mixin.firstPerson;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zigythebird.playeranim.accessors.IPlayerAnimationState;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends HumanoidRenderState, A extends HumanoidModel<T>> {
    @Unique
    private EntityRenderState playerAnimLib$renderState;

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At("HEAD"))
    private void getRenderState(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T renderState, float yRot, float xRot, CallbackInfo ci) {
        this.playerAnimLib$renderState = renderState;
    }

    @Inject(method = "renderArmorPiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"), cancellable = true)
    private void modifyArmorVisibility(PoseStack poseStack, MultiBufferSource bufferSource, ItemStack armorItem, EquipmentSlot equipmentSlot, int packedLight, A humanoidModel, CallbackInfo ci) {
        if (playerAnimLib$renderState instanceof IPlayerAnimationState state && state.playerAnimLib$isFirstPersonPass()) {
            humanoidModel.setAllVisible(false);
            PlayerAnimManager emote = state.playerAnimLib$getAnimManager();
            if (equipmentSlot == EquipmentSlot.CHEST && emote.getFirstPersonConfiguration().isShowArmor()) {
                humanoidModel.rightArm.visible = emote.getFirstPersonConfiguration().isShowRightArm();
                humanoidModel.leftArm.visible = emote.getFirstPersonConfiguration().isShowLeftArm();
            } else ci.cancel();
        } else humanoidModel.head.visible = true; //For some reason only the head doesn't get reset //TODO Try to reduce potential compat issues here
    }
}
