package com.zigythebird.playeranim.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;

/**
 * Helper class for segregating client-side code
 */
public final class ClientUtil {
	/**
	 * Get the player on the client
	 */
	public static LocalPlayer getClientPlayer() {
		return Minecraft.getInstance().player;
	}

	/**
	 * Gets the current level on the client
	 */
	public static Level getLevel() {
		return Minecraft.getInstance().level;
	}
}
