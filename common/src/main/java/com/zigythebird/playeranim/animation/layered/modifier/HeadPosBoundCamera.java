package com.zigythebird.playeranim.animation.layered.modifier;

import com.zigythebird.playeranim.bones.PlayerAnimBone;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

public class HeadPosBoundCamera extends AbstractModifier {
    public HeadPosBoundCamera() {
        super();
    }

    public PlayerAnimBone get3DCameraTransform(Camera camera, PlayerAnimBone bone) {
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            PlayerAnimBone bone1 = host.get3DTransform(new PlayerAnimBone(bone.getName()));
            bone.setPosX(bone1.getPosX());
            bone.setPosY(bone1.getPosY());
            bone.setPosZ(bone1.getPosZ());
        }
        return bone;
    }
}
