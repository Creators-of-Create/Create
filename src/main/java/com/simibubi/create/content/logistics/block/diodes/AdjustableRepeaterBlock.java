package com.simibubi.create.content.logistics.block.diodes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import net.minecraft.block.AbstractBlock.Properties;

public class AdjustableRepeaterBlock extends AbstractDiodeBlock {

	public static BooleanProperty POWERING = BooleanProperty.create("powering");

	public AdjustableRepeaterBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false)
			.setValue(POWERING, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWERED, POWERING, FACING);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllBlocks.ADJUSTABLE_REPEATER.is(this) ? AllTileEntities.ADJUSTABLE_REPEATER.create()
			: AllTileEntities.ADJUSTABLE_PULSE_REPEATER.create();
	}

	@Override
	protected int getOutputSignal(IBlockReader worldIn, BlockPos pos, BlockState state) {
		return state.getValue(POWERING) ? 15 : 0;
	}

	@Override
	public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return blockState.getValue(FACING) == side ? this.getOutputSignal(blockAccess, pos, blockState) : 0;
	}

	@Override
	protected int getDelay(BlockState p_196346_1_) {
		return 0;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		if (side == null)
			return false;
		return side.getAxis() == state.getValue(FACING)
			.getAxis();
	}

}
