package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class InTagAttribute implements ItemAttribute {
	@ApiStatus.Internal
	public TagKey<Item> tag;

	public InTagAttribute(TagKey<Item> tag) {
		this.tag = tag;
	}

	@Override
	public boolean appliesTo(ItemStack stack, Level level) {
		return stack.is(tag);
	}

	@Override
	public String getTranslationKey() {
		return "in_tag";
	}

	@Override
	public Object[] getTranslationParameters() {
		return new Object[]{"#" + tag.location()};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.IN_TAG;
	}

	@Override
	public void save(CompoundTag nbt) {
		NBTHelper.writeResourceLocation(nbt, "tag", tag.location());
	}

	@Override
	public void load(CompoundTag nbt) {
		tag = ItemTags.create(NBTHelper.readResourceLocation(nbt, "tag"));
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new InTagAttribute(ItemTags.LOGS);
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			return stack.getTags()
					.map(InTagAttribute::new)
					.collect(Collectors.toList());
		}
	}
}
