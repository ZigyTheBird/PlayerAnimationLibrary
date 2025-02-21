package com.zigythebird.playeranim.math;

import com.zigythebird.playeranim.animation.AnimationProcessor;
import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.exception.MolangRuntimeException;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.function.Function;

public class PlayerMolangExpression implements MolangExpression {
    public final Function<AbstractClientPlayer, Float> value;

    public PlayerMolangExpression(Function<AbstractClientPlayer, Float> value) {
        this.value = value;
    }

    @Override
    public float get(MolangEnvironment molangEnvironment) throws MolangRuntimeException {
        return value.apply(AnimationProcessor.INSTANCE.getLastPlayer());
    }
}
