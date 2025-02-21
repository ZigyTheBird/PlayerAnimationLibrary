package com.zigythebird.playeranim.mixin.firstPerson;

import com.zigythebird.playeranim.api.firstPerson.FirstPersonMode;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = LivingEntityRenderer.class, priority = 2000)
public class LivingEntityRendererMixin {
    @Shadow @Final protected List<Object> layers;

    @Redirect(
            method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
    at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;layers:Ljava/util/List;", opcode = Opcodes.GETFIELD))
    private List<Object> filterLayers(LivingEntityRenderer instance) {
        if (instance instanceof PlayerRenderer && FirstPersonMode.isFirstPersonPass()) {
            return layers.stream().filter(layer -> layer instanceof PlayerItemInHandLayer || layer instanceof HumanoidArmorLayer<?,?,?>).toList();
        } else return layers;
    }
}
