package com.simibubi.create.modules.gardens;

import java.util.Random;

import com.simibubi.create.foundation.item.InfoItem;
import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;
import com.simibubi.create.foundation.utility.PlacementSimulationWorld;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TreeFertilizerItem extends InfoItem {

	public TreeFertilizerItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemDescription getDescription() {
		return new ItemDescription(Palette.Green)
				.withSummary("A powerful combination of minerals suitable for common tree types.")
				.withBehaviour("When used on Sapling", "Grows Trees regardless of their spacing Conditions").createTabs();
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		BlockState state = context.getWorld().getBlockState(context.getPos());
		Block block = state.getBlock();
		if (block instanceof SaplingBlock) {

			if (context.getWorld().isRemote) {
				BoneMealItem.spawnBonemealParticles(context.getWorld(), context.getPos(), 100);
				return ActionResultType.SUCCESS;
			}

			TreesDreamWorld world = new TreesDreamWorld(context.getWorld());
			BlockPos saplingPos = context.getPos();

			for (BlockPos pos : BlockPos.getAllInBoxMutable(-1, 0, -1, 1, 0, 1)) {
				if (context.getWorld().getBlockState(saplingPos.add(pos)).getBlock() == block)
					world.setBlockState(pos.up(10), state.with(SaplingBlock.STAGE, 1));
			}

			((SaplingBlock) block).grow(world, BlockPos.ZERO.up(10), state.with(SaplingBlock.STAGE, 1), new Random());

			for (BlockPos pos : world.blocksAdded.keySet()) {
				BlockPos actualPos = pos.add(saplingPos).down(10);

				// Don't replace Bedrock
				if (context.getWorld().getBlockState(actualPos).getBlockHardness(context.getWorld(), actualPos) == -1)
					continue;
				// Don't replace solid blocks with leaves
				if (!world.getBlockState(pos).isNormalCube(world, pos)
						&& context.getWorld().getBlockState(actualPos).isNormalCube(context.getWorld(), actualPos))
					continue;
				if (world.getBlockState(pos).getBlock() == Blocks.PODZOL
						&& context.getWorld().getBlockState(actualPos).getBlock() != Blocks.GRASS_BLOCK)
					continue;

				context.getWorld().setBlockState(actualPos, world.getBlockState(pos));
			}

			if (!context.getPlayer().isCreative())
				context.getItem().shrink(1);
			return ActionResultType.SUCCESS;

		}

		return super.onItemUse(context);
	}

	private class TreesDreamWorld extends PlacementSimulationWorld {

		protected TreesDreamWorld(World wrapped) {
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
