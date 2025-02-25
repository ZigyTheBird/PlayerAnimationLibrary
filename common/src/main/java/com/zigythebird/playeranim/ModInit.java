package com.zigythebird.playeranim;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;

public final class ModInit {
    public static final String MOD_ID = "player_animation_library";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = new Gson();

    public static final IEventBus EVENT_BUS = BusBuilder.builder()
            .startShutdown()
            .build();

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    public static void init() {
        // Write common init code here.
    }
}
