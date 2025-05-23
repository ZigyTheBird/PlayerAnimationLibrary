package com.zigythebird.playeranim.animation.layered;

import com.zigythebird.playeranim.bones.AdvancedBoneSnapshot;
import com.zigythebird.playeranim.bones.PlayerAnimBone;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record AnimationSnapshot(Map<String, AdvancedBoneSnapshot> snapshots) implements IAnimation {
    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public PlayerAnimBone get3DTransform(@NotNull PlayerAnimBone bone) {
        if (snapshots.containsKey(bone.getName())) {
            return bone.copySnapshotSafe(snapshots.get(bone.getName()));
        }
        return bone;
    }
}
