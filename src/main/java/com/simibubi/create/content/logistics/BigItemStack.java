package com.simibubi.create.content.logistics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class BigItemStack {

	public static final int INF = 1_000_000_000;
	
	public ItemStack stack;
	public int count;

	public BigItemStack(ItemStack stack) {
		this(stack, 1);
	}
	
	public BigItemStack(ItemStack stack, int count) {
		this.stack = stack;
		this.count = count;
	}
	
	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.put("Item", stack.serializeNBT());
		tag.putInt("Amount", count);
		return tag;
	}

	public static BigItemStack read(CompoundTag tag) {
		return new BigItemStack(ItemStack.of(tag.getCompound("Item")), tag.getInt("Amount"));
	}

	public void send(FriendlyByteBuf buffer) {
		buffer.writeItem(stack);
		buffer.writeVarInt(count);
	}
	
	public boolean isInfinite() {
		return count >= INF;
	}

	public static BigItemStack receive(FriendlyByteBuf buffer) {
		return new BigItemStack(buffer.readItem(), buffer.readVarInt());
	}
	
	public static Comparator<? super BigItemStack> comparator() {
		return (i1, i2) -> Integer.compare(i2.count, i1.count);
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof BigItemStack other)
			return Objects.equals(stack, other.stack) && count == other.count;
		return false;
	}

	@Override
	public int hashCode() {
		return (nullHash(stack) * 31) ^ Integer.hashCode(count);
	}

	int nullHash(Object o) {
		return o == null ? 0 : o.hashCode();
	}

	@Override
	public String toString() {
		return "(" + stack.getHoverName()
			.getString() + " x" + count + ")";
	}
	
	public static List<BigItemStack> duplicateWrappers(List<BigItemStack> list) {
		List<BigItemStack> copy = new ArrayList<>();
		for (BigItemStack bigItemStack : list)
			copy.add(new BigItemStack(bigItemStack.stack, bigItemStack.count));
		return copy;
	}
	
}
