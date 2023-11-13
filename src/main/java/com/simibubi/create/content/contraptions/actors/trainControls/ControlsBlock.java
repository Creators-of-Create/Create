package com.simibubi.create.content.contraptions.actors.trainControls;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.ContraptionWorld;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;

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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ControlsBlock extends HorizontalDirectionalBlock implements IWrenchable, ProperWaterloggedBlock {

	public static final BooleanProperty OPEN = BooleanProperty.create("open");
	public static final BooleanProperty VIRTUAL = BooleanProperty.create("virtual");

	public ControlsBlock(Properties p_54120_) {
		super(p_54120_);
		registerDefaultState(defaultBlockState().setValue(OPEN, false)
			.setValue(WATERLOGGED, false)
			.setValue(VIRTUAL, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(FACING, OPEN, WATERLOGGED, VIRTUAL));
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState,
		LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
		updateWater(pLevel, pState, pCurrentPos);
		return pState.setValue(OPEN, pLevel instanceof ContraptionWorld);
	}

	@Override
	public FluidState getFluidState(BlockState pState) {
		return fluidState(pState);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		BlockState state = withWater(super.getStateForPlacement(pContext), pContext);
		Direction horizontalDirection = pContext.getHorizontalDirection();
		Player player = pContext.getPlayer();

		state = state.setValue(FACING, horizontalDirection.getOpposite());
		if (player != null && player.isShiftKeyDown())
			state = state.setValue(FACING, horizontalDirection);

		return state;
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.CONTROLS.get(pState.getValue(FACING));
	}

}
