package com.zigythebird.playeranim.fabric.client;

import com.zigythebird.playeranim.animation.PlayerAnimResources;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;

public class IdentifiablePlayerAnimResources extends PlayerAnimResources implements IdentifiableResourceReloadListener {
    @Override
    public ResourceLocation getFabricId() {
        return PlayerAnimResources.KEY;
    }
}
