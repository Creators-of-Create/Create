package com.simibubi.create.content.curiosities;

import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationServerWorld;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import net.minecraft.item.Item.Properties;

public class TreeFertilizerItem extends Item {

	public TreeFertilizerItem(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResultType useOn(ItemUseContext context) {
		BlockState state = context.getLevel()
			.getBlockState(context.getClickedPos());
		Block block = state.getBlock();
		if (block instanceof SaplingBlock) {

			if (context.getLevel().isClientSide) {
				BoneMealItem.addGrowthParticles(context.getLevel(), context.getClickedPos(), 100);
				return ActionResultType.SUCCESS;
			}

			BlockPos saplingPos = context.getClickedPos();
			TreesDreamWorld world = new TreesDreamWorld((ServerWorld) context.getLevel(), saplingPos);

			for (BlockPos pos : BlockPos.betweenClosed(-1, 0, -1, 1, 0, 1)) {
				if (context.getLevel()
					.getBlockState(saplingPos.offset(pos))
					.getBlock() == block)
					world.setBlockAndUpdate(pos.above(10), state.setValue(SaplingBlock.STAGE, 1));
			}

			((SaplingBlock) block).performBonemeal(world, world.getRandom(), BlockPos.ZERO.above(10),
				state.setValue(SaplingBlock.STAGE, 1));

			for (BlockPos pos : world.blocksAdded.keySet()) {
				BlockPos actualPos = pos.offset(saplingPos).below(10);
				BlockState newState = world.blocksAdded.get(pos);

				// Don't replace Bedrock
				if (context.getLevel()
					.getBlockState(actualPos)
					.getDestroySpeed(context.getLevel(), actualPos) == -1)
					continue;
				// Don't replace solid blocks with leaves
				if (!newState.isRedstoneConductor(world, pos)
					&& !context.getLevel()
						.getBlockState(actualPos)
						.getCollisionShape(context.getLevel(), actualPos)
						.isEmpty())
					continue;

				context.getLevel()
					.setBlockAndUpdate(actualPos, newState);
			}

			if (context.getPlayer() != null && !context.getPlayer()
				.isCreative())
				context.getItemInHand()
					.shrink(1);
			return ActionResultType.SUCCESS;

		}

		return super.useOn(context);
	}

	private class TreesDreamWorld extends PlacementSimulationServerWorld {
		private final BlockPos saplingPos;
		private final BlockState soil;

		protected TreesDreamWorld(ServerWorld wrapped, BlockPos saplingPos) {
			super(wrapped);
			this.saplingPos = saplingPos;
			soil = wrapped.getBlockState(saplingPos.below());
		}

		@Override
		public BlockState getBlockState(BlockPos pos) {
			if (pos.getY() <= 9)
				return soil;
			return super.getBlockState(pos);
		}

		@Override
		public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
			if (newState.getBlock() == Blocks.PODZOL)
				return true;
			return super.setBlock(pos, newState, flags);
		}
	}

}
