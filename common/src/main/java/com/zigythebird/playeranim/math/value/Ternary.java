package com.zigythebird.playeranim.math.value;

import com.zigythebird.playeranim.math.MathValue;

/**
 * {@link MathValue} value supplier
 *
 * <p>
 * <b>Contract:</b>
 * <br>
 * Returns one of two stored values dependent on the result of the stored condition value.
 * This returns such that a non-zero result from the condition will return the <b>true</b> stored value, otherwise returning the <b>false</b> stored value
 */
public record Ternary(MathValue condition, MathValue trueValue, MathValue falseValue) implements MathValue {
    @Override
    public double get() {
        return this.condition.get() != 0 ? this.trueValue.get() : this.falseValue.get();
    }

    @Override
    public boolean isMutable() {
        return this.condition.isMutable() || this.trueValue.isMutable() || this.falseValue.isMutable();
    }

    @Override
    public String toString() {
        return this.condition.toString() + " ? " + this.trueValue.toString() + " : " + this.falseValue.toString();
    }
}
