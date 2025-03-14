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

import lombok.Getter;
import oshi.annotation.concurrent.Immutable;

import java.util.Objects;

/**
 * Pair, stores two objects.
 * @param <L> Left object
 * @param <R> Right object
 */
@Getter
@Immutable
public class Pair<L, R> {
    final L left;
    final R right;

    /**
     * Creates a pair from two values
     * @param left  left member
     * @param right right member
     */
    public Pair(L left, R right){
        this.left = left;
        this.right = right;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean equals(Object o){
        if(o instanceof Pair){
            Pair o2 = (Pair) o;
            return Objects.equals(this.left, o2.left) && Objects.equals(right, o2.right);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + (left == null ? 0 : left.hashCode());
        hash = hash * 31 + (right == null ? 0 : right.hashCode());
        return hash;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "left=" + left +
                ", right=" + right +
                '}';
    }
}
