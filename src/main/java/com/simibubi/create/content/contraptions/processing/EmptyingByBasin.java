package com.simibubi.create.content.contraptions.processing;

import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.foundation.utility.Pair;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.RecipeWrapper;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

public class EmptyingByBasin {

	static RecipeWrapper wrapper = new RecipeWrapper(new ItemStackHandler(1));

	public static boolean canItemBeEmptied(Level world, ItemStack stack) {
		if (stack.getItem() instanceof PotionItem)
			return true;

		wrapper.setItem(0, stack);
		if (AllRecipeTypes.EMPTYING.find(wrapper, world)
			.isPresent())
			return true;

		return TransferUtil.getFluidContained(stack).isPresent();
	}

	public static Pair<FluidStack, ItemStack> emptyItem(Level world, ItemStack stack, boolean simulate) {
		FluidStack resultingFluid = FluidStack.EMPTY;
		ItemStack resultingItem = ItemStack.EMPTY;

		if (stack.getItem() instanceof PotionItem)
			return PotionFluidHandler.emptyPotion(stack, simulate);

		wrapper.setItem(0, stack);
		Optional<Recipe<RecipeWrapper>> recipe = AllRecipeTypes.EMPTYING.find(wrapper, world);
		if (recipe.isPresent()) {
			EmptyingRecipe emptyingRecipe = (EmptyingRecipe) recipe.get();
			List<ItemStack> results = emptyingRecipe.rollResults();
			if (!simulate)
				stack.shrink(1);
			resultingItem = results.isEmpty() ? ItemStack.EMPTY : results.get(0);
			resultingFluid = emptyingRecipe.getResultingFluid();
			return Pair.of(resultingFluid, resultingItem);
		}

		ItemStack split = stack.copy();
		split.setCount(1);
		ContainerItemContext ctx = ContainerItemContext.withInitial(stack);
		Storage<FluidVariant> tank = FluidStorage.ITEM.find(stack, ctx);
		if (tank == null)
			return Pair.of(resultingFluid, resultingItem);
		try (Transaction t = TransferUtil.getTransaction()) {
			resultingFluid = TransferUtil.extractAnyFluid(tank, FluidConstants.BUCKET);
			resultingItem = ctx.getItemVariant().toStack((int) ctx.getAmount());
			if (!simulate) {
				stack.shrink(1);
				t.commit();
			}

			return Pair.of(resultingFluid, resultingItem);
		}
	}

}
