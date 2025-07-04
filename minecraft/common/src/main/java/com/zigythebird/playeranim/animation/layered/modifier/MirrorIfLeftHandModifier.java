package com.zigythebird.playeranim.animation.layered.modifier;

import com.zigythebird.mcanimcore.bones.AnimBone;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.mcanimcore.animation.layered.modifier.MirrorModifier;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

/**Apply the mirror modifier only if the client has their main arm set as the left one*/
public class MirrorIfLeftHandModifier extends MirrorModifier {
    public MirrorIfLeftHandModifier() {
        super();
    }

    @Override
    public AnimBone get3DTransform(@NotNull AnimBone bone) {
        if (getController() instanceof PlayerAnimationController controller && controller.getPlayer() == Minecraft.getInstance().player && Minecraft.getInstance().options.mainHand().get().getId() == 0) return bone;
        return super.get3DTransform(bone);
    }
}
