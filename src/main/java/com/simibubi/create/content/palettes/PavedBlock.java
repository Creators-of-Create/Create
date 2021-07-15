package com.simibubi.create.content.palettes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import net.minecraft.block.AbstractBlock.Properties;

public class PavedBlock extends Block {

	public static final BooleanProperty COVERED = BooleanProperty.create("covered");

	public PavedBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(COVERED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(COVERED));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return defaultBlockState().setValue(COVERED, context.getLevel()
				.getBlockState(context.getClickedPos().above())
				.getBlock() == this);
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction face, BlockState neighbour, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (face == Direction.UP)
			return stateIn.setValue(COVERED, worldIn.getBlockState(facingPos).getBlock() == this);
		return stateIn;
	}

}
