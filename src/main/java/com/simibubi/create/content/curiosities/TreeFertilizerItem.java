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

public class TreeFertilizerItem extends Item {

	public TreeFertilizerItem(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		BlockState state = context.getWorld()
			.getBlockState(context.getPos());
		Block block = state.getBlock();
		if (block instanceof SaplingBlock) {

			if (context.getWorld().isRemote) {
				BoneMealItem.spawnBonemealParticles(context.getWorld(), context.getPos(), 100);
				return ActionResultType.SUCCESS;
			}

			BlockPos saplingPos = context.getPos();
			TreesDreamWorld world = new TreesDreamWorld((ServerWorld) context.getWorld(), saplingPos);

			for (BlockPos pos : BlockPos.getAllInBoxMutable(-1, 0, -1, 1, 0, 1)) {
				if (context.getWorld()
					.getBlockState(saplingPos.add(pos))
					.getBlock() == block)
					world.setBlockState(pos.up(10), state.with(SaplingBlock.STAGE, 1));
			}

			((SaplingBlock) block).grow(world, world.getRandom(), BlockPos.ZERO.up(10),
				state.with(SaplingBlock.STAGE, 1));

			for (BlockPos pos : world.blocksAdded.keySet()) {
				BlockPos actualPos = pos.add(saplingPos).down(10);
				BlockState newState = world.blocksAdded.get(pos);

				// Don't replace Bedrock
				if (context.getWorld()
					.getBlockState(actualPos)
					.getBlockHardness(context.getWorld(), actualPos) == -1)
					continue;
				// Don't replace solid blocks with leaves
				if (!newState.isNormalCube(world, pos)
					&& !context.getWorld()
						.getBlockState(actualPos)
						.getCollisionShape(context.getWorld(), actualPos)
						.isEmpty())
					continue;

				context.getWorld()
					.setBlockState(actualPos, newState);
			}

			if (context.getPlayer() != null && !context.getPlayer()
				.isCreative())
				context.getItem()
					.shrink(1);
			return ActionResultType.SUCCESS;

		}

		return super.onItemUse(context);
	}

	private class TreesDreamWorld extends PlacementSimulationServerWorld {
		private final BlockState soil;

		protected TreesDreamWorld(ServerWorld wrapped, BlockPos saplingPos) {
			super(wrapped);
			soil = wrapped.getBlockState(saplingPos.down());
		}

		@Override
		public BlockState getBlockState(BlockPos pos) {
			if (pos.getY() <= 9)
				return soil;
			return super.getBlockState(pos);
		}

		@Override
		public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
			if (newState.getBlock() == Blocks.PODZOL)
				return true;
			return super.setBlockState(pos, newState, flags);
		}
	}

}
