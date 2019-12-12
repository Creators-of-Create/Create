package com.simibubi.create.modules.contraptions.processing;

import java.util.Random;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class StochasticOutput {

	private static Random r = new Random();
	private ItemStack stack;
	private float chance;

	public StochasticOutput(ItemStack stack, float chance) {
		this.stack = stack;
		this.chance = chance;
	}

	public ItemStack getStack() {
		return stack;
	}

	public float getChance() {
		return chance;
	}

	public ItemStack rollOutput() {
		int outputAmount = stack.getCount();
		for (int roll = 0; roll < stack.getCount(); roll++)
			if (r.nextFloat() > chance)
				outputAmount--;
		return outputAmount > 0 ? new ItemStack(stack.getItem(), outputAmount) : ItemStack.EMPTY;
	}
	
	public void write(PacketBuffer buf) {
		buf.writeItemStack(getStack());
		buf.writeFloat(getChance());
	}
	
	public static StochasticOutput read(PacketBuffer buf) {
		return new StochasticOutput(buf.readItemStack(), buf.readFloat());
	}

}
