package com.zigythebird.playeranim.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.zigythebird.playeranim.PlayerAnimLibMod;
import com.zigythebird.playeranim.animation.AnimationController;
import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.animation.RawAnimation;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unchecked")
public class PlayerAnimCommands {
    public static <T> void register(CommandDispatcher<T> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register((LiteralArgumentBuilder<T>) Commands.literal("testPlayerAnimation")
                .then(Commands.argument("animationID", ResourceLocationArgument.id())
                        .suggests(new AnimationArgumentProvider())
                        .executes(PlayerAnimCommands::execute)));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        ResourceLocation animation = ResourceLocationArgument.getId(context, "animationID");

        AnimationController controller = (AnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(
                Minecraft.getInstance().player, PlayerAnimLibMod.ANIMATION_LAYER_ID
        );
        if (controller == null) return 0;
        controller.triggerAnimation(RawAnimation.begin().thenPlay(PlayerAnimResources.getAnimation(animation)));
        return 1;
    }
}
