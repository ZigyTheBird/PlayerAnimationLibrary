package com.zigythebird.playeranim.fabric;

import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.commands.PlayPlayerAnimationCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class ModInitFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModInit.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            PlayPlayerAnimationCommand.register(dispatcher);
        });
    }
}
