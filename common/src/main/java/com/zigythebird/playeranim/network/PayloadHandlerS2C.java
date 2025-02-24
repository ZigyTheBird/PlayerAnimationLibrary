package com.zigythebird.playeranim.network;

import com.zigythebird.playeranim.ModInitClient;
import com.zigythebird.playeranim.animation.AnimationController;
import com.zigythebird.playeranim.animation.RawAnimation;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;

public class PayloadHandlerS2C {
    public static void playAnimation(final BasicPlayerAnimPacket packet) {
        AbstractClientPlayer player = (AbstractClientPlayer) Minecraft.getInstance().level.getPlayerByUUID(packet.player());
        if (player == null) return;
        AnimationController controller = (AnimationController) PlayerAnimationAccess.getPlayerAnimationLayer(player, ModInitClient.animationLayerId);
        controller.tryTriggerAnimation(RawAnimation.begin().thenPlay(packet.anim()));
    }
}
