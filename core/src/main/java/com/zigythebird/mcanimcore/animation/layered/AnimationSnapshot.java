package com.zigythebird.mcanimcore.animation.layered;

import com.zigythebird.mcanimcore.bones.AdvancedBoneSnapshot;
import com.zigythebird.mcanimcore.bones.AnimBone;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record AnimationSnapshot(Map<String, AdvancedBoneSnapshot> snapshots) implements IAnimation {
    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public AnimBone get3DTransform(@NotNull AnimBone bone) {
        if (snapshots.containsKey(bone.getName())) {
            return bone.copySnapshotSafe(snapshots.get(bone.getName()));
        }
        return bone;
    }
}
