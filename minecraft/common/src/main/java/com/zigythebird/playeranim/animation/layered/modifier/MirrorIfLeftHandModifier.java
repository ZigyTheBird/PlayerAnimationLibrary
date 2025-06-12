package com.zigythebird.playeranim.animation.layered.modifier;

import com.zigythebird.playeranim.bones.PlayerAnimBone;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

/**Apply the mirror modifier only if the client has their main arm set as the left one*/
public class MirrorIfLeftHandModifier extends MirrorModifier {
    public MirrorIfLeftHandModifier() {
        super();
    }

    @Override
    public PlayerAnimBone get3DTransform(@NotNull PlayerAnimBone bone) {
        if (getController() != null && getController().getPlayer() == Minecraft.getInstance().player && Minecraft.getInstance().options.mainHand().get().getId() == 0) return bone;
        return super.get3DTransform(bone);
    }
}