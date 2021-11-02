package com.simibubi.create.content.logistics.block.diodes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class AdjustableRepeaterBlock extends AbstractDiodeBlock implements ITE<AdjustableRepeaterTileEntity> {

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
	protected int getOutputSignal(BlockGetter worldIn, BlockPos pos, BlockState state) {
		return state.getValue(POWERING) ? 15 : 0;
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return blockState.getValue(FACING) == side ? this.getOutputSignal(blockAccess, pos, blockState) : 0;
	}

	@Override
	protected int getDelay(BlockState p_196346_1_) {
		return 0;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		if (side == null)
			return false;
		return side.getAxis() == state.getValue(FACING)
			.getAxis();
	}

	@Override
	public Class<AdjustableRepeaterTileEntity> getTileEntityClass() {
		return AdjustableRepeaterTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends AdjustableRepeaterTileEntity> getTileEntityType() {
		return AllBlocks.ADJUSTABLE_REPEATER.is(this) ? AllTileEntities.ADJUSTABLE_REPEATER.get()
			: AllTileEntities.ADJUSTABLE_PULSE_REPEATER.get();
	}

}
