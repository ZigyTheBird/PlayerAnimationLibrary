package com.zigythebird.playeranim;

import com.zigythebird.playeranim.animation.AnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranim.enums.PlayState;
import net.minecraft.resources.ResourceLocation;

public class ModInitClient {
    public static final ResourceLocation animationLayerId = ResourceLocation.fromNamespaceAndPath(ModInit.MOD_ID, "factory");

    public static void init() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                animationLayerId,
                1000,
                player -> new AnimationController(player, animationLayerId, (controller, state, animSetter) -> PlayState.STOP)
        );
    }
}
