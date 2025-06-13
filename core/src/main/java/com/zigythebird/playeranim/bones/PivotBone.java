package com.zigythebird.playeranim.bones;

import org.joml.Vector3f;

public class PivotBone extends PlayerAnimBone {
    private final Vector3f pivot;

    public PivotBone(String name, Vector3f pivot) {
        super(name);
        this.pivot = pivot;
    }

    public Vector3f getPivot() {
        return this.pivot;
    }
}
