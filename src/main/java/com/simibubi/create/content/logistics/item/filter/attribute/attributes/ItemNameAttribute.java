package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonParseException;
import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemNameAttribute implements ItemAttribute {
	private String itemName;

	public ItemNameAttribute(String itemName) {
		this.itemName = itemName;
	}

	private static String extractCustomName(ItemStack stack) {
		CompoundTag compoundnbt = stack.getTagElement("display");
		if (compoundnbt != null && compoundnbt.contains("Name", 8)) {
			try {
				Component itextcomponent = Component.Serializer.fromJson(compoundnbt.getString("Name"));
				if (itextcomponent != null) {
					return itextcomponent.getString();
				}
			} catch (JsonParseException ignored) {
			}
		}
		return "";
	}

	@Override
	public boolean appliesTo(ItemStack itemStack, Level level) {
		return extractCustomName(itemStack).equals(itemName);
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

	@Override
	public void save(CompoundTag nbt) {
		nbt.putString("name", itemName);
	}

	@Override
	public void load(CompoundTag nbt) {
		itemName = nbt.getString("name");
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new ItemNameAttribute("dummy");
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			List<ItemAttribute> list = new ArrayList<>();

			String name = extractCustomName(stack);
			if (!name.isEmpty()) {
				list.add(new ItemNameAttribute(name));
			}

			return list;
		}
	}
}
