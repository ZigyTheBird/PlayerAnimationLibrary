package com.zigythebird.playeranim.accessors;

import com.zigythebird.playeranim.animation.PlayerAnimManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public interface IMutableModel {
    void playerAnimLib$setAnimation(@Nullable PlayerAnimManager emoteSupplier);

    @Nullable PlayerAnimManager playerAnimLib$getAnimation();
}
