package com.zigythebird.playeranim.animation.layered.modifier;

import com.zigythebird.playeranim.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranim.cache.PlayerAnimBone;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MirrorModifier extends AbstractModifier {
    public static final Map<String, String> mirrorMap;

    @Override
    public void get3DTransform(@NotNull PlayerAnimBone bone) {
        String modelName = bone.getName();
        if (mirrorMap.containsKey(modelName)) modelName = mirrorMap.get(modelName);
        transformBone(bone);

        PlayerAnimBone newBone = new PlayerAnimBone(null, modelName);
        newBone.copyOtherBone(bone);
        super.get3DTransform(newBone);
        transformBone(newBone);
        bone.copyOtherBone(newBone);
    }

    // Override candidate
    @Override
    public @NotNull FirstPersonConfiguration getFirstPersonConfiguration() {
        FirstPersonConfiguration configuration = super.getFirstPersonConfiguration();
        return new FirstPersonConfiguration()
                .setShowLeftArm(configuration.isShowRightArm())
                .setShowRightArm(configuration.isShowLeftArm())
                .setShowLeftItem(configuration.isShowRightItem())
                .setShowRightItem(configuration.isShowLeftItem());
    }

    protected void transformBone(PlayerAnimBone bone) {
        bone.setPosX(-bone.getPosX());
        bone.setRotY(-bone.getRotY());
        bone.setScaleX(-bone.getScaleX());
        bone.setBend(-bone.getBend());
    }

    static {
        HashMap<String, String> partMap = new HashMap<>();
        partMap.put("left_arm", "right_arm");
        partMap.put("left_eg", "right_leg");
        partMap.put("left_item", "right_item");
        partMap.put("right_arm", "left_arm");
        partMap.put("right_leg", "left_leg");
        partMap.put("right_item", "left_item");
        mirrorMap = Collections.unmodifiableMap(partMap);
    }
}
