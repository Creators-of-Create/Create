package com.simibubi.create.infrastructure.debugInfo;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * A supplier of debug information. May be queried on the client or server.
 */
@FunctionalInterface
public interface InfoProvider {
	/**
	 * @param player the player requesting the data. May be null
	 */
	String getInfo(@Nullable Player player);

	default String getInfoSafe(Player player) {
		try {
			return getInfo(player);
		} catch (Throwable t) {
			StringBuilder builder = new StringBuilder("Error getting information!");
			builder.append(' ').append(t.getMessage());
			for (StackTraceElement element : t.getStackTrace()) {
				builder.append('\n').append("\t").append(element.toString());
			}
			return builder.toString();
		}
	}
}
