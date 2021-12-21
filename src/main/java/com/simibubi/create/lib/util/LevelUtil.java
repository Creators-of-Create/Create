package com.simibubi.create.lib.util;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public class LevelUtil {
	public static boolean isAreaLoaded(LevelAccessor world, BlockPos center, int range) {
		return world.hasChunksAt(center.offset(-range, -range, -range), center.offset(range, range, range));
	}

	public static void markAndNotifyBlock(Level level, BlockPos pos, @Nullable LevelChunk levelchunk,
										  BlockState oldState, BlockState newState, int flags, int recursionLeft) {
		Block block = newState.getBlock();
		BlockState oldState1 = level.getBlockState(pos);
		if (oldState1 == newState) {
			if (oldState != oldState1) {
				level.setBlocksDirty(pos, oldState, oldState1);
			}

			if ((flags & 2) != 0 && (!level.isClientSide || (flags & 4) == 0) && (level.isClientSide || levelchunk.getFullStatus() != null && levelchunk.getFullStatus().isOrAfter(ChunkHolder.FullChunkStatus.TICKING))) {
				level.sendBlockUpdated(pos, oldState, newState, flags);
			}

			if ((flags & 1) != 0) {
				level.blockUpdated(pos, oldState.getBlock());
				if (!level.isClientSide && newState.hasAnalogOutputSignal()) {
					level.updateNeighbourForOutputSignal(pos, block);
				}
			}

			if ((flags & 16) == 0 && recursionLeft > 0) {
				int i = flags & -34;
				oldState.updateIndirectNeighbourShapes(level, pos, i, recursionLeft - 1);
				newState.updateNeighbourShapes(level, pos, i, recursionLeft - 1);
				newState.updateIndirectNeighbourShapes(level, pos, i, recursionLeft - 1);
			}

			level.onBlockStateChange(pos, oldState, oldState1);
		}
	}
}
