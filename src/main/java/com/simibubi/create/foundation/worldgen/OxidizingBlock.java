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

import net.minecraft.block.AbstractBlock.Properties;

public class OxidizingBlock extends Block {

	public static final IntegerProperty OXIDIZATION = IntegerProperty.create("oxidization", 0, 7);
	private float chance;

	public OxidizingBlock(Properties properties, float chance) {
		super(properties);
		this.chance = chance;
		registerDefaultState(defaultBlockState().setValue(OXIDIZATION, 0));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(OXIDIZATION));
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return super.isRandomlyTicking(state) || state.getValue(OXIDIZATION) < 7;
	}

	@Override
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if (worldIn.getRandom()
			.nextFloat() <= chance) {
			int currentState = state.getValue(OXIDIZATION);
			boolean canIncrease = false;
			LinkedList<Integer> neighbors = new LinkedList<>();
			for (Direction facing : Iterate.directions) {
				BlockPos neighbourPos = pos.relative(facing);
				if (!worldIn.isAreaLoaded(neighbourPos, 0))
					continue;
				if (!worldIn.isLoaded(neighbourPos))
					continue;
				BlockState neighborState = worldIn.getBlockState(neighbourPos);
				if (neighborState.hasProperty(OXIDIZATION) && neighborState.getValue(OXIDIZATION) != 0) {
					neighbors.add(neighborState.getValue(OXIDIZATION));
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
					worldIn.setBlockAndUpdate(pos, state.setValue(OXIDIZATION, Math.min(currentState + 1, 7)));
			}
		}
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult blockRayTraceResult) {
		if (state.getValue(OXIDIZATION) > 0 && player.getItemInHand(hand)
			.getItem() instanceof SandPaperItem) {
			if (!player.isCreative())
				player.getItemInHand(hand)
					.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(p.getUsedItemHand()));
			world.setBlockAndUpdate(pos, state.setValue(OXIDIZATION, 0));
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}
}
