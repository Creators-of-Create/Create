package com.simibubi.create.content.contraptions.components.press;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class MechanicalPressBlock extends HorizontalKineticBlock implements ITE<MechanicalPressTileEntity> {

	public MechanicalPressBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		if (context.getEntity() instanceof Player)
			return AllShapes.CASING_14PX.get(Direction.DOWN);
		return AllShapes.MECHANICAL_PROCESSOR_SHAPE;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		return !AllBlocks.BASIN.has(worldIn.getBlockState(pos.below()));
	}

	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return AllTileEntities.MECHANICAL_PRESS.create();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction prefferedSide = getPreferredHorizontalFacing(context);
		if (prefferedSide != null)
			return defaultBlockState().setValue(HORIZONTAL_FACING, prefferedSide);
		return super.getStateForPlacement(context);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(HORIZONTAL_FACING)
			.getAxis();
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.getValue(HORIZONTAL_FACING)
			.getAxis();
	}

	@Override
	public Class<MechanicalPressTileEntity> getTileEntityClass() {
		return MechanicalPressTileEntity.class;
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

}
