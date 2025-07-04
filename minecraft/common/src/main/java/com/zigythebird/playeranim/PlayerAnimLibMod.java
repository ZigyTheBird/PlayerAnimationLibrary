package com.zigythebird.playeranim;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.animation.keyframe.event.builtin.AutoPlayingSoundKeyframeHandler;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranim.molang.MolangQueries;
import com.zigythebird.mcanimcore.MCAnimLib;
import com.zigythebird.mcanimcore.animation.keyframe.event.CustomKeyFrameEvents;
import com.zigythebird.mcanimcore.enums.PlayState;
import com.zigythebird.mcanimcore.event.MolangEvent;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.LoggerFactory;

public abstract class PlayerAnimLibMod extends MCAnimLib {
    public static final ResourceLocation ANIMATION_LAYER_ID = PlayerAnimLibMod.id("factory");
    public static final String MOD_ID = "player_animation_library";

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    protected void init() {
        MCAnimLib.LOGGER = LoggerFactory.getLogger(MOD_ID);
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ANIMATION_LAYER_ID, 1000,
                player -> new PlayerAnimationController(player,
                        (controller, state, animSetter) -> PlayState.STOP
                )
        );
        MolangEvent.MOLANG_EVENT.register((controller, engine, queryBinding) ->
                MolangQueries.setDefaultQueryValues(queryBinding)
        );
        CustomKeyFrameEvents.SOUND_KEYFRAME_EVENT.register(new AutoPlayingSoundKeyframeHandler());
    }
}
