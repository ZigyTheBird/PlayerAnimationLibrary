package com.zigythebird.playeranim.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.zigythebird.playeranim.network.BasicPlayerAnimPacket;
import com.zigythebird.playeranim.network.ModNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PlayPlayerAnimationCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playPlayerAnimation").requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.players())
                        .then(Commands.argument("animationID", ResourceLocationArgument.id())
                                .executes(PlayPlayerAnimationCommand::execute))));
    }

    private static int execute(CommandContext<CommandSourceStack> command) {
        try {
            ServerPlayer player = EntityArgument.getPlayer(command, "player");
            ModNetworking.sendPacketToAllTracking(player, new BasicPlayerAnimPacket(player.getUUID(), ResourceLocationArgument.getId(command, "animationID")));
        } catch (CommandSyntaxException e) {
            Player player = command.getSource().getPlayer();
            if (player != null) {
                player.displayClientMessage(Component.literal(e.getMessage()).withStyle(ChatFormatting.RED), true);
            }
        }

        return 1;
    }
}
