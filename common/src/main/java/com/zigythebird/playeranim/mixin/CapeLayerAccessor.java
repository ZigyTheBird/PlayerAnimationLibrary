package com.zigythebird.playeranim.mixin;

import net.minecraft.client.model.PlayerCapeModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerCapeModel.class)
public interface CapeLayerAccessor {
    @Accessor
    ModelPart getCape();
}
