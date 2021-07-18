package com.simibubi.create.content.logistics.block.diodes;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.server.ServerWorld;

public class PulseRepeaterBlock extends AbstractDiodeBlock {

	public static BooleanProperty PULSING = BooleanProperty.create("pulsing");

	public PulseRepeaterBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(PULSING, false).setValue(POWERED, false));
	}

	@Override
	protected int getDelay(BlockState state) {
		return 1;
	}
	
	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		if (side == null)
			return false;
		return side.getAxis() == state.getValue(FACING).getAxis();
	}
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		boolean powered = state.getValue(POWERED);
		boolean pulsing = state.getValue(PULSING);
		boolean shouldPower = shouldTurnOn(worldIn, pos, state);

		if (pulsing) {
			worldIn.setBlock(pos, state.setValue(POWERED, shouldPower).setValue(PULSING, false), 2);
		} else if (powered && !shouldPower) {
			worldIn.setBlock(pos, state.setValue(POWERED, false).setValue(PULSING, false), 2);
		} else if (!powered) {
			worldIn.setBlock(pos, state.setValue(POWERED, true).setValue(PULSING, true), 2);
			worldIn.getBlockTicks().scheduleTick(pos, this, this.getDelay(state), TickPriority.HIGH);
		}

	}

	@Override
	protected int getOutputSignal(IBlockReader worldIn, BlockPos pos, BlockState state) {
		return state.getValue(PULSING) ? 15 : 0;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED, PULSING);
		super.createBlockStateDefinition(builder);
	}

}
