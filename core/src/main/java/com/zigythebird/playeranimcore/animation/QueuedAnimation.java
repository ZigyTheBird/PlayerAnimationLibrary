package com.zigythebird.playeranimcore.animation;

/**
 * {@link Animation} and {@link Animation.LoopType} override pair,
 * used to define a playable animation stage for a player
 */
public record QueuedAnimation(Animation animation, Animation.LoopType loopType) {
}
