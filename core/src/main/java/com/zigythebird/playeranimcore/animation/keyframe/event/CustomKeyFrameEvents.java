/*
 * MIT License
 *
 * Copyright (c) 2024 GeckoLib
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.zigythebird.playeranimcore.animation.keyframe.event;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.KeyFrameData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranimcore.event.Event;
import com.zigythebird.playeranimcore.event.EventResult;

import java.util.Set;

public class CustomKeyFrameEvents {
    /**
     * A event for pre-defined custom instruction keyframes
     * <p>
     * When the keyframe is encountered, the {@link CustomKeyFrameHandler#handle(float, AnimationController, KeyFrameData, AnimationData)} method will be called.
     * You can then take whatever action you want at this point.
     */
    public static final Event<CustomKeyFrameHandler<CustomInstructionKeyframeData>> CUSTOM_INSTRUCTION_KEYFRAME_EVENT = new Event<>(listeners -> (animationTick, controller, eventKeyFrame, animationData) -> {
        for (CustomKeyFrameHandler<CustomInstructionKeyframeData> listener : listeners) {
            EventResult result = listener.handle(animationTick, controller, eventKeyFrame, animationData);
            if (result == EventResult.FAIL) {
                return result;
            }
        }
        return EventResult.PASS;
    });

    /**
     * A event for when a predefined particle keyframe is hit
     * <p>
     * When the keyframe is encountered, the {@link CustomKeyFrameHandler#handle(float, AnimationController, KeyFrameData, AnimationData)} method will be called.
     * Spawn the particles/effects of your choice at this time.
     */
    public static final Event<CustomKeyFrameHandler<ParticleKeyframeData>> PARTICLE_KEYFRAME_EVENT = new Event<>(listeners -> (animationTick, controller, eventKeyFrame, animationData) -> {
        for (CustomKeyFrameHandler<ParticleKeyframeData> listener : listeners) {
            EventResult result = listener.handle(animationTick, controller, eventKeyFrame, animationData);
            if (result == EventResult.FAIL) {
                return result;
            }
        }
        return EventResult.PASS;
    });

    /**
     * A event for when a predefined sound keyframe is hit
     * <p>
     * When the keyframe is encountered, the {@link CustomKeyFrameHandler#handle(float, AnimationController, KeyFrameData, AnimationData)} method will be called.
     * Play the sound(s) of your choice at this time.
     */
    public static final Event<CustomKeyFrameHandler<SoundKeyframeData>> SOUND_KEYFRAME_EVENT = new Event<>(listeners -> (animationTick, controller, eventKeyFrame, animationData) -> {
        for (CustomKeyFrameHandler<SoundKeyframeData> listener : listeners) {
            EventResult result = listener.handle(animationTick, controller, eventKeyFrame, animationData);
            if (result == EventResult.FAIL) {
                return result;
            }
        }
        return EventResult.PASS;
    });

    public static final Event<ResetKeyFramesHandler> RESET_KEYFRAMES_EVENT = new Event<>(listeners -> (controller, eventKeyFrames) -> {
        for (ResetKeyFramesHandler listener : listeners) {
            listener.handle(controller, eventKeyFrames);
        }
    });

    @FunctionalInterface
    public interface CustomKeyFrameHandler<T extends KeyFrameData> {
        EventResult handle(float animationTick, AnimationController controller, T keyFrameData, AnimationData animationData);
    }

    @FunctionalInterface
    public interface ResetKeyFramesHandler {
        void handle(AnimationController controller, Set<KeyFrameData> keyFrameData);
    }
}
