package com.zigythebird.playeranim.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.zigythebird.playeranim.PlayerAnimLibMod;
import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.network.AnimationBinary;
import com.zigythebird.playeranimcore.network.LegacyAnimationBinary;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

@SuppressWarnings({"unchecked","unused"})
public class PlayerAnimCommands {
    public static <T> void register(CommandDispatcher<T> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register((LiteralArgumentBuilder<T>) Commands.literal("testPlayerAnimation")
                .then(Commands.argument("animationID", ResourceLocationArgument.id())
                        .suggests(new AnimationArgumentProvider<>())
                        .executes(PlayerAnimCommands::execute)
                )
        );
        dispatcher.register((LiteralArgumentBuilder<T>) Commands.literal("testLegacyAnimationBinary")
                .then(Commands.argument("animationID", ResourceLocationArgument.id())
                        .suggests(new AnimationArgumentProvider<>())
                        .then(Commands.argument("version", IntegerArgumentType.integer(1, LegacyAnimationBinary.getCurrentVersion()))
                                .executes(PlayerAnimCommands::executeLegacy)
                        )
                )
        );
        dispatcher.register((LiteralArgumentBuilder<T>) Commands.literal("testAnimationBinary")
                .then(Commands.argument("animationID", ResourceLocationArgument.id())
                        .suggests(new AnimationArgumentProvider<>())
                        .then(Commands.argument("version", IntegerArgumentType.integer(1, AnimationBinary.CURRENT_VERSION))
                                .executes(PlayerAnimCommands::executeBinary)
                        )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        ResourceLocation animation = ResourceLocationArgument.getId(context, "animationID");
        return playAnimation(PlayerAnimResources.getAnimation(animation));
    }

    private static int executeLegacy(CommandContext<CommandSourceStack> context) {
        Animation animation = Objects.requireNonNull(PlayerAnimResources.getAnimation(ResourceLocationArgument.getId(context, "animationID")));
        int version = IntegerArgumentType.getInteger(context, "version");

        ByteBuffer byteBuffer = ByteBuffer.allocate(LegacyAnimationBinary.calculateSize(animation, version));
        LegacyAnimationBinary.write(animation, byteBuffer, version);
        byteBuffer.flip();

        try {
            return playAnimation(LegacyAnimationBinary.read(byteBuffer, version));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int executeBinary(CommandContext<CommandSourceStack> context) {
        Animation animation = Objects.requireNonNull(PlayerAnimResources.getAnimation(ResourceLocationArgument.getId(context, "animationID")));
        int version = IntegerArgumentType.getInteger(context, "version");

        ByteBuf byteBuf = Unpooled.buffer();
        AnimationBinary.write(byteBuf, version, animation);

        return playAnimation(AnimationBinary.read(byteBuf, version));
    }

    private static int playAnimation(Animation animation) {
        AnimationController controller = (AnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(
                Objects.requireNonNull(Minecraft.getInstance().player), PlayerAnimLibMod.ANIMATION_LAYER_ID
        );
        if (controller == null) return 0;
        controller.triggerAnimation(RawAnimation.begin().thenPlay(animation));
        return 1;
    }
}
