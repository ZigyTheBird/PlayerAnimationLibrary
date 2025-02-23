package com.zigythebird.playeranim.math;

import com.google.gson.JsonElement;
import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.animation.AnimationController;
import com.zigythebird.playeranim.event.MolangEvent;
import gg.moonflower.molangcompiler.api.MolangCompiler;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import gg.moonflower.molangcompiler.api.exception.MolangSyntaxException;

public class MolangParser {
    public static final MolangCompiler COMPILER = MolangCompiler.create(MolangCompiler.OPTIMIZE_FLAG, MolangParser.class.getClassLoader());

    public static MolangExpression parseJson(boolean isForRotation, JsonElement element, MolangExpression defaultValue) {
        return MolangParser.parseJson(isForRotation, 1, element, defaultValue);
    }

    public static MolangExpression parseJson(boolean isForRotation, int multi, JsonElement element, MolangExpression defaultValue) {
        MolangExpression raw;
        try {
            raw = MolangParser.COMPILER.compile(element.getAsString());
        } catch (MolangSyntaxException e) {
            ModInit.LOGGER.error("Failed to compile molang!", e);
            raw = defaultValue;
        }
        if (isForRotation) {
            return raw.isConstant() ? MolangExpression.of((float) Math.toRadians(raw.getConstant() * multi)) : raw;
        }
        return raw;
    }

    public static MolangRuntime createNewRuntime(AnimationController controller) {
        MolangRuntime.Builder builder = MolangRuntime.runtime();
        builder.setQuery("anim_time", controller::getAnimTime);

        // TODO: Add all bedrock molang queries. BEFORE EVENT!

        MolangEvent.MOLANG_EVENT.invoker().registerMolangQueries(controller, builder);

        return builder.create();
    }
}
