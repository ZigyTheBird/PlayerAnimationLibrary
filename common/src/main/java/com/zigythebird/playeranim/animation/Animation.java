/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.zigythebird.playeranim.animation;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.zigythebird.playeranim.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranim.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranim.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranim.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranim.cache.bones.PlayerAnimBone;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A compiled animation instance for use by the {@link AnimationController}
 * <p>
 * Modifications or extensions of a compiled Animation are not supported, and therefore an instance of <code>Animation</code> is considered final and immutable
 */
public record Animation(ExtraAnimationData data, double length, LoopType loopType, BoneAnimation[] boneAnimations, Keyframes keyFrames, Map<String, PlayerAnimBone> bones, Map<String, String> parents) {
    public record Keyframes(SoundKeyframeData[] sounds, ParticleKeyframeData[] particles, CustomInstructionKeyframeData[] customInstructions) {}

    static Animation generateWaitAnimation(double length) {
        return new Animation(new ExtraAnimationData(ExtraAnimationData.NAME_KEY, RawAnimation.Stage.WAIT.getPath()), length, LoopType.PLAY_ONCE, new BoneAnimation[0],
                new Keyframes(new SoundKeyframeData[0], new ParticleKeyframeData[0], new CustomInstructionKeyframeData[0]), new HashMap<>(), new HashMap<>());
    }

    /**
     * Loop type functional interface to define post-play handling for a given animation
     * <p>
     * Custom loop types are supported by extending this class and providing the extended class instance as the loop type for the animation
     */
    @FunctionalInterface
    public interface LoopType {
        Map<String, LoopType> LOOP_TYPES = new ConcurrentHashMap<>(4);

        LoopType DEFAULT = (animatable, controller, currentAnimation) -> currentAnimation.loopType().shouldPlayAgain(animatable, controller, currentAnimation);
        LoopType PLAY_ONCE = register("play_once", register("false", (animatable, controller, currentAnimation) -> false));
        LoopType HOLD_ON_LAST_FRAME = register("hold_on_last_frame", (animatable, controller, currentAnimation) -> {
            controller.animationState = State.PAUSED;

            return true;
        });
        LoopType LOOP = register("loop", register("true", (animatable, controller, currentAnimation) -> true));

        /**
         * Override in a custom instance to dynamically decide whether an animation should repeat or stop
         *
         * @param player The player relevant to this method call
         * @param controller The {@link AnimationController} playing the current animation
         * @param currentAnimation The current animation that just played
         * @return Whether the animation should play again, or stop
         */
        boolean shouldPlayAgain(Player player, AnimationController controller, Animation currentAnimation);

        /**
         * Override in a custom instance to dynamically decide where an animation should start after looping.
         *
         * @param player The player relevant to this method call
         * @param controller The {@link AnimationController} playing the current animation
         * @param currentAnimation The current animation that just played
         * @return The tick the animation starts from after looping.
         */
        default double restartFromTick(Player player, AnimationController controller, Animation currentAnimation) {
            return 0;
        }

        static LoopType returnToTickLoop(double tick) {
            return new LoopType() {
                @Override
                public boolean shouldPlayAgain(Player player, AnimationController controller, Animation currentAnimation) {
                    return true;
                }

                @Override
                public double restartFromTick(Player player, AnimationController controller, Animation currentAnimation) {
                    return tick;
                }
            };
        }

        /**
         * Retrieve a LoopType instance based on a {@link JsonElement}
         * <p>
         * Returns either {@link LoopType#PLAY_ONCE} or {@link LoopType#LOOP} based on a boolean or string element type,
         * or any other registered loop type with a matching type string
         *
         * @param json The <code>loop</code> {@link JsonElement} to attempt to parse
         * @return A usable LoopType instance
         */
        static LoopType fromJson(JsonElement json) {
            if (json == null || !json.isJsonPrimitive())
                return PLAY_ONCE;

            JsonPrimitive primitive = json.getAsJsonPrimitive();

            if (primitive.isBoolean())
                return primitive.getAsBoolean() ? LOOP : PLAY_ONCE;

            if (primitive.isString())
                return fromString(primitive.getAsString());

            return PLAY_ONCE;
        }

        static LoopType fromString(String name) {
            return LOOP_TYPES.getOrDefault(name, PLAY_ONCE);
        }

        /**
         * Register a LoopType with Player Animation Library for handling loop functionality of animations
         * <p>
         * <b><u>MUST be called during mod construct</u></b>
         * <p>
         *
         * @param name The name of the loop type
         * @param loopType The loop type to register
         * @return The registered {@code LoopType}
         */
        static LoopType register(String name, LoopType loopType) {
            LOOP_TYPES.put(name, loopType);

            return loopType;
        }
    }
}