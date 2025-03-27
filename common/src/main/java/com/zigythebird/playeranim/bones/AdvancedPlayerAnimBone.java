package com.zigythebird.playeranim.bones;

import net.minecraft.world.phys.Vec3;

public class AdvancedPlayerAnimBone extends PlayerAnimBone {
    public Float scaleXTransitionLength = null;
    public Float scaleYTransitionLength = null;
    public Float scaleZTransitionLength = null;

    public Float positionXTransitionLength = null;
    public Float positionYTransitionLength = null;
    public Float positionZTransitionLength = null;

    public Float rotXTransitionLength = null;
    public Float rotYTransitionLength = null;
    public Float rotZTransitionLength = null;

    public Float bendAxisTransitionLength = null;
    public Float bendTransitionLength = null;

    public AdvancedPlayerAnimBone(String name) {
        super(name);
    }

    public AdvancedPlayerAnimBone(String name, Vec3 pivot) {
        super(name, pivot);
    }

    public AdvancedPlayerAnimBone(PlayerAnimBone parent, String name, Vec3 pivot) {
        super(parent, name, pivot);
    }

    public void setBendTransitionLength(Float bendTransitionLength) {
        this.bendTransitionLength = bendTransitionLength;
    }

    public void setBendAxisTransitionLength(Float bendAxisTransitionLength) {
        this.bendAxisTransitionLength = bendAxisTransitionLength;
    }

    public void setRotZTransitionLength(Float rotZTransitionLength) {
        this.rotZTransitionLength = rotZTransitionLength;
    }

    public void setRotYTransitionLength(Float rotYTransitionLength) {
        this.rotYTransitionLength = rotYTransitionLength;
    }

    public void setRotXTransitionLength(Float rotXTransitionLength) {
        this.rotXTransitionLength = rotXTransitionLength;
    }

    public void setPositionZTransitionLength(Float positionZTransitionLength) {
        this.positionZTransitionLength = positionZTransitionLength;
    }

    public void setPositionYTransitionLength(Float positionYTransitionLength) {
        this.positionYTransitionLength = positionYTransitionLength;
    }

    public void setPositionXTransitionLength(Float positionXTransitionLength) {
        this.positionXTransitionLength = positionXTransitionLength;
    }

    public void setScaleZTransitionLength(Float scaleZTransitionLength) {
        this.scaleZTransitionLength = scaleZTransitionLength;
    }

    public void setScaleYTransitionLength(Float scaleYTransitionLength) {
        this.scaleYTransitionLength = scaleYTransitionLength;
    }

    public void setScaleXTransitionLength(Float scaleXTransitionLength) {
        this.scaleXTransitionLength = scaleXTransitionLength;
    }
}
