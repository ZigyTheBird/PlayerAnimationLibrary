package com.zigythebird.playeranimcore.math;

import java.util.Objects;

public class Vec3f {
    public static final Vec3f ZERO = new Vec3f(0f, 0f, 0f);
    public static final Vec3f ONE = new Vec3f(1f, 1f, 1f);

    public final float x;
    public final float y;
    public final float z;

    public Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Scale the vector
     * @param scalar scalar
     * @return scaled vector
     */
    public Vec3f mul(float scalar) {
        return new Vec3f(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    /**
     * Add two vectors
     * @param other other vector
     * @return sum vector
     */
    public Vec3f add(Vec3f other) {
        return new Vec3f(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    /**
     * Dot product with other vector
     * @param other rhs operand
     * @return v
     */
    public float dotProduct(Vec3f other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    /**
     * Cross product
     * @param other rhs operand
     * @return v
     */
    public Vec3f crossProduct(Vec3f other) {
        return new Vec3f(
                this.y * other.z - this.z * other.y,
                this.z * other.x - this.x * other.z,
                this.x * other.y - this.y * other.x
        );
    }

    /**
     * Subtract a vector from this
     * @param other rhs operand
     * @return v
     */
    public Vec3f subtract(Vec3f other) {
        return new Vec3f(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vec3f vec)) return false;
        return Objects.equals(x, vec.x) && Objects.equals(y, vec.y) && Objects.equals(z, vec.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Vec3f[" + this.x + "; " + this.y + "; " + this.z + "]";
    }
}
