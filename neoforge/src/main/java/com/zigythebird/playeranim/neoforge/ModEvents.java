package com.zigythebird.playeranim.neoforge;

import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.commands.PlayPlayerAnimationCommand;
import com.zigythebird.playeranim.network.BasicPlayerAnimPacket;
import com.zigythebird.playeranim.network.PayloadHandlerS2C;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class ModEvents {
    @EventBusSubscriber(modid = ModInit.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public class GameEventListener {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            PlayPlayerAnimationCommand.register(event.getDispatcher());
        }
    }

    @EventBusSubscriber(modid = ModInit.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public class ModEventListener {
        @SubscribeEvent
        public static void registerPayloadHandlers(final RegisterPayloadHandlersEvent event) {
            event.registrar("1").playToClient(BasicPlayerAnimPacket.TYPE, BasicPlayerAnimPacket.STREAM_CODEC, (payload, context) ->
                    context.enqueueWork(() -> PayloadHandlerS2C.playAnimation(payload)));
        }
    }
}
