package com.simibubi.create.content.contraptions.glue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import com.simibubi.create.api.contraption.BlockMovementChecks;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SuperGlueSelectionHelper {

	public static Set<BlockPos> searchGlueGroup(Level level, BlockPos startPos, BlockPos endPos, boolean includeOther) {
		return searchGlueGroup(level, startPos, endPos, includeOther, true);
	}

	public static boolean isGlueGroupConnected(Level level, BlockPos startPos, BlockPos endPos, boolean includeOther) {
		return searchGlueGroup(level, startPos, endPos, includeOther, false) != null;
	}

	private static Set<BlockPos> searchGlueGroup(Level level, BlockPos startPos, BlockPos endPos, boolean includeOther,
		boolean collectAttached) {
		if (endPos == null || startPos == null)
			return null;
		if (startPos.equals(endPos))
			return null;

		AABB bb = SuperGlueEntity.span(startPos, endPos);

		Deque<BlockPos> frontier = new ArrayDeque<>();
		LongSet visited = new LongOpenHashSet();
		Set<BlockPos> attached = collectAttached ? new HashSet<>() : null;
		Set<SuperGlueEntity> cachedOther = includeOther ? new HashSet<>() : null;
		Long2ObjectOpenHashMap<BlockState> stateCache = new Long2ObjectOpenHashMap<>();

		visited.add(startPos.asLong());
		frontier.add(startPos);
		boolean foundEnd = false;

		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.removeFirst();
			if (collectAttached)
				attached.add(currentPos);
			if (currentPos.equals(endPos))
				foundEnd = true;

			BlockState currentState = getState(level, stateCache, currentPos);
			for (Direction d : Iterate.directions) {
				BlockPos offset = currentPos.relative(d);
				BlockState offsetState = getState(level, stateCache, offset);
				boolean gluePresent = includeOther && SuperGlueEntity.isGlued(level, currentPos, d, cachedOther);
				boolean alreadySticky = includeOther && SuperGlueEntity.isSideSticky(currentState, d)
					|| SuperGlueEntity.isSideSticky(offsetState, d.getOpposite());

				if (!alreadySticky && !gluePresent && !bb.contains(Vec3.atCenterOf(offset)))
					continue;
				if (!BlockMovementChecks.isMovementNecessary(offsetState, level, offset))
					continue;
				if (!SuperGlueEntity.isValidFace(level, currentPos, currentState, d)
					|| !SuperGlueEntity.isValidFace(level, offset, offsetState, d.getOpposite()))
					continue;

				if (offset.equals(endPos) && !collectAttached)
					return Set.of(endPos);

				if (visited.add(offset.asLong()))
					frontier.add(offset);
			}
		}

		if (!foundEnd)
			return null;

		return attached;
	}

	private static BlockState getState(Level level, Long2ObjectOpenHashMap<BlockState> cache, BlockPos pos) {
		long key = pos.asLong();
		BlockState cached = cache.get(key);
		if (cached != null)
			return cached;
		BlockState state = level.getBlockState(pos);
		cache.put(key, state);
		return state;
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
			if (!(stack.getItem() instanceof SuperGlueItem))
				continue;

			int charges = Math.min(requiredAmount, stack.getMaxDamage() - stack.getDamageValue());

			stack.hurtAndBreak(charges, player, EquipmentSlot.MAINHAND);

			requiredAmount -= charges;
			if (requiredAmount <= 0)
				return true;
		}

		return false;
	}

}
