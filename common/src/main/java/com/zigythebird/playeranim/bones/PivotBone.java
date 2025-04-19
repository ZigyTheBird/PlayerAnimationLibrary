package com.zigythebird.playeranim.bones;

import net.minecraft.world.phys.Vec3;

public class PivotBone extends PlayerAnimBone implements IBoneEnabled {
    private final Vec3 pivot;
    public IBoneEnabled child;


    public PivotBone(String name, Vec3 pivot) {
        super(name);
        this.pivot = pivot;
    }

    public Vec3 getPivot() {return this.pivot;}

    @Override
    public boolean isScaleXEnabled() {
        return child == null || child.isScaleXEnabled();
    }

    @Override
    public boolean isScaleYEnabled() {
        return child == null || child.isScaleYEnabled();
    }

    @Override
    public boolean isScaleZEnabled() {
        return child == null || child.isScaleZEnabled();
    }

    @Override
    public boolean isPositionXEnabled() {
        return child == null || child.isPositionXEnabled();
    }

    @Override
    public boolean isPositionYEnabled() {
        return child == null || child.isPositionYEnabled();
    }

    @Override
    public boolean isPositionZEnabled() {
        return child == null || child.isPositionZEnabled();
    }

    @Override
    public boolean isRotXEnabled() {
        return child == null || child.isRotXEnabled();
    }

    @Override
    public boolean isRotYEnabled() {
        return child == null || child.isRotYEnabled();
    }

    @Override
    public boolean isRotZEnabled() {
        return child == null || child.isRotZEnabled();
    }

    @Override
    public boolean isBendAxisEnabled() {
        return child == null || child.isBendAxisEnabled();
    }

    @Override
    public boolean isBendEnabled() {
        return child == null || child.isBendEnabled();
    }
}
