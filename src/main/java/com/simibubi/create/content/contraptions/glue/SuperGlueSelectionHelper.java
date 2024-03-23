package com.simibubi.create.content.contraptions.glue;

import java.util.Set;

import com.simibubi.create.content.contraptions.BlockMovementChecks;
import com.simibubi.create.foundation.utility.Iterate;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SuperGlueSelectionHelper {

	public static Set<BlockPos> searchGlueGroup(Level level, BlockPos startPos, BlockPos endPos, boolean includeOther) {
		if (endPos == null || startPos == null)
			return null;

		AABB bb = SuperGlueEntity.span(startPos, endPos);
		int numBlocks = (int) (bb.getXsize() * bb.getYsize() * bb.getZsize());

		PriorityQueue<BlockPos> frontier = new ObjectArrayFIFOQueue<>(numBlocks);
		Set<BlockPos> visited = new ObjectOpenHashSet<>(numBlocks);
		Set<BlockPos> attached = new ObjectOpenHashSet<>(numBlocks);
		Set<SuperGlueEntity> cachedEntities = new ObjectOpenHashSet<>();

		visited.add(startPos);
		frontier.enqueue(startPos);

		while(!frontier.isEmpty()) {
			BlockPos currentPos = frontier.dequeue();
			attached.add(currentPos);

			for(Direction d : Iterate.directions) {
				BlockPos offset = currentPos.relative(d);

				if(visited.add(offset) && shouldAttach(level, offset, d, cachedEntities, includeOther, bb, currentPos))
					frontier.enqueue(offset);
			}
		}

		if (attached.size() < 2 && attached.contains(endPos))
			return null;

		return attached;
	}

	private static boolean shouldAttach(Level level, BlockPos offset, Direction d, Set<SuperGlueEntity> cachedEntities, boolean includeOther, AABB bb, BlockPos currentPos) {
		BlockState state = level.getChunkAt(offset).getBlockState(offset);
		Block block = state.getBlock();

		// fast-paths for common non-sticky blocks
		if(block == Blocks.AIR || block == Blocks.WATER) return false;

		if(!BlockMovementChecks.isMovementNecessary(state, level, offset)) {
			return false;
		}

		if(includeOther) {
			boolean canAttach = SuperGlueEntity.isGlued(level, currentPos, d, cachedEntities)
					|| SuperGlueEntity.isSideSticky(state, d) || SuperGlueEntity.isSideSticky(state, d.getOpposite())
					|| bb.contains(Vec3.atCenterOf(offset));
			if(!canAttach) {
				return false;
			}
		}

		if(!SuperGlueEntity.isValidFace(state, level, offset, d.getOpposite()) ||
				!SuperGlueEntity.isValidFace(level.getBlockState(currentPos), level, currentPos, d)) {
			return false;
		}

		return true;
	}

	public static boolean collectGlueFromInventory(Player player, int requiredAmount, boolean simulate) {
		if (player.getAbilities().instabuild)
			return true;
		if (requiredAmount == 0)
			return true;

		NonNullList<ItemStack> items = player.getInventory().items;
		for (int i = -1; i < items.size(); i++) {
			int slot = i == -1 ? player.getInventory().selected : i;
			ItemStack stack = items.get(slot);
			if (stack.isEmpty())
				continue;
			if (!stack.isDamageableItem())
				continue;
			if (!(stack.getItem() instanceof SuperGlueItem))
				continue;

			int charges = Math.min(requiredAmount, stack.getMaxDamage() - stack.getDamageValue());

			if (!simulate)
				stack.hurtAndBreak(charges, player, i == -1 ? SuperGlueItem::onBroken : $ -> {
				});

			requiredAmount -= charges;
			if (requiredAmount <= 0)
				return true;
		}

		return false;
	}

}
