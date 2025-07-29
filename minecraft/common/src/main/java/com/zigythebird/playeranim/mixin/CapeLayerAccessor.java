package com.zigythebird.playeranim.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CapeLayer.class)
public interface CapeLayerAccessor {
    @Accessor
    HumanoidModel<PlayerRenderState> getModel();
}
