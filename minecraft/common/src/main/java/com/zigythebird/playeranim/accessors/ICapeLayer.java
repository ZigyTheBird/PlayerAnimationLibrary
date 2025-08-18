package com.zigythebird.playeranim.accessors;

import net.minecraft.client.model.geom.ModelPart;

public interface ICapeLayer {
    default void applyBend(ModelPart cape, ModelPart torso, float bend) {}
    default void resetBend(ModelPart cape) {}
}
