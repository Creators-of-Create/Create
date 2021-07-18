package com.simibubi.create.content.logistics.block.diodes;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class PoweredLatchBlock extends ToggleLatchBlock {

	public static BooleanProperty POWERED_SIDE = BooleanProperty.create("powered_side");

	public PoweredLatchBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED_SIDE, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(POWERED_SIDE));
	}

	@Override
	protected void checkTickOnNeighbor(World worldIn, BlockPos pos, BlockState state) {
		boolean back = state.getValue(POWERED);
		boolean shouldBack = shouldTurnOn(worldIn, pos, state);
		boolean side = state.getValue(POWERED_SIDE);
		boolean shouldSide = isPoweredOnSides(worldIn, pos, state);

		TickPriority tickpriority = TickPriority.HIGH;
		if (this.shouldPrioritize(worldIn, pos, state))
			tickpriority = TickPriority.EXTREMELY_HIGH;
		else if (side || back)
			tickpriority = TickPriority.VERY_HIGH;

		if (worldIn.getBlockTicks().willTickThisTick(pos, this))
			return;
		if (back != shouldBack || side != shouldSide)
			worldIn.getBlockTicks().scheduleTick(pos, this, this.getDelay(state), tickpriority);
	}

	protected boolean isPoweredOnSides(World worldIn, BlockPos pos, BlockState state) {
		Direction direction = state.getValue(FACING);
		Direction left = direction.getClockWise();
		Direction right = direction.getCounterClockWise();

		for (Direction d : new Direction[] { left, right }) {
			BlockPos blockpos = pos.relative(d);
			int i = worldIn.getSignal(blockpos, d);
			if (i > 0)
				return true;
			BlockState blockstate = worldIn.getBlockState(blockpos);
			if (blockstate.getBlock() == Blocks.REDSTONE_WIRE && blockstate.getValue(RedstoneWireBlock.POWER) > 0)
				return true;
		}
		return false;
	}

	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		boolean back = state.getValue(POWERED);
		boolean shouldBack = this.shouldTurnOn(worldIn, pos, state);
		boolean side = state.getValue(POWERED_SIDE);
		boolean shouldSide = isPoweredOnSides(worldIn, pos, state);
		BlockState stateIn = state;

		if (back != shouldBack) {
			state = state.setValue(POWERED, shouldBack);
			if (shouldBack)
				state = state.setValue(POWERING, true);
			else if (side)
				state = state.setValue(POWERING, false);
		}

		if (side != shouldSide) {
			state = state.setValue(POWERED_SIDE, shouldSide);
			if (shouldSide)
				state = state.setValue(POWERING, false);
			else if (back)
				state = state.setValue(POWERING, true);
		}

		if (state != stateIn)
			worldIn.setBlock(pos, state, 2);
	}

	@Override
	protected ActionResultType activated(World worldIn, BlockPos pos, BlockState state) {
		if (state.getValue(POWERED) != state.getValue(POWERED_SIDE))
			return ActionResultType.PASS;
		if (!worldIn.isClientSide)
			worldIn.setBlock(pos, state.cycle(POWERING), 2);
		return ActionResultType.SUCCESS;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		if (side == null)
			return false;
		return side.getAxis().isHorizontal();
	}

}
