package com.simibubi.create.content.contraptions.components.crafter;

import static com.simibubi.create.content.contraptions.base.HorizontalKineticBlock.HORIZONTAL_FACING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Predicates;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pointing;

import net.minecraft.block.BlockState;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FireworkRocketRecipe;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class RecipeGridHandler {

	public static List<MechanicalCrafterTileEntity> getAllCraftersOfChain(MechanicalCrafterTileEntity root) {
		return getAllCraftersOfChainIf(root, Predicates.alwaysTrue());
	}

	public static List<MechanicalCrafterTileEntity> getAllCraftersOfChainIf(MechanicalCrafterTileEntity root,
		Predicate<MechanicalCrafterTileEntity> test) {
		return getAllCraftersOfChainIf(root, test, false);
	}

	public static List<MechanicalCrafterTileEntity> getAllCraftersOfChainIf(MechanicalCrafterTileEntity root,
		Predicate<MechanicalCrafterTileEntity> test, boolean poweredStart) {
		List<MechanicalCrafterTileEntity> crafters = new ArrayList<>();
		List<Pair<MechanicalCrafterTileEntity, MechanicalCrafterTileEntity>> frontier = new ArrayList<>();
		Set<MechanicalCrafterTileEntity> visited = new HashSet<>();
		frontier.add(Pair.of(root, null));

		boolean powered = false;
		boolean empty = false;
		boolean allEmpty = true;

		while (!frontier.isEmpty()) {
			Pair<MechanicalCrafterTileEntity, MechanicalCrafterTileEntity> pair = frontier.remove(0);
			MechanicalCrafterTileEntity current = pair.getKey();
			MechanicalCrafterTileEntity last = pair.getValue();

			if (visited.contains(current))
				return null;
			if (!(test.test(current)))
				empty = true;
			else
				allEmpty = false;
			if (poweredStart && current.getLevel()
				.hasNeighborSignal(current.getBlockPos()))
				powered = true;

			crafters.add(current);
			visited.add(current);

			MechanicalCrafterTileEntity target = getTargetingCrafter(current);
			if (target != last && target != null)
				frontier.add(Pair.of(target, current));
			for (MechanicalCrafterTileEntity preceding : getPrecedingCrafters(current))
				if (preceding != last)
					frontier.add(Pair.of(preceding, current));
		}

		return empty && !powered || allEmpty ? null : crafters;
	}

	public static MechanicalCrafterTileEntity getTargetingCrafter(MechanicalCrafterTileEntity crafter) {
		BlockState state = crafter.getBlockState();
		if (!isCrafter(state))
			return null;

		BlockPos targetPos = crafter.getBlockPos()
			.relative(MechanicalCrafterBlock.getTargetDirection(state));
		MechanicalCrafterTileEntity targetTE = CrafterHelper.getCrafter(crafter.getLevel(), targetPos);
		if (targetTE == null)
			return null;

		BlockState targetState = targetTE.getBlockState();
		if (!isCrafter(targetState))
			return null;
		if (state.getValue(HORIZONTAL_FACING) != targetState.getValue(HORIZONTAL_FACING))
			return null;
		return targetTE;
	}

	public static List<MechanicalCrafterTileEntity> getPrecedingCrafters(MechanicalCrafterTileEntity crafter) {
		BlockPos pos = crafter.getBlockPos();
		World world = crafter.getLevel();
		List<MechanicalCrafterTileEntity> crafters = new ArrayList<>();
		BlockState blockState = crafter.getBlockState();
		if (!isCrafter(blockState))
			return crafters;

		Direction blockFacing = blockState.getValue(HORIZONTAL_FACING);
		Direction blockPointing = MechanicalCrafterBlock.getTargetDirection(blockState);
		for (Direction facing : Iterate.directions) {
			if (blockFacing.getAxis() == facing.getAxis())
				continue;
			if (blockPointing == facing)
				continue;

			BlockPos neighbourPos = pos.relative(facing);
			BlockState neighbourState = world.getBlockState(neighbourPos);
			if (!isCrafter(neighbourState))
				continue;
			if (MechanicalCrafterBlock.getTargetDirection(neighbourState) != facing.getOpposite())
				continue;
			if (blockFacing != neighbourState.getValue(HORIZONTAL_FACING))
				continue;
			MechanicalCrafterTileEntity te = CrafterHelper.getCrafter(world, neighbourPos);
			if (te == null)
				continue;

			crafters.add(te);
		}

		return crafters;
	}

	private static boolean isCrafter(BlockState state) {
		return AllBlocks.MECHANICAL_CRAFTER.has(state);
	}

	public static ItemStack tryToApplyRecipe(World world, GroupedItems items) {
		items.calcStats();
		CraftingInventory craftinginventory = new MechanicalCraftingInventory(items);
		ItemStack result = null;
		if (AllConfigs.SERVER.recipes.allowRegularCraftingInCrafter.get())
			result = world.getRecipeManager()
				.getRecipeFor(IRecipeType.CRAFTING, craftinginventory, world)
				.filter(r -> isRecipeAllowed(r, craftinginventory))
				.map(r -> r.assemble(craftinginventory))
				.orElse(null);
		if (result == null)
			result = AllRecipeTypes.MECHANICAL_CRAFTING.find(craftinginventory, world)
				.map(r -> r.assemble(craftinginventory))
				.orElse(null);
		return result;
	}

	public static boolean isRecipeAllowed(ICraftingRecipe recipe, CraftingInventory inventory) {
		if (!AllConfigs.SERVER.recipes.allowBiggerFireworksInCrafter.get() && recipe instanceof FireworkRocketRecipe) {
			int numItems = IntStream.range(0, inventory.getContainerSize())
				.map(i -> inventory.getItem(i).isEmpty() ? 0 : 1)
				.sum();
			if (numItems > 9)
				return false;
		}
		if (AllRecipeTypes.isManualRecipe(recipe))
			return false;
		return true;
	}

	public static class GroupedItems {
		Map<Pair<Integer, Integer>, ItemStack> grid = new HashMap<>();
		int minX, minY, maxX, maxY, width, height;
		boolean statsReady;

		public GroupedItems() {}

		public GroupedItems(ItemStack stack) {
			grid.put(Pair.of(0, 0), stack);
		}

		public void mergeOnto(GroupedItems other, Pointing pointing) {
			int xOffset = pointing == Pointing.LEFT ? 1 : pointing == Pointing.RIGHT ? -1 : 0;
			int yOffset = pointing == Pointing.DOWN ? 1 : pointing == Pointing.UP ? -1 : 0;
			grid.forEach(
				(pair, stack) -> other.grid.put(Pair.of(pair.getKey() + xOffset, pair.getValue() + yOffset), stack));
			other.statsReady = false;
		}

		public void write(CompoundNBT nbt) {
			ListNBT gridNBT = new ListNBT();
			grid.forEach((pair, stack) -> {
				CompoundNBT entry = new CompoundNBT();
				entry.putInt("x", pair.getKey());
				entry.putInt("y", pair.getValue());
				entry.put("item", stack.serializeNBT());
				gridNBT.add(entry);
			});
			nbt.put("Grid", gridNBT);
		}

		public static GroupedItems read(CompoundNBT nbt) {
			GroupedItems items = new GroupedItems();
			ListNBT gridNBT = nbt.getList("Grid", NBT.TAG_COMPOUND);
			gridNBT.forEach(inbt -> {
				CompoundNBT entry = (CompoundNBT) inbt;
				int x = entry.getInt("x");
				int y = entry.getInt("y");
				ItemStack stack = ItemStack.of(entry.getCompound("item"));
				items.grid.put(Pair.of(x, y), stack);
			});
			return items;
		}

		public void calcStats() {
			if (statsReady)
				return;
			statsReady = true;

			minX = 0;
			minY = 0;
			maxX = 0;
			maxY = 0;

			for (Pair<Integer, Integer> pair : grid.keySet()) {
				int x = pair.getKey();
				int y = pair.getValue();
				minX = Math.min(minX, x);
				minY = Math.min(minY, y);
				maxX = Math.max(maxX, x);
				maxY = Math.max(maxY, y);
			}

			width = maxX - minX + 1;
			height = maxY - minY + 1;
		}

	}

}
