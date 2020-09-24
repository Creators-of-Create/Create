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

			TreesDreamWorld world = new TreesDreamWorld((ServerWorld) context.getWorld());
			BlockPos saplingPos = context.getPos();

			for (BlockPos pos : BlockPos.getAllInBoxMutable(-1, 0, -1, 1, 0, 1)) {
				if (context.getWorld()
					.getBlockState(saplingPos.add(pos))
					.getBlock() == block)
					world.setBlockState(pos.up(10), state.with(SaplingBlock.STAGE, 1));
			}

			((SaplingBlock) block).grow(world, world.getRandom(), BlockPos.ZERO.up(10),
				state.with(SaplingBlock.STAGE, 1));

			for (BlockPos pos : world.blocksAdded.keySet()) {
				BlockPos actualPos = pos.add(saplingPos)
					.down(10);

				// Don't replace Bedrock
				if (context.getWorld()
					.getBlockState(actualPos)
					.getBlockHardness(context.getWorld(), actualPos) == -1)
					continue;
				// Don't replace solid blocks with leaves
				if (!world.getBlockState(pos)
					.isNormalCube(world, pos)
					&& !context.getWorld()
						.getBlockState(actualPos)
						.getCollisionShape(context.getWorld(), actualPos)
						.isEmpty())
					continue;
				if (world.getBlockState(pos)
					.getBlock() == Blocks.GRASS_BLOCK
					|| world.getBlockState(pos)
						.getBlock() == Blocks.PODZOL)
					continue;

				context.getWorld()
					.setBlockState(actualPos, world.getBlockState(pos));
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

		protected TreesDreamWorld(ServerWorld wrapped) {
			super(wrapped);
		}

		@Override
		public BlockState getBlockState(BlockPos pos) {
			if (pos.getY() <= 9)
				return Blocks.GRASS_BLOCK.getDefaultState();
			return super.getBlockState(pos);
		}

	}



}
