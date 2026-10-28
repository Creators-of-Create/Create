package com.simibubi.create.foundation.block;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;

public class DyedBlockList<T extends Block> implements Iterable<BlockEntry<T>> {

	private final BlockEntry<T>[] values;

	@SuppressWarnings("unchecked")
	public DyedBlockList(Function<DyeColor, BlockEntry<? extends T>> filler) {
		values = Arrays.stream(DyeColor.values()).map(filler).toArray(BlockEntry[]::new);
	}

	public BlockEntry<T> get(DyeColor color) {
		return values[color.ordinal()];
	}

	public boolean contains(Block block) {
		for (BlockEntry<?> entry : values) {
			if (entry.is(block)) {
				return true;
			}
		}
		return false;
	}

	public BlockEntry<T>[] toArray() {
		return Arrays.copyOf(values, values.length);
	}

	@Override @NotNull
	public Iterator<BlockEntry<T>> iterator() {
		return new Iterator<>() {
			private int index = 0;

			@Override
			public boolean hasNext() {
				return index < values.length;
			}

			@Override
			public BlockEntry<T> next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return values[index++];
			}
		};
	}

}
