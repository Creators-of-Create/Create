package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.Level;

public class BookCopyAttribute implements ItemAttribute {
	private int generation;

	public BookCopyAttribute(int generation) {
		this.generation = generation;
	}

	private static int extractGeneration(ItemStack stack) {
		CompoundTag nbt = stack.getTag();
		if (nbt != null && stack.getItem() instanceof WrittenBookItem) {
			return nbt.getInt("generation");
		}
		return -1;
	}

	@Override
	public boolean appliesTo(ItemStack itemStack, Level level) {
		return extractGeneration(itemStack) == generation;
	}

	@Override
	public String getTranslationKey() {
		return switch (generation) {
			case 0 -> "book_copy_original";
			case 1 -> "book_copy_first";
			case 2 -> "book_copy_second";
			default -> "book_copy_tattered";
		};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.BOOK_COPY;
	}

	@Override
	public void save(CompoundTag nbt) {
		nbt.putInt("generation", generation);
	}

	@Override
	public void load(CompoundTag nbt) {
		generation = nbt.getInt("generation");
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new BookCopyAttribute(-1);
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			List<ItemAttribute> list = new ArrayList<>();

			int generation = BookCopyAttribute.extractGeneration(stack);
			if (generation >= 0) {
				list.add(new BookCopyAttribute(generation));
			}

			return list;
		}
	}
}
