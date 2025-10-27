package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record BookCopyAttribute(int generation) implements ItemAttribute {
	public static final MapCodec<BookCopyAttribute> CODEC = ExtraCodecs.NON_NEGATIVE_INT
			.xmap(BookCopyAttribute::new, BookCopyAttribute::generation)
			.fieldOf("value");

	public static final StreamCodec<ByteBuf, BookCopyAttribute> STREAM_CODEC = ByteBufCodecs.INT
		.map(BookCopyAttribute::new, BookCopyAttribute::generation);

	private static int extractGeneration(ItemStack stack) {
		if (stack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
			return stack.get(DataComponents.WRITTEN_BOOK_CONTENT).generation();
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
