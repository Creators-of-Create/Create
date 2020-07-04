package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class EncasedShaftBlock extends RotatedPillarKineticBlock {

	public EncasedShaftBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean shouldCheckWeakPower(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
		return false;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.ENCASED_SHAFT.create();
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.PUSH_ONLY;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		if (context.getPlayer() != null && context.getPlayer()
			.isSneaking())
			return super.getStateForPlacement(context);
		Axis preferredAxis = getPreferredAxis(context);
		return this.getDefaultState()
			.with(AXIS, preferredAxis == null ? context.getNearestLookingDirection()
				.getAxis() : preferredAxis);
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.get(AXIS);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(AXIS);
	}

}
