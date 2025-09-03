/*
 * MIT License
 *
 * Copyright (c) 2022 KosmX
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

package com.zigythebird.playeranimcore.animation.layered.modifier;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.math.Vec3f;
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

        public PartModifier(Vec3f rotation, Vec3f offset) {
            this(rotation, Vec3f.ZERO, offset);
        }

        public PartModifier(Vec3f rotation, Vec3f scale, Vec3f offset) {
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

    /// Whether the adjustment should be increasingly applied
    /// between animation start and animation beginTick
    public boolean fadeIn = true;
    /// Whether the adjustment should be decreasingly applied
    /// between animation endTick and animation stop
    public boolean fadeOut = true;
    /// Whether the adjustment should be applied at all
    public boolean enabled = true;
    private float tickDelta;

    protected Function<String, Optional<PartModifier>> source;

    public AdjustmentModifier(Function<String, Optional<PartModifier>> source) {
        this.source = source;
    }

    @Override
    public void tick(AnimationData state) {
        super.tick(state);

        if (remainingFadeout > 0) {
            remainingFadeout -= 1;
            if(remainingFadeout <= 0) {
                instructedFadeout = 0;
            }
        }
    }

    @Override
    public void setupAnim(AnimationData state) {
        super.setupAnim(state);
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
            fadeOut = current / instructedFadeout;
            fadeOut = Math.min(fadeOut, 1F);
            return fadeOut;
        }
        if (this.fadeOut && getController() instanceof AnimationController controller && controller.getCurrentAnimation() != null) {
            float stopTick = controller.getCurrentAnimation().animation().length();
            float endTick = controller.getCurrentAnimation().animation().data().<Float>get("endTick").orElse(stopTick);
            float position = (-1F) * (controller.getAnimationTicks() - stopTick);
            float length = stopTick - endTick;
            if (length > 0) {
                fadeOut = position / length;
                fadeOut = Math.min(fadeOut, 1F);
            }
        }
        return fadeOut;
    }

    protected float getFadeIn() {
        float fadeIn = 1;
        if (this.fadeIn && getController() instanceof AnimationController controller && controller.getCurrentAnimation() != null) {
            float beginTick = controller.getCurrentAnimation().animation().data().<Float>get("beginTick").orElse(0F);
            fadeIn = beginTick > 0 ? controller.getAnimationTicks() / beginTick : 1F;
            fadeIn = Math.min(fadeIn, 1F);
        }
        return fadeIn;
    }

    @Override
    public PlayerAnimBone get3DTransform(@NotNull PlayerAnimBone bone) {
        if (!enabled) {
            return super.get3DTransform(bone);
        }

        Optional<PartModifier> partModifier = source.apply(bone.getName());

        float fade = getFadeIn() * getFadeOut(tickDelta);
        if (partModifier.isPresent()) {
            super.get3DTransform(bone);
            transformBone(bone, partModifier.get(), fade);
            return bone;
        }
        return super.get3DTransform(bone);
    }

    protected void transformBone(PlayerAnimBone bone, PartModifier partModifier, float fade) {
        Vec3f pos = partModifier.offset().mul(fade);
        Vec3f rot = partModifier.rotation().mul(fade);
        Vec3f scale = partModifier.scale().mul(fade);
        bone.updatePosition(pos.x() + bone.getPosX(), pos.y() + bone.getPosY(), pos.z() + bone.getPosZ());
        bone.updateRotation(rot.x() + bone.getRotX(), rot.y() + bone.getRotY(), rot.z() + bone.getRotZ());
        bone.updateScale(scale.x() + bone.getScaleX(), scale.y() + bone.getScaleY(), scale.z() + bone.getScaleZ());
    }
}