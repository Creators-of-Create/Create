package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.contraptions.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class AdjustablePulleyBlock extends EncasedBeltBlock {

	public static BooleanProperty POWERED = BlockStateProperties.POWERED;

	public AdjustablePulleyBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POWERED));
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, worldIn, pos, oldState, isMoving);
		if (oldState.getBlock() == state.getBlock())
			return;
		withBlockEntityDo(worldIn, pos, kte -> ((AdjustablePulleyBlockEntity) kte).neighbourChanged());
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(POWERED, context.getLevel()
			.hasNeighborSignal(context.getClickedPos()));
	}

	@Override
	protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
		return super.areStatesKineticallyEquivalent(oldState, newState)
			&& oldState.getValue(POWERED) == newState.getValue(POWERED);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isClientSide)
			return;

		withBlockEntityDo(worldIn, pos, kte -> ((AdjustablePulleyBlockEntity) kte).neighbourChanged());

		boolean previouslyPowered = state.getValue(POWERED);
		if (previouslyPowered != worldIn.hasNeighborSignal(pos))
			worldIn.setBlock(pos, state.cycle(POWERED), 18);
	}

	@Override
	public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.ADJUSTABLE_PULLEY.get();
	}

}
