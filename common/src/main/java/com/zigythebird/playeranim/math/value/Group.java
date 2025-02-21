package com.zigythebird.playeranim.math.value;

import com.zigythebird.playeranim.math.MathValue;

/**
 * {@link MathValue} value supplier
 *
 * <p>
 * <b>Contract:</b>
 * <br>
 * An unaltered return of the stored MathValue
 */
public record Group(MathValue contents) implements MathValue {
    @Override
    public double get() {
        return this.contents.get();
    }

    @Override
    public boolean isMutable() {
        return this.contents.isMutable();
    }

    @Override
    public String toString() {
        return "(" + this.contents.toString() + ")";
    }
}
