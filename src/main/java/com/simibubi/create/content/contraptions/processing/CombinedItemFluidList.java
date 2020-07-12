package com.simibubi.create.content.contraptions.processing;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.function.Consumer;

public class CombinedItemFluidList {
	private final ArrayList<ItemStack> itemStacks = new ArrayList<>();
	private final ArrayList<FluidStack> fluidStacks = new ArrayList<>();

	public void add(ItemStack itemstack) {
		itemStacks.add(itemstack);
	}

	public void add(FluidStack fluidStack) {
		fluidStacks.add(fluidStack);
	}

	public void forEachItemStack(Consumer<? super ItemStack> itemStackConsumer) {
		itemStacks.forEach(itemStackConsumer);
	}

	public void forEachFluidStack(Consumer<? super FluidStack> fluidStackConsumer) {
		fluidStacks.forEach(fluidStackConsumer);
	}

	public ArrayList<ItemStack> getItemStacks() {
		return itemStacks;
	}

	public ArrayList<FluidStack> getFluidStacks() {
		return fluidStacks;
	}
}
