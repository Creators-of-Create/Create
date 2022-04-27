package com.simibubi.create.content.contraptions.components.structureMovement.glue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.simibubi.create.content.contraptions.components.structureMovement.BlockMovementChecks;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SuperGlueSelectionHelper {

	public static Pair<Set<BlockPos>, List<BlockFace>> searchGlueGroup(Level level, BlockPos startPos,
		BlockPos endPos) {
		if (endPos == null || startPos == null)
			return null;

		AABB bb = new AABB(startPos, endPos).expandTowards(1, 1, 1);

		List<BlockPos> frontier = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		Set<BlockPos> attached = new HashSet<>();
		List<BlockFace> glue = new ArrayList<>();

		visited.add(startPos);
		frontier.add(startPos);

		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
			attached.add(currentPos);

			Map<Direction, SuperGlueEntity> gatheredGlue = SuperGlueHandler.gatherGlue(level, currentPos);
			for (Direction d : Iterate.directions) {
				BlockPos offset = currentPos.relative(d);
				boolean gluePresent = gatheredGlue.containsKey(d);
				boolean alreadySticky = SuperGlueEntity.isSideSticky(level, currentPos, d)
					|| SuperGlueEntity.isSideSticky(level, offset, d.getOpposite());

				if (!alreadySticky && !gluePresent && !bb.contains(Vec3.atCenterOf(offset)))
					continue;
				if (!BlockMovementChecks.isMovementNecessary(level.getBlockState(offset), level, offset))
					continue;
				if (!SuperGlueEntity.isValidFace(level, currentPos, d)
					|| !SuperGlueEntity.isValidFace(level, offset, d.getOpposite()))
					continue;

				boolean glueNecessary = !gluePresent && !alreadySticky;

				if (visited.add(offset)) {
					frontier.add(offset);
					if (glueNecessary)
						glue.add(new BlockFace(offset, d));
				}
			}
		}

		if (attached.size() < 2 && attached.contains(endPos))
			return null;

		return Pair.of(attached, glue);
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
