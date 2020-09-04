package com.simibubi.create.foundation.command;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

public class ChunkUtil {
	private static final Logger LOGGER = LogManager.getLogger("Create/ChunkUtil");
	final EnumSet<Heightmap.Type> POST_FEATURES = EnumSet.of(Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE, Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES);

	private final List<Long> markedChunks;
	private final List<Long> interestingChunks;

	public ChunkUtil() {
		LOGGER.debug("Chunk Util constructed");
		markedChunks = new LinkedList<>();
		interestingChunks = new LinkedList<>();
	}

	public void init() {
		ChunkStatus.FULL = new ChunkStatus("full", ChunkStatus.HEIGHTMAPS, 0, POST_FEATURES, ChunkStatus.Type.LEVELCHUNK,
				(_0, _1, _2, _3, _4, future, _6, chunk) -> future.apply(chunk),
				(_0, _1, _2, _3, future, chunk) -> {
					if (markedChunks.contains(chunk.getPos().asLong())) {
						LOGGER.debug("trying to load unforced chunk " + chunk.getPos().toString() + ", returning chunk loading error");
						//this.reloadChunk(world.getChunkProvider(), chunk.getPos());
						return ChunkHolder.MISSING_CHUNK_FUTURE;
					} else {
						//LOGGER.debug("regular, chunkStatus: " + chunk.getStatus().toString());
						return future.apply(chunk);
					}
				});

	}

	public boolean reloadChunk(ServerChunkProvider provider, ChunkPos pos) {
		ChunkHolder holder = provider.chunkManager.loadedChunks.remove(pos.asLong());
		provider.chunkManager.immutableLoadedChunksDirty = true;
		if (holder != null) {
			provider.chunkManager.chunksToUnload.put(pos.asLong(), holder);
			provider.chunkManager.scheduleSave(pos.asLong(), holder);
			return true;
		} else {
			return false;
		}
	}

	public boolean unloadChunk(ServerChunkProvider provider, ChunkPos pos) {
		this.interestingChunks.add(pos.asLong());
		this.markedChunks.add(pos.asLong());

		return this.reloadChunk(provider, pos);
	}

	public int clear(ServerChunkProvider provider) {
		LinkedList<Long> copy = new LinkedList<>(this.markedChunks);

		int size = this.markedChunks.size();
		this.markedChunks.clear();

		copy.forEach(l -> reForce(provider, new ChunkPos(l)));

		return size;
	}

	public void reForce(ServerChunkProvider provider, ChunkPos pos) {
		provider.forceChunk(pos, true);
		provider.forceChunk(pos, false);
	}

	@SubscribeEvent
	public void chunkUnload(ChunkEvent.Unload event) {
		//LOGGER.debug("Chunk Unload: " + event.getChunk().getPos().toString());
		if (interestingChunks.contains(event.getChunk().getPos().asLong())) {
			LOGGER.info("Interesting Chunk Unload: " + event.getChunk().getPos().toString());
		}
	}

	@SubscribeEvent
	public void chunkLoad(ChunkEvent.Load event) {
		//LOGGER.debug("Chunk Load: " + event.getChunk().getPos().toString());

		ChunkPos pos = event.getChunk().getPos();
		if (interestingChunks.contains(pos.asLong())) {
			LOGGER.info("Interesting Chunk Load: " + pos.toString());
			if (!markedChunks.contains(pos.asLong()))
				interestingChunks.remove(pos.asLong());
		}


	}

}
