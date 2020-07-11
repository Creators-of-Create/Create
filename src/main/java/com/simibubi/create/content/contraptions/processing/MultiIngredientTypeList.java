package com.simibubi.create.content.contraptions.processing;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.function.Consumer;

public class MultiIngredientTypeList {
	private final ArrayList<ItemStack> itemIngredients = new ArrayList<>();
	private final ArrayList<FluidStack> fluidIngredients = new ArrayList<>();

	public void add(ItemStack itemstack) {
		itemIngredients.add(itemstack);
	}

	public void add(FluidStack fluidStack) {
		fluidIngredients.add(fluidStack);
	}

	public void forEachItemStack(Consumer<? super ItemStack> itemStackConsumer) {
		itemIngredients.forEach(itemStackConsumer);
	}

	public void forEachFluidStack(Consumer<? super FluidStack> fluidStackConsumer) {
		fluidIngredients.forEach(fluidStackConsumer);
	}
}
