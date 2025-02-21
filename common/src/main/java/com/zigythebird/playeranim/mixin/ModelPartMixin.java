package com.zigythebird.playeranim.mixin;

import com.zigythebird.playeranim.accessors.IUpperPartHelper;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ModelPart.class)
public class ModelPartMixin implements IUpperPartHelper {
    @Unique
    private boolean playerAnimLib$isUpper = false;

    @Override
    public boolean playerAnimLib$isUpperPart() {
        return playerAnimLib$isUpper;
    }

    @Override
    public void playerAnimLib$setUpperPart(boolean bl) {
        playerAnimLib$isUpper = bl;
    }
}
