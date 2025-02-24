package com.zigythebird.playeranim.network.fabric;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class ModNetworkingImpl {
    public static void sendPacketToAllTracking(Entity trackedEntity, CustomPacketPayload packet) {
        for (ServerPlayer player : PlayerLookup.tracking(trackedEntity)) {
            ServerPlayNetworking.send(player, packet);
        }
    }
}
