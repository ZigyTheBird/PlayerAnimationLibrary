package com.zigythebird.playeranimcore.molang;

import com.google.gson.JsonElement;
import com.zigythebird.playeranimcore.PlayerAnimLib;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.event.MolangEvent;
import team.unnamed.mocha.MochaEngine;
import team.unnamed.mocha.parser.MolangParser;
import team.unnamed.mocha.parser.ParseException;
import team.unnamed.mocha.parser.ast.Expression;
import team.unnamed.mocha.parser.ast.FloatExpression;
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
    private static final Consumer<ParseException> HANDLER = e -> PlayerAnimLib.LOGGER.warn("Failed to parse!", e);

    public static List<Expression> parseJson(boolean isForRotation, JsonElement element, Expression defaultValue) {
        List<Expression> expressions;
        try (MolangParser parser = MolangParser.parser(element.getAsString())) {
            List<Expression> expressions1 = parser.parseAll();
            if (expressions1.size() == 1 && isForRotation && IsConstantExpression.test(expressions1.getFirst())) {
                expressions = new ArrayList<>();
                expressions.add(FloatExpression.of(Math.toRadians(((FloatExpression) expressions1.getFirst()).value())));
            } else {
                expressions = expressions1;
            }
        } catch (IOException e) {
            PlayerAnimLib.LOGGER.error("Failed to compile molang '{}'!", element, e);
            if (defaultValue == null) return null;
            return Collections.singletonList(defaultValue);
        }
        return expressions;
    }

    public static MochaEngine<AnimationController> createNewEngine(AnimationController controller) {
        MochaEngine<AnimationController> engine = MochaEngine.createStandard(controller);
        engine.handleParseExceptions(MolangLoader.HANDLER);

        MutableObjectBinding queryBinding = new QueryBinding<>(controller);
        setDoubleQuery(queryBinding, "anim_time", AnimationController::getAnimationTime);
        setDoubleQuery(queryBinding, "controller_speed", AnimationController::getAnimationSpeed);

        MolangEvent.MOLANG_EVENT.invoker().registerMolangQueries(controller, engine, queryBinding);
        queryBinding.block(); // make immutable

        engine.scope().set("query", queryBinding);
        engine.scope().set("q", queryBinding);
        return engine;
    }

    public static MochaEngine<?> createNewEngine() {
        MochaEngine<?> engine = MochaEngine.createStandard();
        engine.handleParseExceptions(MolangLoader.HANDLER);;
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
