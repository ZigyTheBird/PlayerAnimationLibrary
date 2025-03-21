package com.zigythebird.playeranim.fabric.client;

import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.ModInitClient;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.cache.PlayerAnimCache;
import com.zigythebird.playeranim.event.MolangEvent;
import com.zigythebird.playeranim.fabric.event.PlayerAnimationRegisterEvent;
import com.zigythebird.playeranim.network.BasicPlayerAnimPacket;
import com.zigythebird.playeranim.network.PayloadHandlerS2C;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

public final class ModInitFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ModInit.id("animation");
            }

            @Override
            public void onResourceManagerReload(@NotNull ResourceManager manager) {
                PlayerAnimCache.resourceLoaderCallback(manager);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(BasicPlayerAnimPacket.TYPE, (payload, context) ->
                context.client().execute(() -> PayloadHandlerS2C.playAnimation(payload)));

        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register(((player, manager) ->
                PlayerAnimationRegisterEvent.EVENT.invoker().interact(player, manager)));
        MolangEvent.MOLANG_EVENT.register(((controller, builder, q) ->
                com.zigythebird.playeranim.fabric.event.MolangEvent.EVENT.invoker().interact(controller, builder, q)));

        ModInitClient.init();
    }
}
