package com.zigythebird.playeranim.animation.layered.modifier;

import com.zigythebird.playeranim.animation.*;
import com.zigythebird.playeranim.animation.layered.AnimationContainer;
import com.zigythebird.playeranim.animation.layered.IAnimation;
import com.zigythebird.playeranim.math.Vec3f;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
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

    @Override
    public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
        if (anim == null) return value0;
        return anim instanceof AnimationController ? anim.get3DTransformRaw(modelName, type, tickDelta, value0) : anim.get3DTransform(modelName, type, tickDelta, value0);
    }

    public AbstractModifier() {}

    /**
     * Return true if the modifier should be removed.
     */
    public boolean canRemove() {
        return false;
    }
}
