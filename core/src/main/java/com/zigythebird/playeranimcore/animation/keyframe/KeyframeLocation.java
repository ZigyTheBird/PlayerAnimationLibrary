/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranimcore.animation.keyframe;

/**
 * A named pair object that stores a {@link Keyframe} and a float representing a temporally placed {@code Keyframe}
 *
 * @param keyframe The {@code Keyframe} at the tick time
 * @param startTick The animation tick time at the start of this {@code Keyframe}
 */
public record KeyframeLocation<T extends Keyframe>(T keyframe, float startTick) { }
