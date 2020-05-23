package com.simibubi.create.content.palettes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class PavedBlock extends Block {

	public static final BooleanProperty COVERED = BooleanProperty.create("covered");

	public PavedBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(COVERED, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(COVERED));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(COVERED, context.getWorld()
				.getBlockState(context.getPos().up())
				.getBlock() == this);
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction face, BlockState neighbour, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (face == Direction.UP)
			return stateIn.with(COVERED, worldIn.getBlockState(facingPos).getBlock() == this);
		return stateIn;
	}

}
