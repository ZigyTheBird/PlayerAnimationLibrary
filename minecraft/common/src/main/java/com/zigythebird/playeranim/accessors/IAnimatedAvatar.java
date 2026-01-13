package com.zigythebird.playeranim.accessors;

import com.zigythebird.playeranim.animation.AvatarAnimManager;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

public interface IAnimatedAvatar {
    AvatarAnimManager playerAnimLib$getAnimManager();
    IAnimation playerAnimLib$getAnimation(Identifier id);

    AnimationData playerAnimlib$getAnimData();

    @ApiStatus.Internal
    boolean playerAnimLib$isAwaitingTick();
}
