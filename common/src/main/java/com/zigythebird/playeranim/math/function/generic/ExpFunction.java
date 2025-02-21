package com.zigythebird.playeranim.math.function.generic;

import com.zigythebird.playeranim.math.MathValue;
import com.zigythebird.playeranim.math.function.MathFunction;

/**
 * {@link MathFunction} value supplier
 *
 * <p>
 * <b>Contract:</b>
 * <br>
 * Returns euler's number raised to the power of the input value
 */
public final class ExpFunction extends MathFunction {
    private final MathValue value;

    public ExpFunction(MathValue... values) {
        super(values);

        this.value = values[0];
    }

    @Override
    public String getName() {
        return "math.exp";
    }

    @Override
    public double compute() {
        return Math.exp((float)this.value.get());
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
