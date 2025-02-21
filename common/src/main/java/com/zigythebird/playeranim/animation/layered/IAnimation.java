package com.zigythebird.playeranim.animation.layered;

import com.zigythebird.playeranim.animation.State;
import com.zigythebird.playeranim.animation.TransformType;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranim.math.Pair;
import com.zigythebird.playeranim.math.Vec3f;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

public interface IAnimation {
    FirstPersonConfiguration DEFAULT_FIRST_PERSON_CONFIG = new FirstPersonConfiguration();

    /**
     * Animation tick, on lag-free client 20 [tick/sec]
     * You can get the animations time from other places, but it will be invoked when the animation is ACTIVE
     */
    default void tick() {}

    /**
     * Called before rendering a character
     * @param tickDelta Time since the last tick. 0-1
     */
    default void setupAnim(float tickDelta) {}

    /**
     * Pretty self-explanatory.
     */
    @NotNull State getAnimationState();
    default boolean isActive() {
        return getAnimationState().isActive();
    }

    /**
     * Whether or not {@link IAnimation#get3DTransform} should be called.
     * Basically removes all influence your anim has if false, but still ticks it.
     */
    default boolean shouldGet3DTransform() {
        return true;
    }

    /**
     * Get the transformed value to a model part without any modifiers applied.
     * @param modelName The questionable model part
     * @param type      Transform type
     * @param tickDelta Time since the last tick. 0-1
     * @param value0    The value before the transform. For identity transform return with it.
     * @return The new transform value
     */
    default @NotNull Vec3f get3DTransformRaw(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
        return value0;
    }

    /**
     * Get the transformed value to a model part.
     * @param modelName The questionable model part
     * @param type      Transform type
     * @param tickDelta Time since the last tick. 0-1
     * @param value0    The value before the transform. For identity transform return with it.
     * @return The new transform value
     */
    default @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
        return value0;
    }

    /**
     * Keep in mind that modifiers can't affect the first-person mode, at least not by default.
     */
    @NotNull
    default FirstPersonMode getFirstPersonMode() {
        return FirstPersonMode.NONE;
    }

    /**
     * Keep in mind that modifiers can't affect the first-person configuration, at least not by default.
     */
    @NotNull
    default FirstPersonConfiguration getFirstPersonConfiguration() {
        return DEFAULT_FIRST_PERSON_CONFIG;
    }

    default @NotNull Pair<Float, Float> getBend(String modelName) {
        float tickDelta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Vec3f bendVec = this.get3DTransform(modelName, TransformType.BEND, tickDelta, Vec3f.ZERO);
        return new Pair<>(bendVec.getX(), bendVec.getY());
    }
}
