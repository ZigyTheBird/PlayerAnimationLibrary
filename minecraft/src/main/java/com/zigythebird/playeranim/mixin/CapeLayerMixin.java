package com.zigythebird.playeranim.mixin;

import com.zigythebird.playeranim.accessors.IAnimatedByPAL;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(CapeLayer.class)
public class CapeLayerMixin implements IAnimatedByPAL {
    @Shadow
    @Final
    private HumanoidModel<AvatarRenderState> model;

    @Override
    public Map<String, PlayerAnimBone> pal$getCurrentBoneStates() {
        return ((IAnimatedByPAL)this.model).pal$getCurrentBoneStates();
    }
}
