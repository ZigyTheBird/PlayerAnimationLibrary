package com.zigythebird.playeranimcore.animation.layered.modifier;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.easing.EasingType;
import org.jetbrains.annotations.NotNull;

/**
 * Handles begin tick fade-in and end tick fade-out based on animation data.
 * Automatically reads BEGIN_TICK_KEY and END_TICK_KEY from current animation.
 */
public class BeginEndTickModifier extends AbstractModifier {
    private static final EasingType EASING = EasingType.EASE_IN_OUT_SINE;

    @Override
    public void get3DTransform(@NotNull PlayerAnimBone bone) {
        super.get3DTransform(bone);

        if (!(getController() instanceof AnimationController controller)) {
            return;
        }

        var currentAnim = controller.getCurrentAnimation();
        if (currentAnim == null) {
            return;
        }

        Animation animation = currentAnim.animation();
        ExtraAnimationData data = animation.data();
        float animTicks = controller.getAnimationTicks();

        Float beginTick = data.getNullable(ExtraAnimationData.BEGIN_TICK_KEY);
        Float endTick = data.getNullable(ExtraAnimationData.END_TICK_KEY);
        boolean hasEndTick = endTick != null && !currentAnim.loopType().shouldPlayAgain(null, animation);

        float fade = 1.0f;

        if (beginTick != null && animTicks < beginTick) {
            fade = EASING.apply(0, 1, animTicks / beginTick);
        } else if (hasEndTick && animTicks >= endTick) {
            float duration = animation.length() - endTick;
            fade = duration > 0 ? EASING.apply(1, 0, (animTicks - endTick) / duration) : 0;
        }

        if (fade < 1.0f) {
            bone.position.mul(fade);
            bone.rotation.mul(fade);
            bone.scale.set(
                1 + (bone.scale.x - 1) * fade,
                1 + (bone.scale.y - 1) * fade,
                1 + (bone.scale.z - 1) * fade
            );
        }
    }

    @Override
    public boolean canRemove() {
        return false;
    }
}
