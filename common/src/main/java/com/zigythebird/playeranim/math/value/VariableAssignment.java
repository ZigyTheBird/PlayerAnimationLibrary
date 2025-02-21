package com.zigythebird.playeranim.math.value;

import com.zigythebird.playeranim.math.MathValue;

/**
 * {@link MathValue} value supplier
 *
 * <p>
 * <b>Contract:</b>
 * <br>
 * Assigns a variable to the given value, then returns 0
 */
public record VariableAssignment(Variable variable, MathValue value) implements MathValue {
    @Override
    public double get() {
        this.variable.set(this.value.get());

        return 0;
    }

    @Override
    public String toString() {
        return this.variable.name() + "=" + this.value.toString();
    }
}
