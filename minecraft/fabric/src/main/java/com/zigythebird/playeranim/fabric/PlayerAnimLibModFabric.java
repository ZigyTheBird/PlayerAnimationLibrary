package com.zigythebird.playeranim.fabric;

import com.zigythebird.playeranim.PlayerAnimLibMod;
import com.zigythebird.playeranim.commands.PlayerAnimCommands;
import com.zigythebird.playeranim.fabric.client.IdentifiablePlayerAnimResources;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;

public final class PlayerAnimLibModFabric extends PlayerAnimLibMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiablePlayerAnimResources());
        if (FabricLoader.getInstance().isDevelopmentEnvironment() || FabricLoader.getInstance().getModContainer(PlayerAnimLibMod.MOD_ID).get().getMetadata().getVersion().getFriendlyString().contains("dev"))
            ClientCommandRegistrationCallback.EVENT.register(PlayerAnimCommands::register);

        super.init();
    }
}
