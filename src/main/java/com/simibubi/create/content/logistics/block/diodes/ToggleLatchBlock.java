package com.simibubi.create.content.logistics.block.diodes;

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

import net.minecraft.block.AbstractBlock.Properties;

public class ToggleLatchBlock extends AbstractDiodeBlock {

	public static BooleanProperty POWERING = BooleanProperty.create("powering");

	public ToggleLatchBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERING, false)
			.setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWERED, POWERING, FACING);
	}

	@Override
	public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return blockState.getValue(FACING) == side ? this.getOutputSignal(blockAccess, pos, blockState) : 0;
	}

	@Override
	protected int getDelay(BlockState state) {
		return 1;
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		if (!player.mayBuild())
			return ActionResultType.PASS;
		if (player.isShiftKeyDown())
			return ActionResultType.PASS;
		if (AllItems.WRENCH.isIn(player.getItemInHand(handIn)))
			return ActionResultType.PASS;
		return activated(worldIn, pos, state);
	}

	@Override
	protected int getOutputSignal(IBlockReader worldIn, BlockPos pos, BlockState state) {
		return state.getValue(POWERING) ? 15 : 0;
	}

	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		boolean poweredPreviously = state.getValue(POWERED);
		super.tick(state, worldIn, pos, random);
		BlockState newState = worldIn.getBlockState(pos);
		if (newState.getValue(POWERED) && !poweredPreviously)
			worldIn.setBlock(pos, newState.cycle(POWERING), 2);
	}

	protected ActionResultType activated(World worldIn, BlockPos pos, BlockState state) {
		if (!worldIn.isClientSide)
			worldIn.setBlock(pos, state.cycle(POWERING), 2);
		return ActionResultType.SUCCESS;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		if (side == null)
			return false;
		return side.getAxis() == state.getValue(FACING)
			.getAxis();
	}

}
