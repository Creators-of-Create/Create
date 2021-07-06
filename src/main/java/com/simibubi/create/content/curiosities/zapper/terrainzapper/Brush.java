package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import java.util.Collection;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IWorld;

public abstract class Brush {

	protected int param0;
	protected int param1;
	protected int param2;
	int amtParams;

	public Brush(int amtParams) {
		this.amtParams = amtParams;
	}

	public void set(int param0, int param1, int param2) {
		this.param0 = param0;
		this.param1 = param1;
		this.param2 = param2;
	}

	public TerrainTools[] getSupportedTools() {
		return TerrainTools.values();
	}
	
	public TerrainTools redirectTool(TerrainTools tool) {
		return tool;
	}

	public boolean hasPlacementOptions() {
		return true;
	}

	public boolean hasConnectivityOptions() {
		return false;
	}

	int getMax(int paramIndex) {
		return Integer.MAX_VALUE;
	}

	int getMin(int paramIndex) {
		return 0;
	}

	ITextComponent getParamLabel(int paramIndex) {
		return Lang
			.translate(paramIndex == 0 ? "generic.width" : paramIndex == 1 ? "generic.height" : "generic.length");
	}

	public int get(int paramIndex) {
		return paramIndex == 0 ? param0 : paramIndex == 1 ? param1 : param2;
	}

	public BlockPos getOffset(Vector3d ray, Direction face, PlacementOptions option) {
		return BlockPos.ZERO;
	}

	public abstract Collection<BlockPos> addToGlobalPositions(IWorld world, BlockPos targetPos, Direction targetFace,
		Collection<BlockPos> affectedPositions, TerrainTools usedTool);

}
