package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Predicate;

import com.simibubi.create.foundation.render.backend.instancing.IFlywheelWorld;
import com.simibubi.create.foundation.utility.worldWrappers.chunk.WrappedChunk;

import ca.spottedleaf.starlight.common.light.VariableBlockLightHandler;
import ca.spottedleaf.starlight.common.light.VariableBlockLightHandlerImpl;
import ca.spottedleaf.starlight.common.world.ExtendedWorld;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.lighting.WorldLightManager;

public class PlacementSimulationWorld extends WrappedWorld implements IFlywheelWorld {
	public HashMap<BlockPos, BlockState> blocksAdded;
	public HashMap<BlockPos, TileEntity> tesAdded;

	public HashSet<SectionPos> spannedChunks;
	public WorldLightManager lighter;
	public WrappedChunkProvider chunkProvider;
	private final BlockPos.Mutable scratch = new BlockPos.Mutable();

	public PlacementSimulationWorld(World wrapped) {
		this(wrapped, new WrappedChunkProvider(WrappedChunk::new));
	}

	public PlacementSimulationWorld(World wrapped, WrappedChunkProvider chunkProvider) {
		super(wrapped, chunkProvider);
		this.chunkProvider = chunkProvider.setWorld(this);
		spannedChunks = new HashSet<>();
		lighter = new WorldLightManager(chunkProvider, true, false); // blockLight, skyLight
		blocksAdded = new HashMap<>();
		tesAdded = new HashMap<>();
	}

	@Override
	public WorldLightManager getLightingProvider() {
		return lighter;
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

		SectionPos sectionPos = SectionPos.from(pos);

		if (spannedChunks.add(sectionPos)) {
			lighter.updateSectionStatus(sectionPos, false);
		}

		lighter.checkBlock(pos);

		blocksAdded.put(pos, newState);
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
		else
			return Blocks.AIR.getDefaultState();
	}

}
