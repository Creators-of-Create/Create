package com.simibubi.create.modules.logistics.block.diodes;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;

public class LatchBlock extends ToggleLatchBlock {

	public static BooleanProperty POWERED_SIDE = BooleanProperty.create("powered_side");

	public LatchBlock() {
		setDefaultState(getDefaultState().with(POWERED_SIDE, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(POWERED_SIDE));
	}

	@Override
	protected void updateState(World worldIn, BlockPos pos, BlockState state) {
		boolean back = state.get(POWERED);
		boolean shouldBack = this.shouldBePowered(worldIn, pos, state);
		boolean side = state.get(POWERED_SIDE);
		boolean shouldSide = getPowerOnSides(worldIn, pos, state) > 0;

		TickPriority tickpriority = TickPriority.HIGH;
		if (this.isFacingTowardsRepeater(worldIn, pos, state))
			tickpriority = TickPriority.EXTREMELY_HIGH;
		else if (side || back)
			tickpriority = TickPriority.VERY_HIGH;

		if (worldIn.getPendingBlockTicks().isTickPending(pos, this))
			return;
		if (back != shouldBack || side != shouldSide)
			worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.getDelay(state), tickpriority);
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
		boolean back = state.get(POWERED);
		boolean shouldBack = this.shouldBePowered(worldIn, pos, state);
		boolean side = state.get(POWERED_SIDE);
		boolean shouldSide = getPowerOnSides(worldIn, pos, state) > 0;
		BlockState stateIn = state;

		if (back != shouldBack) {
			state = state.with(POWERED, shouldBack);
			if (shouldBack)
				state = state.with(POWERING, true);
			else if (side)
				state = state.with(POWERING, false);
		}

		if (side != shouldSide) {
			state = state.with(POWERED_SIDE, shouldSide);
			if (shouldSide)
				state = state.with(POWERING, false);
			else if (back)
				state = state.with(POWERING, true);
		}

		if (state != stateIn)
			worldIn.setBlockState(pos, state, 2);
	}

	@Override
	protected boolean activated(World worldIn, BlockPos pos, BlockState state) {
		if (state.get(POWERED) != state.get(POWERED_SIDE))
			return false;
		if (!worldIn.isRemote)
			worldIn.setBlockState(pos, state.cycle(POWERING), 2);
		return true;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		if (side == null)
			return false;
		return side.getAxis().isHorizontal();
	}

}
