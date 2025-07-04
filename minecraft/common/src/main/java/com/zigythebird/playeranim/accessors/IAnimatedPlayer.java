package com.zigythebird.playeranim.accessors;

import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.mcanimcore.animation.AnimationProcessor;
import com.zigythebird.mcanimcore.animation.layered.IAnimation;
import net.minecraft.resources.ResourceLocation;

public interface IAnimatedPlayer {
    PlayerAnimManager playerAnimLib$getAnimManager();
    IAnimation playerAnimLib$getAnimation(ResourceLocation id);

    AnimationProcessor playerAnimLib$getAnimProcessor();
}
