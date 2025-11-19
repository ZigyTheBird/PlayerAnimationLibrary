package com.zigythebird.playeranimcore.bones;

public class BoneModifier implements IBoneEnabled {
    private final AdvancedPlayerAnimBone bone;

    public BoneModifier(AdvancedPlayerAnimBone modifiedBone) {
        this.bone = modifiedBone;
    }

    @Override
    public boolean isScaleXEnabled() {
        return bone.isScaleXEnabled();
    }

    @Override
    public boolean isScaleYEnabled() {
        return bone.isScaleYEnabled();
    }

    @Override
    public boolean isScaleZEnabled() {
        return bone.isScaleZEnabled();
    }

    @Override
    public boolean isPositionXEnabled() {
        return bone.isPositionXEnabled();
    }

    @Override
    public boolean isPositionYEnabled() {
        return bone.isPositionYEnabled();
    }

    @Override
    public boolean isPositionZEnabled() {
        return bone.isPositionZEnabled();
    }

    @Override
    public boolean isRotXEnabled() {
        return bone.isRotXEnabled();
    }

    @Override
    public boolean isRotYEnabled() {
        return bone.isRotYEnabled();
    }

    @Override
    public boolean isRotZEnabled() {
        return bone.isRotZEnabled();
    }

    @Override
    public boolean isBendEnabled() {
        return bone.isBendEnabled();
    }

    public void setScaleXEnabled(boolean enabled) {
        bone.scaleXEnabled = enabled;
    }

    public void setScaleYEnabled(boolean enabled) {
        bone.scaleYEnabled = enabled;
    }

    public void setScaleZEnabled(boolean enabled) {
        bone.scaleZEnabled = enabled;
    }

    public void setPositionXEnabled(boolean enabled) {
        bone.positionXEnabled = enabled;
    }

    public void setPositionYEnabled(boolean enabled) {
        bone.positionYEnabled = enabled;
    }

    public void setPositionZEnabled(boolean enabled) {
        bone.positionZEnabled = enabled;
    }

    public void setRotXEnabled(boolean enabled) {
        bone.rotXEnabled = enabled;
    }

    public void setRotYEnabled(boolean enabled) {
        bone.rotYEnabled = enabled;
    }

    public void setRotZEnabled(boolean enabled) {
        bone.rotZEnabled = enabled;
    }

    public void setBendEnabled(boolean enabled) {
        bone.bendEnabled = enabled;
    }

    public void setPositionEnabled(boolean enabled) {
        bone.positionXEnabled = enabled;
        bone.positionYEnabled = enabled;
        bone.positionZEnabled = enabled;
    }

    public void setRotEnabled(boolean enabled) {
        bone.rotXEnabled = enabled;
        bone.rotYEnabled = enabled;
        bone.rotZEnabled = enabled;
    }

    public void setScaleEnabled(boolean enabled) {
        bone.scaleXEnabled = enabled;
        bone.scaleYEnabled = enabled;
        bone.scaleZEnabled = enabled;
    }
}
