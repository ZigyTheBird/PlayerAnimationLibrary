package com.zigythebird.playeranimcore.bones;

public class AdvancedPlayerAnimBone extends PlayerAnimBone implements IBoneEnabled {
    public Float scaleXTransitionLength = null;
    public Float scaleYTransitionLength = null;
    public Float scaleZTransitionLength = null;

    public Float positionXTransitionLength = null;
    public Float positionYTransitionLength = null;
    public Float positionZTransitionLength = null;

    public Float rotXTransitionLength = null;
    public Float rotYTransitionLength = null;
    public Float rotZTransitionLength = null;

    public Float bendTransitionLength = null;

    public boolean scaleXEnabled = true;
    public boolean scaleYEnabled = true;
    public boolean scaleZEnabled = true;

    public boolean positionXEnabled = true;
    public boolean positionYEnabled = true;
    public boolean positionZEnabled = true;

    public boolean rotXEnabled = true;
    public boolean rotYEnabled = true;
    public boolean rotZEnabled = true;

    public boolean bendAxisEnabled = true;
    public boolean bendEnabled = true;

    public AdvancedPlayerAnimBone(String name) {
        super(name);
    }

    public void setEnabled(boolean enabled) {
        scaleXEnabled = enabled;
        scaleYEnabled = enabled;
        scaleZEnabled = enabled;

        positionXEnabled = enabled;
        positionYEnabled = enabled;
        positionZEnabled = enabled;

        rotXEnabled = enabled;
        rotYEnabled = enabled;
        rotZEnabled = enabled;

        bendAxisEnabled = enabled;
        bendEnabled = enabled;
    }

    public void setBendTransitionLength(Float bendTransitionLength) {
        this.bendTransitionLength = bendTransitionLength;
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

    @Override
    public boolean isScaleXEnabled() {
        return scaleXEnabled;
    }

    @Override
    public boolean isScaleYEnabled() {
        return scaleYEnabled;
    }

    @Override
    public boolean isScaleZEnabled() {
        return scaleZEnabled;
    }

    @Override
    public boolean isPositionXEnabled() {
        return positionXEnabled;
    }

    @Override
    public boolean isPositionYEnabled() {
        return positionYEnabled;
    }

    @Override
    public boolean isPositionZEnabled() {
        return positionZEnabled;
    }

    @Override
    public boolean isRotXEnabled() {
        return rotXEnabled;
    }

    @Override
    public boolean isRotYEnabled() {
        return rotYEnabled;
    }

    @Override
    public boolean isRotZEnabled() {
        return rotZEnabled;
    }

    @Override
    public boolean isBendEnabled() {
        return bendEnabled;
    }
}
