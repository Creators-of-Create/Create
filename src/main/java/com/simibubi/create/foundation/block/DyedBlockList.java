package com.simibubi.create.foundation.block;

import java.util.Arrays;
import java.util.function.Function;

import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.DyeColor;

public class DyedBlockList<T extends Block> {

	private static final int COLOR_AMOUNT = DyeColor.values().length;

	private final BlockEntry<?>[] values = new BlockEntry<?>[COLOR_AMOUNT];

	public DyedBlockList(Function<DyeColor, BlockEntry<? extends T>> filler) {
		for (DyeColor color : DyeColor.values()) {
			values[color.ordinal()] = filler.apply(color);
		}
	}

	@SuppressWarnings("unchecked")
	public BlockEntry<T> get(DyeColor color) {
		return (BlockEntry<T>) values[color.ordinal()];
	}

	public boolean contains(Block block) {
		for (BlockEntry<?> entry : values) {
			if (entry.is(block)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public BlockEntry<T>[] toArray() {
		return (BlockEntry<T>[]) Arrays.copyOf(values, values.length);
	}

}
