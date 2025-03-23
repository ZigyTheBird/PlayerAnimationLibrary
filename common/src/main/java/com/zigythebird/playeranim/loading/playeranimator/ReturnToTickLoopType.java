package com.zigythebird.playeranim.loading.playeranimator;

import com.zigythebird.playeranim.animation.Animation;
import com.zigythebird.playeranim.animation.AnimationController;
import net.minecraft.world.entity.player.Player;

public class ReturnToTickLoopType implements Animation.LoopType {
    protected final int returnToTick;

    public ReturnToTickLoopType(int returnToTick) {
        this.returnToTick = returnToTick;
    }

    @Override
    public boolean shouldPlayAgain(Player player, AnimationController controller, Animation currentAnimation) {
        // TODO Zigy, please roll back the animation :)
        return Animation.LoopType.LOOP.shouldPlayAgain(player, controller, currentAnimation); // Like a default loop
    }
}
