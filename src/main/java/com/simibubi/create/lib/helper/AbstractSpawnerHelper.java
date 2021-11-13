package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.mixin.accessor.BaseSpawnerAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;

public final class AbstractSpawnerHelper {
	public static WeightedRandomList<SpawnData> getPotentialSpawns(BaseSpawner abstractSpawner) {
		return get(abstractSpawner).create$spawnPotentials();
	}

	public static SpawnData getSpawnData(BaseSpawner abstractSpawner) {
		return get(abstractSpawner).create$nextSpawnData();
	}

	private static BaseSpawnerAccessor get(BaseSpawner abstractSpawner) {
		return MixinHelper.cast(abstractSpawner);
	}

	private AbstractSpawnerHelper() {}
}
