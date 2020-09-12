package com.simibubi.create.content.logistics.block.funnel;

import com.simibubi.create.AllBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BrassFunnelBlock extends FunnelBlock {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public BrassFunnelBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
		setDefaultState(getDefaultState().with(POWERED, false));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return super.getStateForPlacement(context).with(POWERED, context.getWorld()
			.isBlockPowered(context.getPos()));
	}

	@Override
	protected boolean canInsertIntoFunnel(BlockState state) {
		return super.canInsertIntoFunnel(state) && !state.get(POWERED);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(POWERED));
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isRemote)
			return;
		boolean previouslyPowered = state.get(POWERED);
		if (previouslyPowered != worldIn.isBlockPowered(pos))
			worldIn.setBlockState(pos, state.cycle(POWERED), 2);
	}

	@Override
	public BlockState getEquivalentBeltFunnel(IBlockReader world, BlockPos pos, BlockState state) {
		Direction facing = state.get(FACING);
		return AllBlocks.BRASS_BELT_FUNNEL.getDefaultState()
			.with(BeltFunnelBlock.HORIZONTAL_FACING, facing)
			.with(POWERED, state.get(POWERED));
	}

}
