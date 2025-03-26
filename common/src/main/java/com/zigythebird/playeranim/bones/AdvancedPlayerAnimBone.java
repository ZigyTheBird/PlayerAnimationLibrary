package com.zigythebird.playeranim.bones;

import net.minecraft.world.phys.Vec3;

public class AdvancedPlayerAnimBone extends PlayerAnimBone {
    public Float scaleXBeginTransitionLength = null;
    public Float scaleYBeginTransitionLength = null;
    public Float scaleZBeginTransitionLength = null;

    public Float positionXBeginTransitionLength = null;
    public Float positionYBeginTransitionLength = null;
    public Float positionZBeginTransitionLength = null;

    public Float rotXBeginTransitionLength = null;
    public Float rotYBeginTransitionLength = null;
    public Float rotZBeginTransitionLength = null;

    public Float bendAxisBeginTransitionLength = null;
    public Float bendBeginTransitionLength = null;

    public AdvancedPlayerAnimBone(String name) {
        super(name);
    }

    public AdvancedPlayerAnimBone(String name, Vec3 pivot) {
        super(name, pivot);
    }

    public AdvancedPlayerAnimBone(PlayerAnimBone parent, String name, Vec3 pivot) {
        super(parent, name, pivot);
    }

    public void setBendBeginTransitionLength(Float bendBeginTransitionLength) {
        this.bendBeginTransitionLength = bendBeginTransitionLength;
    }

    public void setBendAxisBeginTransitionLength(Float bendAxisBeginTransitionLength) {
        this.bendAxisBeginTransitionLength = bendAxisBeginTransitionLength;
    }

    public void setRotZBeginTransitionLength(Float rotZBeginTransitionLength) {
        this.rotZBeginTransitionLength = rotZBeginTransitionLength;
    }

    public void setRotYBeginTransitionLength(Float rotYBeginTransitionLength) {
        this.rotYBeginTransitionLength = rotYBeginTransitionLength;
    }

    public void setRotXBeginTransitionLength(Float rotXBeginTransitionLength) {
        this.rotXBeginTransitionLength = rotXBeginTransitionLength;
    }

    public void setPositionZBeginTransitionLength(Float positionZBeginTransitionLength) {
        this.positionZBeginTransitionLength = positionZBeginTransitionLength;
    }

    public void setPositionYBeginTransitionLength(Float positionYBeginTransitionLength) {
        this.positionYBeginTransitionLength = positionYBeginTransitionLength;
    }

    public void setPositionXBeginTransitionLength(Float positionXBeginTransitionLength) {
        this.positionXBeginTransitionLength = positionXBeginTransitionLength;
    }

    public void setScaleZBeginTransitionLength(Float scaleZBeginTransitionLength) {
        this.scaleZBeginTransitionLength = scaleZBeginTransitionLength;
    }

    public void setScaleYBeginTransitionLength(Float scaleYBeginTransitionLength) {
        this.scaleYBeginTransitionLength = scaleYBeginTransitionLength;
    }

    public void setScaleXBeginTransitionLength(Float scaleXBeginTransitionLength) {
        this.scaleXBeginTransitionLength = scaleXBeginTransitionLength;
    }
}
