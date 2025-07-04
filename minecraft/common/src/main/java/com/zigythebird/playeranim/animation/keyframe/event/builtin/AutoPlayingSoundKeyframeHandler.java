package com.zigythebird.playeranim.animation.keyframe.event.builtin;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.util.ClientUtil;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.event.CustomKeyFrameEvents;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranimcore.event.EventResult;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Built-in helper for a {@link CustomKeyFrameEvents.CustomKeyFrameHandler CustomKeyFrameHandler} that automatically plays the sound defined in the keyframe data
 * <p>
 * The expected keyframe data format is one of the below:
 * <pre>{@code
 * namespace:soundid
 * namespace:soundid|volume|pitch
 * }</pre>
 */
public class AutoPlayingSoundKeyframeHandler implements CustomKeyFrameEvents.CustomKeyFrameHandler<SoundKeyframeData> {
    @Override
    public EventResult handle(float animationTick, AnimationController controller, SoundKeyframeData keyFrameData, AnimationData animationData) {
        Vec3 position = controller instanceof PlayerAnimationController player ? player.getPlayer().position() : null;
        if (position == null) return EventResult.PASS;

        String[] segments = keyFrameData.getSound().split("\\|");
        ResourceLocation rl = ResourceLocation.tryParse(segments[0]);
        if (rl == null) return EventResult.PASS;

        Optional<Holder.Reference<SoundEvent>> soundEvent = BuiltInRegistries.SOUND_EVENT.get(rl);
        if (soundEvent.isEmpty()) return EventResult.PASS;

        float volume = segments.length > 1 ? Float.parseFloat(segments[1]) : 1;
        float pitch = segments.length > 2 ? Float.parseFloat(segments[2]) : 1;
        ClientUtil.getLevel().playSound(null, position.x, position.y, position.z, soundEvent.get(), SoundSource.PLAYERS, volume, pitch);
        return EventResult.SUCCESS;
    }
}
