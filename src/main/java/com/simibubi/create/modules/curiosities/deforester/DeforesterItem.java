package com.simibubi.create.modules.curiosities.deforester;

import com.simibubi.create.foundation.utility.TreeCutter;
import com.simibubi.create.foundation.utility.TreeCutter.Tree;
import com.simibubi.create.modules.curiosities.tools.AllToolTiers;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DeforesterItem extends AxeItem {

	public DeforesterItem(Properties builder) {
		super(AllToolTiers.RADIANT, 10.0F, -3.1F, builder);
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos,
			LivingEntity entityLiving) {

		if (state.isIn(BlockTags.LOGS) && !entityLiving.isSneaking()) {
			Tree tree = TreeCutter.cutTree(worldIn, pos);
			if (tree == null)
				return super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
			boolean dropBlock = !(entityLiving instanceof PlayerEntity) || !((PlayerEntity) entityLiving).isCreative();
			for (BlockPos log : tree.logs)
				worldIn.destroyBlock(log, dropBlock);
			for (BlockPos leaf : tree.leaves)
				worldIn.destroyBlock(leaf, dropBlock);
		}

		return super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
	}

}
