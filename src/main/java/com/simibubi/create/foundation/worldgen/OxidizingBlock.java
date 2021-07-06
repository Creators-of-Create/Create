package com.simibubi.create.foundation.worldgen;

import java.util.LinkedList;
import java.util.OptionalDouble;
import java.util.Random;

import com.simibubi.create.content.curiosities.tools.SandPaperItem;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class OxidizingBlock extends Block {

	public static final IntegerProperty OXIDIZATION = IntegerProperty.create("oxidization", 0, 7);
	private float chance;

	public OxidizingBlock(Properties properties, float chance) {
		super(properties);
		this.chance = chance;
		setDefaultState(getDefaultState().with(OXIDIZATION, 0));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(OXIDIZATION));
	}

	@Override
	public boolean ticksRandomly(BlockState state) {
		return super.ticksRandomly(state) || state.get(OXIDIZATION) < 7;
	}

	@Override
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if (worldIn.getRandom()
			.nextFloat() <= chance) {
			int currentState = state.get(OXIDIZATION);
			boolean canIncrease = false;
			LinkedList<Integer> neighbors = new LinkedList<>();
			for (Direction facing : Iterate.directions) {
				BlockPos neighbourPos = pos.offset(facing);
				if (!worldIn.isAreaLoaded(neighbourPos, 0))
					continue;
				if (!worldIn.isBlockPresent(neighbourPos))
					continue;
				BlockState neighborState = worldIn.getBlockState(neighbourPos);
				if (neighborState.contains(OXIDIZATION) && neighborState.get(OXIDIZATION) != 0) {
					neighbors.add(neighborState.get(OXIDIZATION));
				}
				if (BlockHelper.hasBlockSolidSide(neighborState, worldIn, neighbourPos, facing.getOpposite())) {
					continue;
				}
				canIncrease = true;
			}
			if (canIncrease) {
				OptionalDouble average = neighbors.stream()
					.mapToInt(v -> v)
					.average();
				if (average.orElse(7d) >= currentState)
					worldIn.setBlockState(pos, state.with(OXIDIZATION, Math.min(currentState + 1, 7)));
			}
		}
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult blockRayTraceResult) {
		if (state.get(OXIDIZATION) > 0 && player.getHeldItem(hand)
			.getItem() instanceof SandPaperItem) {
			if (!player.isCreative())
				player.getHeldItem(hand)
					.damageItem(1, player, p -> p.sendBreakAnimation(p.getActiveHand()));
			world.setBlockState(pos, state.with(OXIDIZATION, 0));
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}
}
