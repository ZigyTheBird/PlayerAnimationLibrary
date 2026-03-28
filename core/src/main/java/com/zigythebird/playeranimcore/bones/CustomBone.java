package com.zigythebird.playeranimcore.bones;

import com.zigythebird.playeranimcore.bindings.PlatformModel;
import com.zigythebird.playeranimcore.math.Vec3f;
import org.jetbrains.annotations.Nullable;

public class CustomBone extends PlayerAnimBone {
    private final Vec3f pivot;

    @Nullable
    private final PlatformModel model;

    public CustomBone(String name, Vec3f pivot, @Nullable PlatformModel model) {
        super(name);
        this.pivot = pivot;
        this.model = model;
    }

    public Vec3f getPivot() {
        return this.pivot;
    }

    public @Nullable PlatformModel getModel() {
        return this.model;
    }
}
