package com.zigythebird.playeranimcore.math;

import org.joml.Vector3f;

public class ExtendedVector3f extends Vector3f {
    public boolean xEnabled = true;
    public boolean yEnabled = true;
    public boolean zEnabled = true;

    public ExtendedVector3f() {
    }

    public ExtendedVector3f(float d) {
        super(d);
    }

    public ExtendedVector3f(float x, float y, float z) {
        super(x, y, z);
    }

    public ExtendedVector3f copyOtherIfNotDisabled(ExtendedVector3f vec) {
        if (vec.xEnabled) this.x = vec.x;
        if (vec.yEnabled) this.y = vec.y;
        if (vec.zEnabled) this.z = vec.z;
        return this;
    }

    public void setEnabled(boolean enabled) {
        this.xEnabled = enabled;
        this.yEnabled = enabled;
        this.zEnabled = enabled;
    }
}
