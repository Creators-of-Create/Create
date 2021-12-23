package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.FlywheelWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class PlacementSimulationWorld extends WrappedWorld implements FlywheelWorld {
	public Map<BlockPos, BlockState> blocksAdded = new HashMap<>();
	public Map<BlockPos, BlockEntity> tesAdded = new HashMap<>();

	public Set<SectionPos> spannedSections = new HashSet<>();
	public LevelLightEngine lighter;
	public final WrappedChunkProvider chunkSource;
	private final BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();

	public PlacementSimulationWorld(Level wrapped) {
		this(wrapped, new WrappedChunkProvider());
	}

	public PlacementSimulationWorld(Level wrapped, WrappedChunkProvider chunkSource) {
		super(wrapped, chunkSource);
		// You can't leak this before the super ctor is called.
		// You can't create inner classes before super ctor is called.
		chunkSource.setPlacementWorld(this);
		this.chunkSource = chunkSource;
		lighter = new LevelLightEngine(chunkSource, true, false);
	}

	/**
	 * Run this after you're done using setBlock().
	 */
	public void runLightingEngine() {
		for (Map.Entry<BlockPos, BlockState> entry : blocksAdded.entrySet()) {
			BlockPos pos = entry.getKey();
			BlockState state = entry.getValue();
			int light = state.getLightEmission(this, pos);
			if (light > 0) {
				lighter.onBlockEmissionIncrease(pos, light);
			}
		}

		lighter.runUpdates(Integer.MAX_VALUE, false, false);
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return lighter;
	}

	public void setTileEntities(Collection<BlockEntity> tileEntities) {
		tesAdded.clear();
		tileEntities.forEach(te -> tesAdded.put(te.getBlockPos(), te));
	}

	public void clear() {
		blocksAdded.clear();
	}

	@Override
	public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
		blocksAdded.put(pos, newState);

		SectionPos sectionPos = SectionPos.of(pos);
		if (spannedSections.add(sectionPos)) {
			lighter.updateSectionStatus(sectionPos, false);
		}

		if ((flags & Block.UPDATE_SUPPRESS_LIGHT) == 0) {
			lighter.checkBlock(pos);
		}

		return true;
	}

	@Override
	public boolean setBlockAndUpdate(BlockPos pos, BlockState state) {
		return setBlock(pos, state, 0);
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		return tesAdded.get(pos);
	}

	@Override
	public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> condition) {
		return condition.test(getBlockState(pos));
	}

	@Override
	public boolean isLoaded(BlockPos pos) {
		return true;
	}

	@Override
	public boolean isAreaLoaded(BlockPos center, int range) {
		return true;
	}

	public BlockState getBlockState(int x, int y, int z) {
		return getBlockState(scratch.set(x, y, z));
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		BlockState state = blocksAdded.get(pos);
		if (state != null)
			return state;
		return Blocks.AIR.defaultBlockState();
	}
}
