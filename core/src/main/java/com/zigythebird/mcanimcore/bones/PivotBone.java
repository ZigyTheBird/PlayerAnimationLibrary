package com.zigythebird.mcanimcore.bones;

import com.zigythebird.mcanimcore.math.Vec3f;

public class PivotBone extends AnimBone {
    private final Vec3f pivot;

    public PivotBone(String name, Vec3f pivot) {
        super(name);
        this.pivot = pivot;
    }

    public Vec3f getPivot() {
        return this.pivot;
    }
}
