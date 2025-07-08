package com.zigythebird.playeranimcore.math;

public class ModVector2d {
    public float x;
    public float y;

    public ModVector2d() {}

    public ModVector2d(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void set(ModVector2d vector2d) {
        this.x = vector2d.x;
        this.y = vector2d.y;
    }
}
