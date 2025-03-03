package com.zigythebird.playeranim.animation.layered.modifier;

import com.zigythebird.playeranim.animation.AnimationState;
import com.zigythebird.playeranim.cache.PlayerAnimBone;
import com.zigythebird.playeranim.math.Vec3f;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Adjusts body parts during animations.<br>
 * Make sure this modifier is the first one on the list.
 * <p>
 * Example use (adjusting the vertical angle of a custom attack animation):
 * <pre>
 * {@code
 * new AdjustmentModifier((partName) -> {
 *     float rotationX = 0;
 *     float rotationY = 0;
 *     float rotationZ = 0;
 *     float scaleX = 0;
 *     float scaleY = 0;
 *     float scaleZ = 0;
 *     float offsetX = 0;
 *     float offsetY = 0;
 *     float offsetZ = 0;
 *
 *     var pitch = player.getPitch() / 2F;
 *     pitch = (float) Math.toRadians(pitch);
 *     switch (partName) {
 *         case "body" -> {
 *             rotationX = (-1F) * pitch;
 *         }
 *         case "rightArm", "leftArm" -> {
 *             rotationX = pitch;
 *         }
 *         default -> {
 *             return Optional.empty();
 *         }
 *     }
 *
 *     return Optional.of(new AdjustmentModifier.PartModifier(
 *             new Vec3f(rotationX, rotationY, rotationZ),
 *             new Vec3f(scaleX, scaleY, scaleZ),
 *             new Vec3f(offsetX, offsetY, offsetZ))
 *     );
 * });
 * }
 * </pre>
 */
public class AdjustmentModifier extends AbstractModifier {
    public static final class PartModifier {
        private final Vec3f rotation;
        private final Vec3f scale;
        private final Vec3f offset;

        public PartModifier(
                Vec3f rotation,
                Vec3f offset
        ) {
            this(rotation, Vec3f.ZERO, offset);
        }

        public PartModifier(
                Vec3f rotation,
                Vec3f scale,
                Vec3f offset
        ) {
            this.rotation = rotation;
            this.scale = scale;
            this.offset = offset;
        }

        public Vec3f rotation() {
            return rotation;
        }

        public Vec3f scale() {
            return scale;
        }

        public Vec3f offset() {
            return offset;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            PartModifier that = (PartModifier) obj;
            return Objects.equals(this.rotation, that.rotation) &&
                    Objects.equals(this.scale, that.scale) &&
                    Objects.equals(this.offset, that.offset);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rotation, scale, offset);
        }

        @Override
        public String toString() {
            return "PartModifier[" +
                    "rotation=" + rotation + ", " +
                    "scale=" + scale + ", " +
                    "offset=" + offset + ']';
        }
    }

    public boolean enabled = true;
    private float tickDelta;

    protected Function<String, Optional<PartModifier>> source;

    public AdjustmentModifier(Function<String, Optional<PartModifier>> source) {
        this.source = source;
    }

    @Override
    public void tick(AnimationState state) {
        super.tick(state);

        if (remainingFadeout > 0) {
            remainingFadeout -= 1;
            if(remainingFadeout <= 0) {
                instructedFadeout = 0;
            }
        }
    }

    @Override
    public void setupAnim(AnimationState state) {
        this.tickDelta = state.getPartialTick();
    }

    protected int instructedFadeout = 0;
    private int remainingFadeout = 0;

    public void fadeOut(int fadeOut) {
        instructedFadeout = fadeOut;
        remainingFadeout = fadeOut + 1;
    }

    protected float getFadeOut(float delta) {
        float fadeOut = 1;
        if(remainingFadeout > 0 && instructedFadeout > 0) {
            float current = Math.max(remainingFadeout - delta , 0);
            fadeOut = current / ((float)instructedFadeout);
            fadeOut = Math.min(fadeOut, 1F);
            return fadeOut;
        }
        return fadeOut;
    }

    @Override
    public void get3DTransform(@NotNull PlayerAnimBone bone) {
        if (!enabled) {
            super.get3DTransform(bone);
            return;
        }

        Optional<PartModifier> partModifier = source.apply(bone.getName());

        float fade = getFadeOut(tickDelta);
        if (partModifier.isPresent()) {
            super.get3DTransform(bone);
            transformBone(bone, partModifier.get(), fade);
        } else {
            super.get3DTransform(bone);
        }
    }

    protected void transformBone(PlayerAnimBone bone, PartModifier partModifier, float fade) {
        Vec3f pos = partModifier.offset().scale(fade);
        Vec3f rot = partModifier.rotation().scale(fade);
        Vec3f scale = partModifier.scale().scale(fade);
        bone.updatePosition(pos.getX() + bone.getPosX(), pos.getY() + bone.getPosY(), pos.getZ() + bone.getPosZ());
        bone.updateRotation(rot.getX() + bone.getRotX(), rot.getY() + bone.getRotY(), rot.getZ() + bone.getRotZ());
        bone.updateScale(scale.getX() + bone.getScaleX(), scale.getY() + bone.getScaleY(), scale.getZ() + bone.getScaleZ());
    }
}