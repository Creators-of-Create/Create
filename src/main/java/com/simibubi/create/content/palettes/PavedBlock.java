package com.simibubi.create.content.palettes;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

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
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(COVERED, context.getLevel()
				.getBlockState(context.getClickedPos().above())
				.getBlock() == this);
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction face, BlockState neighbour, LevelAccessor worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if (face == Direction.UP)
			return stateIn.setValue(COVERED, worldIn.getBlockState(facingPos).getBlock() == this);
		return stateIn;
	}

}
