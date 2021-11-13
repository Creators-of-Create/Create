package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.mixin.accessor.MinecraftServerAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorageSource;

public final class MinecraftServerHelper {
	public static LevelStorageSource.LevelStorageAccess getAnvilConverterForAnvilFile(MinecraftServer minecraftServer) {
		return get(minecraftServer).create$storageSource();
	}

	private static MinecraftServerAccessor get(MinecraftServer minecraftServer) {
		return MixinHelper.cast(minecraftServer);
	}

	private MinecraftServerHelper() {}
}
