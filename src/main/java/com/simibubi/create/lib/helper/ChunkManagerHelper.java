package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.mixin.accessor.ChunkMapAccessor;
import com.simibubi.create.lib.utility.MixinHelper;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;

public final class ChunkManagerHelper {
	public static Long2ObjectLinkedOpenHashMap<ChunkHolder> getLoadedChunks(ChunkMap chunkManager) {
		return get(chunkManager).create$updatingChunkMap();
	}

	public static Long2ObjectLinkedOpenHashMap<ChunkHolder> getChunksToUnload(ChunkMap chunkManager) {
		return get(chunkManager).create$pendingUnloads();
	}

	public static void setImmutableLoadedChunksDirty(ChunkMap chunkManager, boolean v) {
		get(chunkManager).create$modified(v);
	}

	public static void scheduleSave(ChunkMap chunkManager, long l, ChunkHolder chunkHolder) {
		get(chunkManager).create$scheduleUnload(l, chunkHolder);
	}

	private static ChunkMapAccessor get(ChunkMap chunkManager) {
		return MixinHelper.cast(chunkManager);
	}

	private ChunkManagerHelper() {}
}
