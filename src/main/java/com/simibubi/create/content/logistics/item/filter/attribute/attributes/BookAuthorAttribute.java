package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record BookAuthorAttribute(String author) implements ItemAttribute {
	public static final MapCodec<BookAuthorAttribute> CODEC = Codec.STRING
			.xmap(BookAuthorAttribute::new, BookAuthorAttribute::author)
			.fieldOf("value");

	public static final StreamCodec<ByteBuf, BookAuthorAttribute> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
		.map(BookAuthorAttribute::new, BookAuthorAttribute::author);

	private static String extractAuthor(ItemStack stack) {
		if (stack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
			return stack.get(DataComponents.WRITTEN_BOOK_CONTENT).author();
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

		@Override
		public MapCodec<? extends ItemAttribute> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, ? extends ItemAttribute> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
