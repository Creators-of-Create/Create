package com.simibubi.create.content.logistics.block.redstone;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.elevator.ElevatorColumn;
import com.simibubi.create.content.contraptions.components.structureMovement.elevator.ElevatorColumn.ColumnCoords;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneContactItem extends BlockItem {

	public RedstoneContactItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Override
	protected BlockState getPlacementState(BlockPlaceContext ctx) {
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState state = super.getPlacementState(ctx);

		if (state == null)
			return state;
		if (!(state.getBlock() instanceof RedstoneContactBlock))
			return state;
		Direction facing = state.getValue(RedstoneContactBlock.FACING);
		if (facing.getAxis() == Axis.Y)
			return state;

		if (ElevatorColumn.get(world, new ColumnCoords(pos.getX(), pos.getZ(), facing)) == null)
			return state;

		return BlockHelper.copyProperties(state, AllBlocks.ELEVATOR_CONTACT.getDefaultState());
	}

}
