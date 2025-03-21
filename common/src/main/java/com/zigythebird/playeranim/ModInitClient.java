package com.zigythebird.playeranim;

import com.zigythebird.playeranim.animation.AnimationController;
import com.zigythebird.playeranim.animation.PlayState;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import net.minecraft.resources.ResourceLocation;

public class ModInitClient {
    public static final ResourceLocation animationLayerId = ResourceLocation.fromNamespaceAndPath(ModInit.MOD_ID, "factory");

    public static void init() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                animationLayerId,
                1000,
                player -> new AnimationController(player, animationLayerId, state -> PlayState.STOP)
        );
    }
}
