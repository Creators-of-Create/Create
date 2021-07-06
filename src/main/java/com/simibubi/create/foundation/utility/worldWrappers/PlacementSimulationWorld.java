package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.jozufozu.flywheel.backend.IFlywheelWorld;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.World;
import net.minecraft.world.lighting.WorldLightManager;

public class PlacementSimulationWorld extends WrappedWorld implements IFlywheelWorld {
	public Map<BlockPos, BlockState> blocksAdded;
	public Map<BlockPos, TileEntity> tesAdded;

	public Set<SectionPos> spannedSections;
	public WorldLightManager lighter;
	public WrappedChunkProvider chunkProvider;
	private final BlockPos.Mutable scratch = new BlockPos.Mutable();

	public PlacementSimulationWorld(World wrapped) {
		this(wrapped, new WrappedChunkProvider());
	}

	public PlacementSimulationWorld(World wrapped, WrappedChunkProvider chunkProvider) {
		super(wrapped, chunkProvider);
		this.chunkProvider = chunkProvider.setWorld(this);
		spannedSections = new HashSet<>();
		lighter = new WorldLightManager(chunkProvider, true, false); // blockLight, skyLight
		blocksAdded = new HashMap<>();
		tesAdded = new HashMap<>();
	}

	@Override
	public WorldLightManager getLightingProvider() {
		return lighter;
	}

	public void updateLightSources() {
		for (Map.Entry<BlockPos, BlockState> entry : blocksAdded.entrySet()) {
			BlockPos pos = entry.getKey();
			BlockState state = entry.getValue();
			int light = state.getLightValue(this, pos);
			if (light > 0) {
				lighter.func_215573_a(pos, light);
			}
		}
	}

	public void setTileEntities(Collection<TileEntity> tileEntities) {
		tesAdded.clear();
		tileEntities.forEach(te -> tesAdded.put(te.getPos(), te));
	}

	public void clear() {
		blocksAdded.clear();
	}

	@Override
	public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
		blocksAdded.put(pos, newState);

		SectionPos sectionPos = SectionPos.from(pos);
		if (spannedSections.add(sectionPos)) {
			lighter.updateSectionStatus(sectionPos, false);
		}

		if ((flags & 128) == 0) {
			lighter.checkBlock(pos);
		}

		return true;
	}

	@Override
	public boolean setBlockState(BlockPos pos, BlockState state) {
		return setBlockState(pos, state, 0);
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return tesAdded.get(pos);
	}

	@Override
	public boolean hasBlockState(BlockPos pos, Predicate<BlockState> condition) {
		return condition.test(getBlockState(pos));
	}

	@Override
	public boolean isBlockPresent(BlockPos pos) {
		return true;
	}

	@Override
	public boolean isAreaLoaded(BlockPos center, int range) {
		return true;
	}

	public BlockState getBlockState(int x, int y, int z) {
		return getBlockState(scratch.setPos(x, y, z));
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		BlockState state = blocksAdded.get(pos);
		if (state != null)
			return state;
		return Blocks.AIR.getDefaultState();
	}
}
