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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class MolangLoader {
    private static final Consumer<ParseException> HANDLER = e -> ModInit.LOGGER.warn("Failed to parse!", e);

    public static List<Expression> parseJson(boolean isForRotation, JsonElement element, Expression defaultValue) {
        List<Expression> expressions = new ArrayList<>(1);
        try (MolangParser parser = MolangParser.parser(element.getAsString())) {
            for (Expression raw : parser.parseAll()) {
                if (isForRotation && IsConstantExpression.test(raw)) {
                    expressions.add(new DoubleExpression(Math.toRadians(
                            ((DoubleExpression) raw).value()
                    )));
                } else {
                    expressions.add(raw);
                }
            }
        } catch (IOException e) {
            ModInit.LOGGER.error("Failed to compile molang!", e);
            return Collections.singletonList(defaultValue);
        }
        return expressions;
    }

    public static MochaEngine<AnimationController> createNewEngine(AnimationController controller) {
        MochaEngine<AnimationController> engine = MochaEngine.createStandard(controller);
        engine.handleParseExceptions(MolangLoader.HANDLER);

        MutableObjectBinding queryBinding = new QueryBinding<>(controller);
        setDoubleQuery(queryBinding, "anim_time", AnimationController::getAnimTime);
        MolangQueries.setDefaultQueryValues(queryBinding);

        MolangEvent.MOLANG_EVENT.invoker().registerMolangQueries(controller, engine, queryBinding);
        queryBinding.block(); // make immutable

        engine.scope().set("query", queryBinding);
        engine.scope().set("q", queryBinding);
        return engine;
    }

    public static boolean setDoubleQuery(ObjectValue binding, String name, ToDoubleFunction<AnimationController> value) {
        return setControllerQuery(binding, name, controller -> NumberValue.of(value.applyAsDouble(controller)));
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

    public static boolean isConstant(List<Expression> expressions) {
        return expressions.stream().anyMatch(IsConstantExpression::test);
    }
}
