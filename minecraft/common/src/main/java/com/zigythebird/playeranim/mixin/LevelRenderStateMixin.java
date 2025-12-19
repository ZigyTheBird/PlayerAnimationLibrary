package com.zigythebird.playeranim.mixin;

import com.zigythebird.playeranim.accessors.IAvatarAnimationState;
import com.zigythebird.playeranim.accessors.ILevelRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LevelRenderState.class)
public class LevelRenderStateMixin implements ILevelRenderState {
    @Unique
    private final List<IAvatarAnimationState> playerAnimLib$animatedAvatarsToTick = new ArrayList<>();

    @Override
    public List<IAvatarAnimationState> playerAnimLib$getAnimatedAvatarsToTick() {
        return playerAnimLib$animatedAvatarsToTick;
    }

    @Inject(method = "reset", at = @At("TAIL"))
    private void reset(CallbackInfo ci) {
        this.playerAnimLib$animatedAvatarsToTick.clear();
    }
}
