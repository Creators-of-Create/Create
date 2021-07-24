package com.simibubi.create.content.contraptions.fluids.actors;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class HosePulleyBlock extends HorizontalKineticBlock implements ITE<HosePulleyTileEntity> {

	public HosePulleyBlock(Properties properties) {
		super(properties);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(HORIZONTAL_FACING)
			.getClockWise()
			.getAxis();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction preferredHorizontalFacing = getPreferredHorizontalFacing(context);
		return this.defaultBlockState()
			.setValue(HORIZONTAL_FACING,
				preferredHorizontalFacing != null ? preferredHorizontalFacing.getCounterClockWise()
					: context.getHorizontalDirection()
						.getOpposite());
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return state.getValue(HORIZONTAL_FACING)
			.getClockWise() == face;
	}

	public static boolean hasPipeTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return state.getValue(HORIZONTAL_FACING)
			.getCounterClockWise() == face;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.HOSE_PULLEY.create();
	}

	@Override
	public Direction getPreferredHorizontalFacing(BlockItemUseContext context) {
		Direction fromParent = super.getPreferredHorizontalFacing(context);
		if (fromParent != null)
			return fromParent;

		Direction prefferedSide = null;
		for (Direction facing : Iterate.horizontalDirections) {
			BlockPos pos = context.getClickedPos()
				.relative(facing);
			BlockState blockState = context.getLevel()
				.getBlockState(pos);
			if (FluidPipeBlock.canConnectTo(context.getLevel(), pos, blockState, facing))
				if (prefferedSide != null && prefferedSide.getAxis() != facing.getAxis()) {
					prefferedSide = null;
					break;
				} else
					prefferedSide = facing;
		}
		return prefferedSide == null ? null : prefferedSide.getOpposite();
	}

	@Override
	public void onRemove(BlockState p_196243_1_, World world, BlockPos pos, BlockState p_196243_4_,
		boolean p_196243_5_) {
		if (p_196243_1_.hasTileEntity()
			&& (p_196243_1_.getBlock() != p_196243_4_.getBlock() || !p_196243_4_.hasTileEntity())) {
			TileEntityBehaviour.destroy(world, pos, FluidDrainingBehaviour.TYPE);
			TileEntityBehaviour.destroy(world, pos, FluidFillingBehaviour.TYPE);
			world.removeBlockEntity(pos);
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.PULLEY.get(state.getValue(HORIZONTAL_FACING)
			.getClockWise()
			.getAxis());
	}

	@Override
	public Class<HosePulleyTileEntity> getTileEntityClass() {
		return HosePulleyTileEntity.class;
	}

}
