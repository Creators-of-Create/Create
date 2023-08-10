package com.simibubi.create.content.redstone.diodes;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.ticks.TickPriority;

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
	protected void checkTickOnNeighbor(Level worldIn, BlockPos pos, BlockState state) {
		boolean back = state.getValue(POWERED);
		boolean shouldBack = shouldTurnOn(worldIn, pos, state);
		boolean side = state.getValue(POWERED_SIDE);
		boolean shouldSide = isPoweredOnSides(worldIn, pos, state);

		TickPriority tickpriority = TickPriority.HIGH;
		if (this.shouldPrioritize(worldIn, pos, state))
			tickpriority = TickPriority.EXTREMELY_HIGH;
		else if (side || back)
			tickpriority = TickPriority.VERY_HIGH;

		if (worldIn.getBlockTicks()
			.willTickThisTick(pos, this))
			return;
		if (back != shouldBack || side != shouldSide)
			worldIn.scheduleTick(pos, this, this.getDelay(state), tickpriority);
	}

	protected boolean isPoweredOnSides(Level worldIn, BlockPos pos, BlockState state) {
		Direction direction = state.getValue(FACING);
		Direction left = direction.getClockWise();
		Direction right = direction.getCounterClockWise();

		for (Direction d : new Direction[] { left, right }) {
			BlockPos blockpos = pos.relative(d);
			int i = worldIn.getSignal(blockpos, d);
			if (i > 0)
				return true;
			BlockState blockstate = worldIn.getBlockState(blockpos);
			if (blockstate.getBlock() == Blocks.REDSTONE_WIRE && blockstate.getValue(RedStoneWireBlock.POWER) > 0)
				return true;
		}
		return false;
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
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
	protected InteractionResult activated(Level worldIn, BlockPos pos, BlockState state) {
		if (state.getValue(POWERED) != state.getValue(POWERED_SIDE))
			return InteractionResult.PASS;
		if (!worldIn.isClientSide) {
			float f = !state.getValue(POWERING) ? 0.6F : 0.5F;
			worldIn.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
			worldIn.setBlock(pos, state.cycle(POWERING), 2);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
		if (side == null)
			return false;
		return side.getAxis()
			.isHorizontal();
	}

}
