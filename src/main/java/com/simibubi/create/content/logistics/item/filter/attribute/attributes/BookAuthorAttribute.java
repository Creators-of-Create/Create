package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BookAuthorAttribute implements ItemAttribute {
	private String author;

	public BookAuthorAttribute(String author) {
		this.author = author;
	}

	private static String extractAuthor(ItemStack stack) {
		CompoundTag nbt = stack.getTag();
		if (nbt != null && nbt.contains("author")) {
			return nbt.getString("author");
		}
		return "";
	}

	@Override
	public boolean appliesTo(ItemStack itemStack, Level level) {
		return extractAuthor(itemStack).equals(author);
	}

	@Override
	public String getTranslationKey() {
		return "book_author";
	}

	@Override
	public Object[] getTranslationParameters() {
		return new Object[]{author};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.BOOK_AUTHOR;
	}

	@Override
	public void save(CompoundTag nbt) {
		nbt.putString("author", author);
	}

	@Override
	public void load(CompoundTag nbt) {
		author = nbt.getString("author");
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new BookAuthorAttribute("dummy");
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			List<ItemAttribute> list = new ArrayList<>();

			String name = BookAuthorAttribute.extractAuthor(stack);
			if (!name.isEmpty()) {
				list.add(new BookAuthorAttribute(name));
			}

			return list;
		}
	}
}
