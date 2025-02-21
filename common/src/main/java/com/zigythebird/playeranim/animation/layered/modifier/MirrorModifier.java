package com.zigythebird.playeranim.animation.layered.modifier;

import com.zigythebird.playeranim.animation.TransformType;
import com.zigythebird.playeranim.api.firstPerson.FirstPersonConfiguration;
import com.zigythebird.playeranim.math.Vec3f;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MirrorModifier extends AbstractModifier {
    public static final Map<String, String> mirrorMap;

    @Override
    public @NotNull Vec3f get3DTransform(@NotNull String modelName, @NotNull TransformType type, float tickDelta, @NotNull Vec3f value0) {
        if (mirrorMap.containsKey(modelName)) modelName = mirrorMap.get(modelName);
        value0 = transformVector(value0, type);

        Vec3f vec3f = super.get3DTransform(modelName, type, tickDelta, value0);
        return transformVector(vec3f, type);
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

    protected Vec3f transformVector(Vec3f value0, TransformType type) {
        switch (type) {
            case POSITION:
                return new Vec3f(-value0.getX(), value0.getY(), value0.getZ());
            case ROTATION:
                return new Vec3f(value0.getX(), -value0.getY(), -value0.getZ());
            case BEND:
                return new Vec3f(value0.getX(), -value0.getY(), value0.getZ());
            default:
                return value0; //why?!
        }
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
