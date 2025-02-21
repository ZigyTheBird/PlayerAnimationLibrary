package com.zigythebird.playeranim.math;

import com.google.gson.JsonElement;
import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.event.MolangEvent;
import gg.moonflower.molangcompiler.api.MolangCompiler;
import gg.moonflower.molangcompiler.api.MolangEnvironment;
import gg.moonflower.molangcompiler.api.MolangExpression;
import gg.moonflower.molangcompiler.api.MolangRuntime;
import gg.moonflower.molangcompiler.api.exception.MolangSyntaxException;

public class MathParser {
    public static MolangCompiler COMPILER;
    public static final MolangEnvironment ENVIRONMENT;
    public static float animTime;

    public static MolangExpression parseJson(JsonElement element, float defaultValue) {
        try {
            return COMPILER.compile(element.getAsString());
        } catch (MolangSyntaxException e) {
            ModInit.LOGGER.error(e.getContext());
        }
        return MolangExpression.of(defaultValue);
    }

    static {
        MolangRuntime.Builder builder = MolangRuntime.runtime();
        builder.setQuery("anim_time", () -> MathParser.animTime);
        MolangEvent.MOLANG_EVENT.invoker().registerMolangQueries(builder);
        //Todo: Add all bedrock molang queries.
        ENVIRONMENT = builder.create();
    }
}
