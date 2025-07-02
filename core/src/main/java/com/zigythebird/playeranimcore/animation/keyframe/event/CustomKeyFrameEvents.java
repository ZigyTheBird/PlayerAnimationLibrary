package com.zigythebird.playeranimcore.animation.keyframe.event;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.CustomInstructionKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.KeyFrameData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.ParticleKeyframeData;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranimcore.event.Event;
import com.zigythebird.playeranimcore.event.EventResult;

public class CustomKeyFrameEvents {
    public static final Event<CustomKeyFrameHandler<CustomInstructionKeyframeData>> CUSTOM_INSTRUCTION_KEYFRAME_EVENT = new Event<>(listeners -> (animationTick, controller, eventKeyFrame, animationData) -> {
        for (CustomKeyFrameHandler<CustomInstructionKeyframeData> listener : listeners) {
            EventResult result = listener.handle(animationTick, controller, eventKeyFrame, animationData);
            if (result == EventResult.FAIL) {
                return result;
            }
        }
        return EventResult.PASS;
    });

    public static final Event<CustomKeyFrameHandler<ParticleKeyframeData>> PARTICLE_KEYFRAME_EVENT = new Event<>(listeners -> (animationTick, controller, eventKeyFrame, animationData) -> {
        for (CustomKeyFrameHandler<ParticleKeyframeData> listener : listeners) {
            EventResult result = listener.handle(animationTick, controller, eventKeyFrame, animationData);
            if (result == EventResult.FAIL) {
                return result;
            }
        }
        return EventResult.PASS;
    });

    public static final Event<CustomKeyFrameHandler<SoundKeyframeData>> SOUND_KEYFRAME_EVENT = new Event<>(listeners -> (animationTick, controller, eventKeyFrame, animationData) -> {
        for (CustomKeyFrameHandler<SoundKeyframeData> listener : listeners) {
            EventResult result = listener.handle(animationTick, controller, eventKeyFrame, animationData);
            if (result == EventResult.FAIL) {
                return result;
            }
        }
        return EventResult.PASS;
    });

    @FunctionalInterface
    public interface CustomKeyFrameHandler<T extends KeyFrameData> {
        EventResult handle(float animationTick, AnimationController controller, T keyFrameData, AnimationData animationData);
    }
}
