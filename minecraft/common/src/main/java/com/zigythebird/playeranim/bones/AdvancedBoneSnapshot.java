package com.zigythebird.playeranim.bones;

public class AdvancedBoneSnapshot extends BoneSnapshot {
    public boolean scaleXEnabled;
    public boolean scaleYEnabled;
    public boolean scaleZEnabled;

    public boolean positionXEnabled;
    public boolean positionYEnabled;
    public boolean positionZEnabled;

    public boolean rotXEnabled;
    public boolean rotYEnabled;
    public boolean rotZEnabled;

    public boolean bendAxisEnabled;
    public boolean bendEnabled;

    public AdvancedBoneSnapshot(PlayerAnimBone bone) {
        super(bone);

        if (bone instanceof IBoneEnabled boneEnabled) {
            scaleXEnabled = boneEnabled.isScaleXEnabled();
            scaleYEnabled = boneEnabled.isScaleYEnabled();
            scaleZEnabled = boneEnabled.isScaleZEnabled();

            positionXEnabled = boneEnabled.isPositionXEnabled();
            positionYEnabled = boneEnabled.isPositionYEnabled();
            positionZEnabled = boneEnabled.isPositionZEnabled();

            rotXEnabled = boneEnabled.isRotXEnabled();
            rotYEnabled = boneEnabled.isRotYEnabled();
            rotZEnabled = boneEnabled.isRotZEnabled();

            bendAxisEnabled = boneEnabled.isBendAxisEnabled();
            bendEnabled = boneEnabled.isBendEnabled();
        }
    }
}
