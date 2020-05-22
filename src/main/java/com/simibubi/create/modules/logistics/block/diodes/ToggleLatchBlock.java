package com.simibubi.create.modules.logistics.block.diodes;

import java.util.Random;

import com.simibubi.create.AllItems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ToggleLatchBlock extends AbstractDiodeBlock {

	public static BooleanProperty POWERING = BooleanProperty.create("powering");

	public ToggleLatchBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(POWERING, false).with(POWERED, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(POWERED, POWERING, HORIZONTAL_FACING);
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return blockState.get(HORIZONTAL_FACING) == side ? this.getActiveSignal(blockAccess, pos, blockState) : 0;
	}

	@Override
	protected int getDelay(BlockState state) {
		return 1;
	}

	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (!player.isAllowEdit())
			return ActionResultType.PASS;
		if (player.isSneaking())
			return ActionResultType.PASS;
		if (AllItems.WRENCH.typeOf(player.getHeldItem(handIn)))
			return ActionResultType.PASS;
		return activated(worldIn, pos, state);
	}

	@Override
	protected int getActiveSignal(IBlockReader worldIn, BlockPos pos, BlockState state) {
		return state.get(POWERING) ? 15 : 0;
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		boolean poweredPreviously = state.get(POWERED);
		super.scheduledTick(state, worldIn, pos, random);
		BlockState newState = worldIn.getBlockState(pos);
		if (newState.get(POWERED) && !poweredPreviously)
			worldIn.setBlockState(pos, newState.cycle(POWERING), 2);
	}

	protected ActionResultType activated(World worldIn, BlockPos pos, BlockState state) {
		if (!worldIn.isRemote)
			worldIn.setBlockState(pos, state.cycle(POWERING), 2);
		return ActionResultType.SUCCESS;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		if (side == null)
			return false;
		return side.getAxis() == state.get(HORIZONTAL_FACING).getAxis();
	}

}
