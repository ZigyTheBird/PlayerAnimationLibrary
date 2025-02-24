package com.zigythebird.playeranim.network;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.NotImplementedException;

public class ModNetworking {
    @ExpectPlatform
    public static void sendPacketToAllTracking(Entity trackedEntity, CustomPacketPayload packet) {
        throw new NotImplementedException();
    }
}
