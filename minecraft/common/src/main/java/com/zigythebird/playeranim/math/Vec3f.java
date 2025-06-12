/*
 * MIT License
 *
 * Copyright (c) 2022 KosmX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.zigythebird.playeranim.math;

import oshi.annotation.concurrent.Immutable;

@Immutable
public class Vec3f extends Vector3<Float> {

    public static final Vec3f ZERO = new Vec3f(0f, 0f, 0f);
    public static final Vec3f ONE = new Vec3f(1f, 1f, 1f);

    public Vec3f(float x, float y, float z) {
        super(x, y, z);
    }

    /**
     * Scale the vector
     * @param scalar scalar
     * @return scaled vector
     */
    public Vec3f scale(float scalar) {
        return new Vec3f(this.getX() * scalar, this.getY() * scalar, this.getZ() * scalar);
    }

    /**
     * Add two vectors
     * @param other other vector
     * @return sum vector
     */
    public Vec3f add(Vec3f other) {
        return new Vec3f(this.getX() + other.getX(), this.getY() + other.getY(), this.getZ() + other.getZ());
    }

    /**
     * Dot product with other vector
     * @param other rhs operand
     * @return v
     */
    public float dotProduct(Vec3f other) {
        return this.getX() * other.getX() + this.getY() * other.getY() + this.getZ() * other.getZ();
    }

    /**
     * Cross product
     * @param other rhs operand
     * @return v
     */
    public Vec3f crossProduct(Vec3f other) {
        return new Vec3f(
                this.getY()*other.getZ() - this.getZ()*other.getY(),
                this.getZ()*other.getX() - this.getX()*other.getZ(),
                this.getX()*other.getY() - this.getY()*other.getX()
        );
    }

    /**
     * Subtract a vector from this
     * @param rhs rhs operand
     * @return v
     */
    public Vec3f subtract(Vec3f rhs) {
        //You could have guessed what will happen here.
        return add(rhs.scale(-1));
    }
}
