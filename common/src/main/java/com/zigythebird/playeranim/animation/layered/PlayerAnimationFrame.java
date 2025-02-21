package com.zigythebird.playeranim.animation.layered;

import com.zigythebird.playeranim.animation.TransformType;
import com.zigythebird.playeranim.math.Pair;
import com.zigythebird.playeranim.math.Vec3f;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Mixin it into a player, add to its Animation stack,
 * and override its tick,
 * <p>
 * It is a representation of your pose on the frame.
 * Override {@link IAnimation#setupAnim(float)} and set the pose there.
 */
public abstract class PlayerAnimationFrame implements IAnimation {

    protected PlayerPart head = new PlayerPart();
    protected PlayerPart body = new PlayerPart();
    protected PlayerPart rightArm = new PlayerPart();
    protected PlayerPart leftArm = new PlayerPart();
    protected PlayerPart rightLeg = new PlayerPart();
    protected PlayerPart leftLeg = new PlayerPart();
    protected PlayerPart rightItem = new PlayerPart();
    protected PlayerPart leftItem = new PlayerPart();

    HashMap<String, PlayerPart> parts = new HashMap<>();

    public PlayerAnimationFrame() {
        parts.put("head", head);
        parts.put("body", body);
        parts.put("rightArm", rightArm);
        parts.put("leftArm", leftArm);
        parts.put("rightLeg", rightLeg);
        parts.put("leftLeg", leftLeg);
        parts.put("rightItem", rightItem);
        parts.put("leftItem", leftItem);
    }


    @Override
    public void tick() {
        IAnimation.super.tick();
    }

    @Override
    public boolean isActive() {
        for (Map.Entry<String, PlayerPart> entry: parts.entrySet()) {
            PlayerPart part = entry.getValue();
            if (part.bend != null || part.pos != null || part.rot != null || part.scale != null) return true;
        }
        return false;
    }

    /**
     * Reset every part, those parts won't influence the animation
     * Don't use it if you don't want to set every part in every frame
     */
    public void resetPose() {
        for (Map.Entry<String, PlayerPart> entry: parts.entrySet()) {
            entry.getValue().setNull();
        }
    }


    @Override
    public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
        PlayerPart part = parts.get(modelName);
        if (part == null) return value0;
        switch (type) {
            case POSITION:
                return part.pos == null ? value0 : part.pos;
            case ROTATION:
                return part.rot == null ? value0 : part.rot;
            case SCALE:
                return part.scale == null ? value0 : part.scale;
            case BEND:
                return part.bend == null ? value0 : new Vec3f(part.bend.getLeft(), part.bend.getRight(), 0f);
            default:
                return value0;
        }
    }

    public static class PlayerPart {
        public Vec3f pos;
        public Vec3f scale;
        public Vec3f rot;
        public Pair<Float, Float> bend;

        protected void setNull() {
            pos = scale = rot = null;
            bend = null;
        }
    }
}
