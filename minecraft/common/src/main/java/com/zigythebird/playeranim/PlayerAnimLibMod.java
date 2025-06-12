package com.zigythebird.playeranim;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranim.enums.PlayState;
import net.minecraft.resources.ResourceLocation;

public abstract class PlayerAnimLibMod extends PlayerAnimLib {
    public static final ResourceLocation ANIMATION_LAYER_ID = PlayerAnimLibMod.id("factory");

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    protected void init() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ANIMATION_LAYER_ID, 1000,
                player -> new PlayerAnimationController(player, ANIMATION_LAYER_ID,
                        (controller, state, animSetter) -> PlayState.STOP
                )
        );
    }
}
