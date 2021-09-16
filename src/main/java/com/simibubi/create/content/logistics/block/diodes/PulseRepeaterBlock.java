package com.simibubi.create.content.logistics.block.diodes;

import java.util.Random;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.TickPriority;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

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
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		if (side == null)
			return false;
		return side.getAxis() == state.getValue(FACING).getAxis();
	}
	
	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
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
	protected int getOutputSignal(BlockGetter worldIn, BlockPos pos, BlockState state) {
		return state.getValue(PULSING) ? 15 : 0;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED, PULSING);
		super.createBlockStateDefinition(builder);
	}

}
