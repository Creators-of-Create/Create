package com.simibubi.create.content.curiosities.girder;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class GirderCTBehaviour extends ConnectedTextureBehaviour {

	@Override
	public CTSpriteShiftEntry get(BlockState state, Direction direction) {
		if (!state.hasProperty(GirderBlock.X))
			return null;
		return !state.getValue(GirderBlock.X) && !state.getValue(GirderBlock.Z) && direction.getAxis() != Axis.Y
			? AllSpriteShifts.GIRDER_POLE
			: null;
	}

	@Override
	public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
		BlockPos otherPos, Direction face) {
		if (other.getBlock() != state.getBlock())
			return false;
		return !other.getValue(GirderBlock.X) && !other.getValue(GirderBlock.Z);
	}

}
