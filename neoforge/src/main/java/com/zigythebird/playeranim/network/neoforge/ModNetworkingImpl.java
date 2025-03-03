package com.zigythebird.playeranim.network.neoforge;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

public class ModNetworkingImpl {
    public static void sendPacketToAllTracking(Entity trackedEntity, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(trackedEntity, packet);
    }
}
