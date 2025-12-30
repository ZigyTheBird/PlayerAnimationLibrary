package com.zigythebird.playeranimcore.animation.layered;

import com.zigythebird.playeranimcore.bones.AdvancedBoneSnapshot;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
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

    @Override
    public boolean modifiesPart(String name) {
        return snapshots.containsKey(name);
    }

    @Override
    public boolean bendsPart(String name) {
        if (modifiesPart(name)) {
            AdvancedBoneSnapshot snapshot = snapshots.get(name);
            return snapshot.bendEnabled && snapshot.getBend() != 0;
        }
        return false;
    }

    @Override
    public @NotNull String toString() {
        return "AnimationSnapshot{" +
                "snapshots=" + snapshots +
                '}';
    }
}
