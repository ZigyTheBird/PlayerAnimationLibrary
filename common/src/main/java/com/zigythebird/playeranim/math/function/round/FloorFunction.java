package com.zigythebird.playeranim.math.function.round;

import com.zigythebird.playeranim.math.MathValue;
import com.zigythebird.playeranim.math.function.MathFunction;

/**
 * {@link MathFunction} value supplier
 *
 * <p>
 * <b>Contract:</b>
 * <br>
 * Returns the largest value that is less than or equal to the input value and is equal to an integer
 */
public final class FloorFunction extends MathFunction {
    private final MathValue value;

    public FloorFunction(MathValue... values) {
        super(values);

        this.value = values[0];
    }

    @Override
    public String getName() {
        return "math.floor";
    }

    @Override
    public double compute() {
        return Math.floor(this.value.get());
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public MathValue[] getArgs() {
        return new MathValue[] {this.value};
    }
}
