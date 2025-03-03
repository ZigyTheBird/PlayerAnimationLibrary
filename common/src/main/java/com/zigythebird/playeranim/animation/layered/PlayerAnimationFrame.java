package com.zigythebird.playeranim.animation.layered;

import com.zigythebird.playeranim.animation.AnimationState;
import com.zigythebird.playeranim.animation.BoneSnapshot;
import com.zigythebird.playeranim.cache.PlayerAnimBone;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Mixin it into a player, add to its Animation stack,
 * and override its tick,
 * <p>
 * It is a representation of your pose on the frame.
 * Override {@link IAnimation#setupAnim} and set the pose there.
 */
public abstract class PlayerAnimationFrame implements IAnimation {

    protected PlayerBone head = new PlayerBone();
    protected PlayerBone body = new PlayerBone();
    protected PlayerBone rightArm = new PlayerBone();
    protected PlayerBone leftArm = new PlayerBone();
    protected PlayerBone rightLeg = new PlayerBone();
    protected PlayerBone leftLeg = new PlayerBone();
    protected PlayerBone rightItem = new PlayerBone();
    protected PlayerBone leftItem = new PlayerBone();

    HashMap<String, PlayerBone> parts = new HashMap<>();

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
    public void tick(AnimationState state) {
        IAnimation.super.tick(state);
    }

    @Override
    public boolean isActive() {
        for (Map.Entry<String, PlayerBone> entry: parts.entrySet()) {
            BoneSnapshot part = entry.getValue();
            if (part.isScaleAnimInProgress() || part.isRotAnimInProgress() || part.isPosAnimInProgress() || part.isBendAnimInProgress()) return true;
        }
        return false;
    }

    /**
     * Reset every part, those parts won't influence the animation
     * Don't use it if you don't want to set every part in every frame
     */
    public void resetPose() {
        for (Map.Entry<String, PlayerBone> entry: parts.entrySet()) {
            entry.getValue().setToInitialPose();
        }
    }
    
    @Override
    public void get3DTransform(@NotNull PlayerAnimBone bone) {
        BoneSnapshot part = parts.get(bone.getName());
        if (part != null) bone.copySnapshot(part);
    }
    
    public static class PlayerBone extends BoneSnapshot {
        public PlayerBone() {
            super();
            this.posAnimInProgress = false;
            this.rotAnimInProgress = false;
            this.scaleAnimInProgress = false;
            this.bendAnimInProgress = false;
        }
        
        @Override
        public void setToInitialPose() {
            this.posAnimInProgress = false;
            this.rotAnimInProgress = false;
            this.scaleAnimInProgress = false;
            this.bendAnimInProgress = false;
        }
        
        public void updateScale(float scaleX, float scaleY, float scaleZ) {
            super.updateScale(scaleX, scaleY, scaleZ);
            this.scaleAnimInProgress = scaleX != 1 || scaleY != 1 || scaleZ != 1;
        }
        
        public void updateOffset(float offsetX, float offsetY, float offsetZ) {
            super.updateOffset(offsetX, offsetY, offsetZ);
            this.posAnimInProgress = offsetX != 0 || offsetY != 0 || offsetZ != 0;
        }
        
        public void updateRotation(float rotX, float rotY, float rotZ) {
            super.updateRotation(rotX, rotY, rotZ);
            this.rotAnimInProgress = rotX != 0 || rotY != 0 || rotZ != 0;
        }

        public void updateBend(float bendAxis, float bend) {
            super.updateBend(bendAxis, bend);
            this.bendAnimInProgress = bend != 0;
        }
    }
}
