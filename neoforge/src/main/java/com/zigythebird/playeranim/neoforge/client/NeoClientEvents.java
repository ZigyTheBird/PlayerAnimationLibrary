package com.zigythebird.playeranim.neoforge.client;

import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.animation.PlayerAnimResources;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import org.jetbrains.annotations.NotNull;

public class NeoClientEvents {
    @EventBusSubscriber(modid = ModInit.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class modEventBus {
        @SubscribeEvent
        public static void resourceLoadingListener(@NotNull AddClientReloadListenersEvent event) {
            event.addListener(ModInit.id("animation") ,(ResourceManagerReloadListener) PlayerAnimResources::resourceLoaderCallback);
        }
    }
}
