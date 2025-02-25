package com.zigythebird.playeranim.api;

import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.animation.layered.IAnimation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class PlayerAnimationAccess {
    /**
     * Get the animation manager for a player entity on the client.
     *
     * @param player The ClientPlayer object
     * @return The players' animation manager
     */
    public static PlayerAnimManager getPlayerAnimManager(AbstractClientPlayer player) throws IllegalArgumentException {
        if (player instanceof IAnimatedPlayer) {
            return ((IAnimatedPlayer) player).playerAnimLib$getAnimManager();
        } else throw new IllegalArgumentException(player + " is not a player or library mixins failed");
    }

    /**
     * Get the player animator (usually a {@link com.zigythebird.playeranim.animation.AnimationController}) associated with an id.
     * @param player player entity
     * @throws IllegalArgumentException if the given argument is not a player, or api mixins have failed (normally never)
     * @implNote data is stored in the player object (using mixins), using it is more efficient than any objectMap as objectMap solution does not know when to delete the data.
     */
    public static @Nullable IAnimation getPlayerAnimator(@NotNull AbstractClientPlayer player, @NotNull ResourceLocation id) {
        if (player instanceof IAnimatedPlayer animatedPlayer) {
            return animatedPlayer.playerAnimLib$getAnimation(id);
        } else throw new IllegalArgumentException(player + " is not a player or library mixins failed");
    }
}
