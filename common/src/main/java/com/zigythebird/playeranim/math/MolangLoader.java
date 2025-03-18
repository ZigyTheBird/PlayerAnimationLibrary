package com.zigythebird.playeranim.math;

import com.google.gson.JsonElement;
import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.animation.AnimationController;
import com.zigythebird.playeranim.event.MolangEvent;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.parser.MolangParser;
import team.unnamed.mocha.parser.ParseException;
import team.unnamed.mocha.parser.ast.DoubleExpression;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.runtime.IsConstantExpression;
import team.unnamed.mocha.runtime.value.MutableObjectBinding;
import team.unnamed.mocha.runtime.value.NumberValue;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.Value;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class MolangLoader {
    private static final Consumer<ParseException> HANDLER = e -> ModInit.LOGGER.warn("Failed to parse!", e);

    public static Expression parseJson(boolean isForRotation, JsonElement element, Expression defaultValue) {
        Expression raw;
        try (MolangParser parser = MolangParser.parser(element.getAsString())) {
            List<Expression> expressions = parser.parseAll();
            if (expressions.size() > 1) { // TODO entire list
                throw new IOException("not a singleton!");
            }
            raw = expressions.getFirst();
        } catch (IOException e) {
            ModInit.LOGGER.error("Failed to compile molang!", e);
            raw = defaultValue;
        }
        if (isForRotation && IsConstantExpression.test(raw)) { // TODO move to animator
            double constant = ((DoubleExpression) raw).value();
            return new DoubleExpression(Math.toRadians(constant));
        }
        return raw;
    }

    public static MochaEngine<AnimationController> createNewEngine(AnimationController controller) {
        MochaEngine<AnimationController> engine = MochaEngine.createStandard(controller);
        engine.handleParseExceptions(MolangLoader.HANDLER);

        MutableObjectBinding queryBinding = new QueryBinding<>(controller);
        setDoubleQuery(queryBinding, "anim_time", AnimationController::getAnimTime);
        setBoolQuery(queryBinding, "blocking", cont -> cont.getPlayer().isBlocking());
        // TODO: Add all bedrock molang queries. BEFORE EVENT!

        MolangEvent.MOLANG_EVENT.invoker().registerMolangQueries(controller, engine, queryBinding);
        queryBinding.block(); // make immutable

        engine.scope().set("query", queryBinding);
        engine.scope().set("q", queryBinding);
        return engine;
    }

    public static boolean setDoubleQuery(ObjectValue binding, String name, Function<AnimationController, Double> value) {
        return setControllerQuery(binding, name, controller -> NumberValue.of(value.apply(controller)));
    }

    public static boolean setBoolQuery(ObjectValue binding, String name, Function<AnimationController, Boolean> value) {
        return setControllerQuery(binding, name, controller -> Value.of((boolean) value.apply(controller)));
    }

    /**
     * some shit code
     */
    public static boolean setControllerQuery(ObjectValue binding, String name, Function<AnimationController, Value> value) {
        return binding.set(name, (team.unnamed.mocha.runtime.value.Function<AnimationController>)
                (ctx, args) -> value.apply(ctx.entity())
        );
    }
}
