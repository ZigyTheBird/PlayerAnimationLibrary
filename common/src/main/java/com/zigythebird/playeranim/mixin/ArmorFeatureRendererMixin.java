package com.zigythebird.playeranim.mixin;

import com.zigythebird.playeranim.accessors.IUpperPartHelper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
@SuppressWarnings("unckecked_cast")
public class ArmorFeatureRendererMixin {

    @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/RenderLayerParent;Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/client/renderer/entity/layers/EquipmentLayerRenderer;)V", at = @At("RETURN"))
    private void initInject(RenderLayerParent renderLayerParent, HumanoidModel humanoidModel, HumanoidModel humanoidModel2, HumanoidModel humanoidModel3, HumanoidModel humanoidModel4, EquipmentLayerRenderer equipmentLayerRenderer, CallbackInfo ci){
        ((IUpperPartHelper)this).playerAnimLib$setUpperPart(false);
    }
}
