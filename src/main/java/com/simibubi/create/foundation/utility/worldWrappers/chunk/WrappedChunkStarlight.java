package com.simibubi.create.foundation.utility.worldWrappers.chunk;

import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import ca.spottedleaf.starlight.common.chunk.ExtendedChunk;
import ca.spottedleaf.starlight.common.light.SWMRNibbleArray;
import ca.spottedleaf.starlight.common.light.StarLightEngine;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.IChunk;

public class WrappedChunkStarlight extends WrappedChunk implements ExtendedChunk {

	private SWMRNibbleArray[] blockNibbles;
	private SWMRNibbleArray[] skyNibbles;
	private boolean[] skyEmptinessMap;
	private boolean[] blockEmptinessMap;

	public WrappedChunkStarlight(PlacementSimulationWorld world, int x, int z) {
		super(world, x, z);

		this.blockNibbles = StarLightEngine.getFilledEmptyLight(world);
		this.skyNibbles = StarLightEngine.getFilledEmptyLight(world);
		this.skyEmptinessMap = getEmptySectionsForChunk(this);
		this.blockEmptinessMap = getEmptySectionsForChunk(this);
	}

	@Override
	public SWMRNibbleArray[] getBlockNibbles() {
		return blockNibbles;
	}

	@Override
	public void setBlockNibbles(SWMRNibbleArray[] swmrNibbleArrays) {
		this.blockNibbles = swmrNibbleArrays;
	}

	@Override
	public SWMRNibbleArray[] getSkyNibbles() {
		return skyNibbles;
	}

	@Override
	public void setSkyNibbles(SWMRNibbleArray[] swmrNibbleArrays) {
		this.skyNibbles = swmrNibbleArrays;
	}

	@Override
	public boolean[] getSkyEmptinessMap() {
		return skyEmptinessMap;
	}

	@Override
	public void setSkyEmptinessMap(boolean[] booleans) {
		this.skyEmptinessMap = booleans;
	}

	@Override
	public boolean[] getBlockEmptinessMap() {
		return blockEmptinessMap;
	}

	@Override
	public void setBlockEmptinessMap(boolean[] booleans) {
		this.blockEmptinessMap = booleans;
	}

	public static boolean[] getEmptySectionsForChunk(IChunk chunk) {
		ChunkSection[] sections = chunk.getSections();
		boolean[] ret = new boolean[sections.length];

		for (int i = 0; i < sections.length; ++i) {
			ret[i] = sections[i] == null || sections[i].isEmpty();
		}

		return ret;
	}
}
