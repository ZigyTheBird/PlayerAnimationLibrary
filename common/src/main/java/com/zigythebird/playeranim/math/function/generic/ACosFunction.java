package com.zigythebird.playeranim.math.function.generic;

import net.minecraft.util.Mth;
import com.zigythebird.playeranim.math.MathValue;
import com.zigythebird.playeranim.math.function.MathFunction;

/**
 * {@link MathFunction} value supplier
 *
 * <p>
 * <b>Contract:</b>
 * <br>
 * Returns the arc-cosine of the input value angle, with the input angle converted to radians
 */
public final class ACosFunction extends MathFunction {
    private final MathValue value;

    public ACosFunction(MathValue... values) {
        super(values);

        this.value = values[0];
    }

    @Override
    public String getName() {
        return "math.acos";
    }

    @Override
    public double compute() {
        return Math.acos((float)this.value.get() * Mth.DEG_TO_RAD);
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
