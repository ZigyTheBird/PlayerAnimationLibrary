package com.zigythebird.playeranim.animation.layered.modifier;

import com.zigythebird.playeranim.animation.AnimationState;
import lombok.NoArgsConstructor;

/**
 * Modifies the animation speed.
 * It's better to use {@link com.zigythebird.playeranim.animation.AnimationController#setAnimationSpeed(float)}
 * speed = 2 means twice the speed, the animation will take half as long
 * <code>length = 1/speed</code>
 */
@NoArgsConstructor
public class SpeedModifier extends AbstractModifier {
    public float speed = 1;

    private float delta = 0;

    private float shiftedDelta = 0;

    public SpeedModifier(float speed) {
        if (!Float.isFinite(speed)) throw new IllegalArgumentException("Speed must be a finite number");
        this.speed = speed;
    }

    @Override
    public void tick(AnimationState state) {
        float delta = 1f - this.delta;
        this.delta = 0;
        step(delta, state);
    }

    @Override
    public void setupAnim(AnimationState state) {
        float delta = state.getPartialTick() - this.delta; //this should stay positive
        this.delta = state.getPartialTick();
        step(delta, state);
    }

    protected void step(float delta, AnimationState state) {
        delta *= speed;
        delta += shiftedDelta;
        while (delta > 1) {
            delta -= 1;
            super.tick(state);
        }
        state.setPartialTick(delta);
        super.setupAnim(state);
        this.shiftedDelta = delta;
    }
}
