package com.zigythebird.playeranim.loading.playeranimator;

import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class DefaultValues {
    public static final StateCollection EMPTY = new StateCollection(Vec3.ZERO, Vec3.ZERO, new Vec3(1.0F, 1.0F, 1.0F));

    private static final Map<String, StateCollection> DEFAULT_VALUES = Map.of(
            "rightArm", new StateCollection(new Vec3(-5, 2, 0), Vec3.ZERO, new Vec3(1.0F, 1.0F, 1.0F)),
            "leftArm", new StateCollection(new Vec3(5, 2, 0), Vec3.ZERO, new Vec3(1.0F, 1.0F, 1.0F)),
            "leftLeg", new StateCollection(new Vec3(1.9f, 12, 0.1f), Vec3.ZERO, new Vec3(1.0F, 1.0F, 1.0F)),
            "rightLeg", new StateCollection(new Vec3(-1.9f, 12, 0.1f), Vec3.ZERO, new Vec3(1.0F, 1.0F, 1.0F))
    );

    public record StateCollection(Vec3 pos, Vec3 rot, Vec3 scale) {}

    public static StateCollection getDefaultValues(String bone) {
        return DEFAULT_VALUES.getOrDefault(bone, EMPTY);
    }
}
