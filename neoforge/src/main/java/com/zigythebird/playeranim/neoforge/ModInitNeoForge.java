package com.zigythebird.playeranim.neoforge;

import com.zigythebird.playeranim.ModInitClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import com.zigythebird.playeranim.ModInit;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(ModInit.MOD_ID)
public final class ModInitNeoForge {
    public ModInitNeoForge(IEventBus modEventBus) {
        modEventBus.addListener(this::clientInit);
        ModInit.init();
    }

    public void clientInit(FMLClientSetupEvent event) {
        ModInitClient.init();
    }
}
