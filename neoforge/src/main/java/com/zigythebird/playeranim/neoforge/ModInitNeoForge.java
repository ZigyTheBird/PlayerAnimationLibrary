package com.zigythebird.playeranim.neoforge;

import com.zigythebird.playeranim.ModInitClient;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.event.MolangEvent;
import com.zigythebird.playeranim.neoforge.event.PlayerAnimationRegisterEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import com.zigythebird.playeranim.ModInit;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ModInit.MOD_ID)
public final class ModInitNeoForge {
    public ModInitNeoForge(IEventBus modEventBus) {
        modEventBus.addListener(this::clientInit);

        ModInit.init();
    }

    public void clientInit(FMLClientSetupEvent event) {
        ModInitClient.init();

        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register(((player, manager) ->
                NeoForge.EVENT_BUS.post(new PlayerAnimationRegisterEvent(player, manager))));
        MolangEvent.MOLANG_EVENT.register(((controller, builder) ->
                NeoForge.EVENT_BUS.post(new com.zigythebird.playeranim.neoforge.event.MolangEvent(controller, builder))));
    }
}
