package com.simibubi.create.content.logistics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class BigItemStack {
	public static final Codec<BigItemStack> CODEC = RecordCodecBuilder.create(i -> i.group(
		ItemStack.OPTIONAL_CODEC.fieldOf("item_stack").forGetter(s -> s.stack),
		ExtraCodecs.NON_NEGATIVE_INT.fieldOf("count").forGetter(s -> s.count)
	).apply(i, BigItemStack::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BigItemStack> STREAM_CODEC = StreamCodec.composite(
		ItemStack.OPTIONAL_STREAM_CODEC, s -> s.stack,
		ByteBufCodecs.VAR_INT, s -> s.count,
		BigItemStack::new
	);

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

	public boolean isInfinite() {
		return count >= INF;
	}

	public static BigItemStack receive(RegistryFriendlyByteBuf buffer) {
		return new BigItemStack(ItemStack.STREAM_CODEC.decode(buffer), buffer.readVarInt());
	}

	public static Comparator<? super BigItemStack> comparator() {
		return (i1, i2) -> Integer.compare(i2.count, i1.count);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof BigItemStack other)
			return ItemStack.isSameItemSameComponents(stack, other.stack) && count == other.count;
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
