package com.zigythebird.playeranim.animation.keyframe.event.builtin;

import com.zigythebird.playeranim.animation.AnimationController;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.animation.keyframe.event.SoundKeyframeEvent;
import com.zigythebird.playeranim.util.ClientUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * Built-in helper for a {@link com.zigythebird.playeranim.animation.AnimationController.SoundKeyframeHandler SoundKeyframeHandler} that automatically plays the sound defined in the keyframe data
 * <p>
 * Due to an inability to determine the position of the sound for all animatables, this handler only supports {@link com.zigythebird.playeranim.animatable.GeoEntity GeoEntity} and {@link com.zigythebird.playeranim.animatable.GeoBlockEntity GeoBlockEntity}
 * <p>
 * The expected keyframe data format is one of the below:
 * <pre>{@code
 * namespace:soundid
 * namespace:soundid|volume|pitch
 * }</pre>
 */
public class AutoPlayingSoundKeyframeHandler implements AnimationController.SoundKeyframeHandler {
    @Override
    public void handle(SoundKeyframeEvent event) {
        String[] segments = event.getKeyframeData().getSound().split("\\|");

        BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.read(segments[0]).getOrThrow()).ifPresent(sound -> {
            Vec3 position = event.getController() instanceof PlayerAnimationController controller ? controller.getPlayer().position() : null;

            if (position != null) {
                float volume = segments.length > 1 ? Float.parseFloat(segments[1]) : 1;
                float pitch = segments.length > 2 ? Float.parseFloat(segments[2]) : 1;
                ClientUtil.getLevel().playSound(null, position.x, position.y, position.z, sound, SoundSource.PLAYERS, volume, pitch);
            }
        });
    }
}
