package com.simibubi.create.compat.dynamictrees;

import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.util.BranchDestructionData;
import com.simibubi.create.foundation.utility.AbstractBlockBreakQueue;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DynamicTree extends AbstractBlockBreakQueue {
	private final BlockPos startCutPos;

	public DynamicTree(BlockPos startCutPos) {
		this.startCutPos = startCutPos;
	}

	@Override
	public void destroyBlocks(World world, ItemStack toDamage, @Nullable PlayerEntity playerEntity, BiConsumer<BlockPos, ItemStack> drop) {
		BranchBlock start = TreeHelper.getBranch(world.getBlockState(startCutPos));
		if (start == null)
			return;

		BranchDestructionData data = start.destroyBranchFromNode(world, startCutPos, Direction.DOWN, false, playerEntity);

		// Feed all the tree drops to drop bi-consumer
		data.leavesDrops.forEach(stackPos -> drop.accept(stackPos.pos.add(startCutPos), stackPos.stack));
		start.getLogDrops(world, startCutPos, data.species, data.woodVolume).forEach(stack -> drop.accept(startCutPos, stack));
	}

	public static boolean isDynamicBranch(Block block) {
		return TreeHelper.isBranch(block);
	}
}
