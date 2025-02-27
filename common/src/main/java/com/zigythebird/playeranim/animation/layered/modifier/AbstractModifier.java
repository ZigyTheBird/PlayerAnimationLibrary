package com.zigythebird.playeranim.animation.layered.modifier;

import com.zigythebird.playeranim.animation.*;
import com.zigythebird.playeranim.animation.layered.AnimationContainer;
import com.zigythebird.playeranim.animation.layered.IAnimation;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("rawtypes")
public abstract class AbstractModifier extends AnimationContainer<IAnimation> {
    @Setter
    protected IAnimation host;

    /**
     * Could be null if someone made their own custom way of playing animations other than animation controllers.
     */
    @Nullable
    protected AnimationController getController() {
        return host instanceof AnimationController ? (AnimationController) host : null;
    }

    public AbstractModifier() {}

    /**
     * Return true if the modifier should be removed.
     */
    public boolean canRemove() {
        return false;
    }
}
