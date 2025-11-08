package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record ItemNameAttribute(String itemName) implements ItemAttribute {
	public static final MapCodec<ItemNameAttribute> CODEC = Codec.STRING
			.xmap(ItemNameAttribute::new, ItemNameAttribute::itemName)
			.fieldOf("value");

	public static final StreamCodec<ByteBuf, ItemNameAttribute> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
		.map(ItemNameAttribute::new, ItemNameAttribute::itemName);

	private static String extractCustomName(ItemStack stack, Level level) {
		if (stack.has(DataComponents.CUSTOM_NAME)) {
			try {
				String customName = stack.getOrDefault(DataComponents.CUSTOM_NAME, Component.empty()).getString();
				Component component = Component.Serializer.fromJson(customName, level.registryAccess());
				if (component != null) {
					return component.getString();
				}
			} catch (JsonParseException ignored) {
			}
		}
		return "";
	}

	@Override
	public boolean appliesTo(ItemStack itemStack, Level level) {
		return extractCustomName(itemStack, level).equals(itemName);
	}

	@Override
	public String getTranslationKey() {
		return "has_name";
	}

	@Override
	public Object[] getTranslationParameters() {
		return new Object[]{itemName};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.HAS_NAME;
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new ItemNameAttribute("dummy");
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			List<ItemAttribute> list = new ArrayList<>();

			String name = extractCustomName(stack, level);
			if (!name.isEmpty()) {
				list.add(new ItemNameAttribute(name));
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
