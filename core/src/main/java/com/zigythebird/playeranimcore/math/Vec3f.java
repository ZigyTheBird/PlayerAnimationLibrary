package com.zigythebird.playeranimcore.math;

import java.util.Objects;

public record Vec3f(float x, float y, float z) {
    public static final Vec3f ZERO = new Vec3f(0f, 0f, 0f);
    public static final Vec3f ONE = new Vec3f(1f, 1f, 1f);

    /**
     * Scale the vector
     *
     * @param scalar scalar
     * @return scaled vector
     */
    public Vec3f mul(float scalar) {
        return new Vec3f(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    /**
     * Add two vectors
     *
     * @param other other vector
     * @return sum vector
     */
    public Vec3f add(Vec3f other) {
        return new Vec3f(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vec3f vec)) return false;
        return Objects.equals(x, vec.x) && Objects.equals(y, vec.y) && Objects.equals(z, vec.z);
    }

    @Override
    public String toString() {
        return "Vec3f[" + this.x + "; " + this.y + "; " + this.z + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
