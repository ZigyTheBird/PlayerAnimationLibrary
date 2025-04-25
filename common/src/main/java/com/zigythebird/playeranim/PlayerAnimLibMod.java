package com.zigythebird.playeranim;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import com.zigythebird.playeranim.animation.AnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranim.enums.PlayState;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public abstract class PlayerAnimLibMod {
    public static final String MOD_ID = "player_animation_library";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = new Gson();
    public static final ResourceLocation ANIMATION_LAYER_ID = PlayerAnimLibMod.id("factory");

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    protected void init() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ANIMATION_LAYER_ID, 1000,
                player -> new AnimationController(player, ANIMATION_LAYER_ID,
                        (controller, state, animSetter) -> PlayState.STOP
                )
        );
    }
}
