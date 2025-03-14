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

import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.accessors.IUpperPartHelper;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
@SuppressWarnings("unckecked_cast")
public abstract class HumanoidArmorLayerMixin<T extends HumanoidRenderState, A extends HumanoidModel<T>> {
    @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/RenderLayerParent;Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;)V", at = @At("RETURN"))
    private void initInject(RenderLayerParent renderLayerParent, HumanoidModel humanoidModel, HumanoidModel humanoidModel2, HumanoidModel humanoidModel3, HumanoidModel humanoidModel4, EquipmentLayerRenderer equipmentLayerRenderer, CallbackInfo ci){
        ((IUpperPartHelper)this).playerAnimLib$setUpperPart(false);
    }

    @Inject(method = "setPartVisibility", at = @At("HEAD"), cancellable = true)
    private void modifyArmorVisibility(A humanoidModel, EquipmentSlot equipmentSlot, CallbackInfo ci) {
        PlayerAnimManager emote = ((IAnimatedPlayer) Minecraft.getInstance().player).playerAnimLib$getAnimManager();
        if (emote.isActive() && emote.getFirstPersonMode() == FirstPersonMode.THIRD_PERSON_MODEL &&
                emote.getFirstPersonConfiguration().isShowArmor() && FirstPersonMode.isFirstPersonPass()) {
            humanoidModel.setAllVisible(false);
            if (equipmentSlot == EquipmentSlot.CHEST) {
                humanoidModel.rightArm.visible = emote.getFirstPersonConfiguration().isShowRightArm();
                humanoidModel.leftArm.visible = emote.getFirstPersonConfiguration().isShowLeftArm();
                humanoidModel.body.visible = false;
            }
            ci.cancel();
        }
    }
}
