package com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.components.structureMovement.ContraptionWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ControlsBlock extends HorizontalDirectionalBlock {

	public static final BooleanProperty OPEN = BooleanProperty.create("open");

	public ControlsBlock(Properties p_54120_) {
		super(p_54120_);
		registerDefaultState(defaultBlockState().setValue(OPEN, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(FACING, OPEN));
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState,
		LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
		return pState.setValue(OPEN, pLevel instanceof ContraptionWorld);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		BlockState state = super.getStateForPlacement(pContext);
		Direction horizontalDirection = pContext.getHorizontalDirection();
		Player player = pContext.getPlayer();

		state = state.setValue(FACING, horizontalDirection.getOpposite());
		if (player != null && player.isSteppingCarefully())
			state = state.setValue(FACING, horizontalDirection);

		return state;
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.CONTROLS.get(pState.getValue(FACING));
	}

}
