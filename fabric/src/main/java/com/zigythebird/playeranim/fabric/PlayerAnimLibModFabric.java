package com.zigythebird.playeranim.fabric;

import com.zigythebird.playeranim.PlayerAnimLibMod;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.commands.PlayerAnimCommands;
import com.zigythebird.playeranim.event.MolangEvent;
import com.zigythebird.playeranim.fabric.client.IdentifiablePlayerAnimResources;
import com.zigythebird.playeranim.fabric.event.PlayerAnimationRegisterEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public final class PlayerAnimLibModFabric extends PlayerAnimLibMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiablePlayerAnimResources());
        ClientCommandRegistrationCallback.EVENT.register(PlayerAnimCommands::register);

        super.init();

        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, manager) ->
                PlayerAnimationRegisterEvent.EVENT.invoker().interact(player, manager)
        );
        MolangEvent.MOLANG_EVENT.register((controller, builder, q) ->
                com.zigythebird.playeranim.fabric.event.MolangEvent.EVENT.invoker().interact(controller, builder, q)
        );
    }
}
